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

    /**
     * A Topology allows you to construct an acyclic graph of processing nodes (source, processor, sink), and then passed
     * into a new KafkaStream instance that will then begin consuming, processing, and producing records.
     */
    public Topology buildPipeline(StreamsBuilder streamsBuilder) {
        final var tapSerde = new ObjectMapperSerde<>(ValidatedTap.class, objectMapper);
        final var accumulatorSerde = new ObjectMapperSerde<>(TapAccumulator.class, objectMapper);
        final Duration windowSize = Duration.ofHours(inactivityGapInHours);
        final SessionWindows sessionWindows = SessionWindows.ofInactivityGapWithNoGrace(windowSize);

        streamsBuilder
                .stream(inputTopic, Consumed.with(STRING_SERDE, tapSerde)
                        .withTimestampExtractor(tapTimestampExtractor)) // listener
                .selectKey((k, v) -> v.getMediumId())
                .groupByKey() // group taps by mediumId
                .windowedBy(sessionWindows)
                .aggregate(TapAccumulator::new, // initial value of accumulator
                        this::fraudTaps, // accumulator step
                        this::merger, // merge function, used when windows are overlapping - late taps
                        Materialized.with(STRING_SERDE, accumulatorSerde) // kafka store, caching mechanism for statefull operations , can RocksDB, hashmap, ...
                ) // for each window compute agregate function
                .toStream()
                .peek((k, v) -> log.debug("Aggregated taps - mediumId: {}, tap acumulator: {}", k.key(), v))
                .filter((k, v) -> v != null && v.getFraudTap() != null) // filter non-fraud taps
                .map((key, value) -> KeyValue.pair(key.key(), value.getFraudTap())) // map to <String, ValidatedTap> keypair
                // need to map Windowed to string (or provide serde)
                .to(outputTopic, Produced.with(STRING_SERDE, tapSerde)); // producer

        return streamsBuilder.build();
    }

    private TapAccumulator fraudTaps(String key, ValidatedTap tap, TapAccumulator accumulator) {

        log.info("Previous taps: {} Actual value: {}", accumulator.getTaps(), tap);
        if (accumulator.getTaps() == null) {
            accumulator.setTaps(new TreeMap<>());
            accumulator.getTaps().put(tap.getTimestamp(), tap);
            return accumulator;
        }

        // Count speed between current tap and his predecessor
        final Map.Entry<Instant, ValidatedTap> floorTapEntry = accumulator.getTaps().floorEntry(tap.getTimestamp());
        final double floorSpeed = getSpeed(tap, floorTapEntry);
        log.debug("Floor speed {}", floorSpeed);

        // Count speed between current tap and his successor
        final Map.Entry<Instant, ValidatedTap> ceilingTapEntry = accumulator.getTaps().ceilingEntry(tap.getTimestamp());
        final double ceilingSpeed = getSpeed(tap, ceilingTapEntry);
        log.debug("Ceiling speed {}", ceilingSpeed);

        // put to proccesed taps
        accumulator.getTaps().put(tap.getTimestamp(), tap);

        log.debug("Calulating if tap is fraud floorSpeed: {}, fraudSpeed: {}, ceilingSpeed: {}", floorSpeed, fraudSpeed, ceilingSpeed);
        // determines if speed between current tap and his predecessor or current tap and his successor his higher than allowed
        if (floorSpeed > fraudSpeed || ceilingSpeed > fraudSpeed) {
            log.debug("Tap: {} was detected as fraud", tap);
            accumulator.setFraudTap(tap);
        }

        return accumulator;
    }

    private TapAccumulator merger(String key, TapAccumulator previousSession, TapAccumulator streamAccumulator) {

        if (previousSession.getTaps() != null) {
            // merge windows, put taps from previous session into processed taps
            streamAccumulator.getTaps().putAll(previousSession.getTaps());
        }

        // because late tap arrived, we must find again the fraud tap (what was initially fraud, now maybe is not because of new tap)
        streamAccumulator.setFraudTap(null);
        return streamAccumulator;
    }

    public double getSpeed(ValidatedTap actual, Map.Entry<Instant, ValidatedTap> existed) {
        // existed = previous or successor
        if (existed == null || existed.getValue() == null
            || existed.getValue().getGpsCoordinates() == null) {
            return 0;
        }

        // calculate distance from coordinates
        final GpsCoordinates actualCoordinates = actual.getGpsCoordinates();
        final GpsCoordinates existedCoordinates = existed.getValue().getGpsCoordinates();
        final double distance = calculateDistanceInKmh(
                actualCoordinates.getLatitude(), actualCoordinates.getLongitude(),
                existedCoordinates.getLatitude(), existedCoordinates.getLongitude()
        );

        // calculate elapsed time
        final long time = Math.abs(ChronoUnit.SECONDS.between(actual.getTimestamp(), existed.getKey()));
        if (time == 0) {
            log.warn("Zero time diff between actual {} and existed {}", actual, existed.getValue());
            return 0;
        }

        // calculate speed
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