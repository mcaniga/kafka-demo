package com.example.kafkademo.speedFraudDetection.kafka;

import com.example.kafkademo.speedFraudDetection.dto.GpsCoordinates;
import com.example.kafkademo.speedFraudDetection.dto.TapAccumulator;
import com.example.kafkademo.speedFraudDetection.dto.ValidatedTap;
import com.example.kafkademo.speedFraudDetection.kafka.serdes.ObjectMapperSerde;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SessionWindows;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.kafkademo.speedFraudDetection.service.GpsUtils.calculateDistanceInKmh;

@Slf4j
public class SpeedFraudPipeline implements Detector {
    private static final Serde<String> STRING_SERDE = Serdes.String();

    private ScheduledExecutorService service;

    private final Integer inactivityGapInHours;
    private final String inputTopic;
    private final String outputTopic;
    private final Integer fraudSpeed;
    private final Properties kafkaConfig;
    private final ObjectMapper objectMapper;
    private final TapTimestampExtractor tapTimestampExtractor;

    private KafkaStreams streams;

    public SpeedFraudPipeline(Integer inactivityGapInHours,
                              String inputTopic,
                              String outputTopic,
                              Integer fraudSpeedInKmh,
                              Properties kafkaConfig,
                              ObjectMapper objectMapper
    ) {
        this.inactivityGapInHours = inactivityGapInHours;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.fraudSpeed = fraudSpeedInKmh;
        this.kafkaConfig = kafkaConfig;
        this.objectMapper = objectMapper;
        this.tapTimestampExtractor = new TapTimestampExtractor();
    }

    public Topology buildPipeline(StreamsBuilder streamsBuilder) {
        final var tapSerde = new ObjectMapperSerde<>(ValidatedTap.class, objectMapper);
        final var accumulatorSerde = new ObjectMapperSerde<>(TapAccumulator.class, objectMapper);
        final Duration windowSize = Duration.ofHours(inactivityGapInHours);
        final SessionWindows sessionWindows = SessionWindows.ofInactivityGapWithNoGrace(windowSize);

        streamsBuilder
                .stream(inputTopic, Consumed.with(STRING_SERDE, tapSerde)
                        .withTimestampExtractor(tapTimestampExtractor))
                .selectKey((k, v) -> v.getMediumId())
                .groupByKey()
                .windowedBy(sessionWindows)
                .aggregate(TapAccumulator::new,
                        this::fraudTaps,
                        this::merger,
                        Materialized.with(STRING_SERDE, accumulatorSerde)
                )
                .toStream()
                .filter((k, v) -> v != null && v.getFraudTap() != null)
                .map((key, value) -> KeyValue.pair(key.key(), value.getFraudTap()))
                // need to map Windowed to string (or provide serde)
                .to(outputTopic, Produced.with(STRING_SERDE, tapSerde));

        return streamsBuilder.build();
    }

    private TapAccumulator fraudTaps(String key, ValidatedTap tap, TapAccumulator accumulator) {

        log.info("Previous taps: {} Actual value: {}", accumulator.getTaps(), tap);
        if (accumulator.getTaps() == null) {
            accumulator.setTaps(new TreeMap<>());
            accumulator.getTaps().put(tap.getTimestamp(), tap);
            return accumulator;
        }

        //Count distance for lower date
        final Map.Entry<Instant, ValidatedTap> floorTapEntry = accumulator.getTaps().floorEntry(tap.getTimestamp());
        final double floorSpeed = getSpeed(tap, floorTapEntry);
        log.debug("Floor speed {}", floorSpeed);

        //Count distance for higher date late taps
        final Map.Entry<Instant, ValidatedTap> ceilingTapEntry = accumulator.getTaps().ceilingEntry(tap.getTimestamp());
        final double ceilingSpeed = getSpeed(tap, ceilingTapEntry);
        log.debug("Ceiling speed {}", ceilingSpeed);

        accumulator.getTaps().put(tap.getTimestamp(), tap);

        log.debug("Calulating if tap is fraud floorSpeed: {}, fraudSpeed: {}, ceilingSpeed: {}", floorSpeed, fraudSpeed, ceilingSpeed);
        if (floorSpeed > fraudSpeed || ceilingSpeed > fraudSpeed) {
            log.debug("Tap: {} was detected as fraud", tap);
            accumulator.setFraudTap(tap);
        }

        return accumulator;
    }

    private TapAccumulator merger(String key, TapAccumulator previousSession, TapAccumulator streamAccumulator) {

        if (previousSession.getTaps() != null) {
            streamAccumulator.getTaps().putAll(previousSession.getTaps());
        }

        streamAccumulator.setFraudTap(null);
        return streamAccumulator;
    }

    public double getSpeed(ValidatedTap actual, Map.Entry<Instant, ValidatedTap> existed) {
        if (existed == null || existed.getValue() == null
            || existed.getValue().getGpsCoordinates() == null) {
            return 0;
        }
        final GpsCoordinates actualCoordinates = actual.getGpsCoordinates();
        final GpsCoordinates existedCoordinates = existed.getValue().getGpsCoordinates();
        final double distance = calculateDistanceInKmh(
                actualCoordinates.getLatitude(), actualCoordinates.getLongitude(),
                existedCoordinates.getLatitude(), existedCoordinates.getLongitude()
        );
        final long time = Math.abs(ChronoUnit.SECONDS.between(actual.getTimestamp(), existed.getKey()));
        if (time == 0) {
            log.warn("Zero time diff between actual {} and existed {}", actual, existed.getValue());
            return 0;
        }
        log.info("Distance: {} over time {}", distance, time);
        return Math.abs((distance / time) * 3600);
    }

    @Override
    public void run() {
        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        final Topology topology = buildPipeline(streamsBuilder);
        streams = new KafkaStreams(topology, kafkaConfig);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public void start() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(this, 0, TimeUnit.SECONDS);
    }

    public void stop() {
        streams.close();
        service.shutdown();
    }
}