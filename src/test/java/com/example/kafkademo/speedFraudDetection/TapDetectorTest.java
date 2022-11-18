package com.example.kafkademo.speedFraudDetection;

import com.example.kafkademo.speedFraudDetection.dto.GpsCoordinates;
import com.example.kafkademo.speedFraudDetection.dto.ValidatedTap;
import com.example.kafkademo.speedFraudDetection.kafka.SpeedFraudPipeline;
import com.example.kafkademo.speedFraudDetection.kafka.serdes.ObjectMapperDeserializer;
import com.example.kafkademo.speedFraudDetection.kafka.serdes.ObjectMapperSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class TapDetectorTest {

    public static final String INPUT_TOPIC = "validated-tap";
    public static final String OUTPUT_TOPIC = "fraud-tap";
    public static final Integer DURATION = 1;
    public static final Integer SPEED = 50;

    private final Properties kafkaConfig;
    private final ObjectMapper objectMapper;

    public TapDetectorTest() {
        kafkaConfig = new Properties();
        kafkaConfig.put(APPLICATION_ID_CONFIG, "kafka-unit-test");
        kafkaConfig.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        kafkaConfig.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
    }

    @Test
    void testSpeedFraudNormalOrder() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:25:10"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(1, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(0).value.getGpsCoordinates());
        }
    }

    @Test
    void testSpeedFraudLateTap() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:28:10"));
            inputTopic.pipeInput("bot1", makeTap(48.848148342526905, 17.996325061538116, "02:22:05"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(1, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.848148342526905, 17.996325061538116), values.get(0).value.getGpsCoordinates());
        }
    }

    @Test
    void testSpeedFraudMoreThanOne() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:21:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:21:30"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:26:10"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(2, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.849256823671936, 17.9937715526234), values.get(0).value.getGpsCoordinates());
            assertEquals(new GpsCoordinates(48.85030169697089, 18.018276214196636), values.get(1).value.getGpsCoordinates());
        }
    }

    @Test
    void testSpeedFraudMoreThanOneBotDifferentWindows() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:25:10"));

            inputTopic.pipeInput("bot2", makeTap("bot2", 48.85868813347197, 17.97660541523793, "05:20:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.849256823671936, 17.9937715526234, "05:22:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.85030169697089, 18.018276214196636, "05:25:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.87364042501854, 18.022769726251678, "05:25:10"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(2, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(0).value.getGpsCoordinates());
            assertEquals("bot1", values.get(0).key);
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(1).value.getGpsCoordinates());
            assertEquals("bot2", values.get(1).key);
        }
    }

    @Test
    void testSpeedFraudMoreThanOneBotSameWindows() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:25:10"));

            inputTopic.pipeInput("bot2", makeTap("bot2", 48.85868813347197, 17.97660541523793, "02:30:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.849256823671936, 17.9937715526234, "02:32:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.85030169697089, 18.018276214196636, "02:35:00"));
            inputTopic.pipeInput("bot2", makeTap("bot2", 48.87364042501854, 18.022769726251678, "02:35:10"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(2, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(0).value.getGpsCoordinates());
            assertEquals("bot1", values.get(0).key);
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(1).value.getGpsCoordinates());
            assertEquals("bot2", values.get(1).key);
        }
    }

    @Test
    void testSpeedFraudLateTapMiddleSession() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:28:10"));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "03:29:10"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "03:31:10"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "03:35:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "03:38:10"));

            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "03:28:05"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(1, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.85030169697089, 18.018276214196636), values.get(0).value.getGpsCoordinates());
        }
    }

    @Test
    void testSpeedFraudSameTimeTap() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        var detector = new SpeedFraudPipeline(DURATION, INPUT_TOPIC, OUTPUT_TOPIC, SPEED, new Properties(), objectMapper);
        final Topology topology = detector.buildPipeline(streamsBuilder);

        try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, kafkaConfig)) {
            TestInputTopic<String, ValidatedTap> inputTopic = topologyTestDriver
                    .createInputTopic(INPUT_TOPIC, new StringSerializer(), new ObjectMapperSerializer<>(objectMapper));

            TestOutputTopic<String, ValidatedTap> outputTopic = topologyTestDriver
                    .createOutputTopic(OUTPUT_TOPIC, new StringDeserializer(), new ObjectMapperDeserializer<>(ValidatedTap.class, objectMapper));

            inputTopic.pipeInput("bot1", makeTap(48.85868813347197, 17.97660541523793, "02:20:00"));
            inputTopic.pipeInput("bot1", makeTap(48.849256823671936, 17.9937715526234, "02:22:00"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:00"));
            inputTopic.pipeInput("bot1", makeTap(48.87364042501854, 18.022769726251678, "02:25:10"));
            inputTopic.pipeInput("bot1", makeTap(48.85030169697089, 18.018276214196636, "02:25:10"));

            List<KeyValue<String, ValidatedTap>> values = outputTopic.readKeyValuesToList();
            log.warn("Topic output {}", values);
            assertEquals(1, values.size());
            // reduce was called for 2nd event within the minute
            assertEquals(new GpsCoordinates(48.87364042501854, 18.022769726251678), values.get(0).value.getGpsCoordinates());
        }
    }

    private ValidatedTap makeTap(double latitude, double longitude, String time) {
        return makeTap("bot1", latitude, longitude, time);
    }

    private ValidatedTap makeTap(String bot, double latitude, double longitude, String time) {
        final GpsCoordinates coordinates = new GpsCoordinates(latitude, longitude);
        return new ValidatedTap(bot, coordinates, Instant.parse("2022-10-19T" + time + "Z"));
    }
}
