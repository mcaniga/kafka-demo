package com.example.kafkademo.speedFraudDetection.service;

import com.example.kafkademo.speedFraudDetection.dto.SFPipelineConfigurationRequest;
import com.example.kafkademo.speedFraudDetection.kafka.SpeedFraudPipeline;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.*;

@Slf4j
@Service
public class SFPipelineManagementService {
    private final Boolean autoStartup;
    private final Integer inactivityGap;
    private final Integer fraudSpeed;
    private final String inputTopic;
    private final String outputTopic;
    private final Properties kafkaConfig;
    private SpeedFraudPipeline speedFraudPipeline;
    private final ObjectMapper objectMapper;

    public SFPipelineManagementService(
       @Value("${speed-fraud-detection.inputTopic.topic}") String inputTopic,
       @Value("${speed-fraud-detection.outputTopic.topic}") String outputTopic,
       @Value("${spring.kafka.bootstrap-servers}") String kafkaUrl,
       @Value("${kafka.applicationId}") String applicationId,
       @Value("${speed-fraud-detection.autoStartup}") Boolean autoStartup,
       @Value("${speed-fraud-detection.inactivityGapInHours}") Integer inactivityGap,
       @Value("${speed-fraud-detection.fraudSpeedInKmh}") Integer fraudSpeed,
       ObjectMapper objectMapper
    ) {
        this.inputTopic = inputTopic;
        this.inactivityGap = inactivityGap;
        this.fraudSpeed = fraudSpeed;
        this.outputTopic = outputTopic;
        this.autoStartup = autoStartup;
        this.objectMapper = objectMapper;
        this.kafkaConfig = new Properties();
        kafkaConfig.put(APPLICATION_ID_CONFIG, applicationId);
        kafkaConfig.put(BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        kafkaConfig.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        kafkaConfig.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        kafkaConfig.put(DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        autoStartDetector();
    }

    public void restartDetector(SFPipelineConfigurationRequest sfPipelineConfigurationRequest) {
        stopDetector();
        startDetector(sfPipelineConfigurationRequest);
    }

    private void autoStartDetector() {
        log.debug("Autostartup: {}", autoStartup);
        if (autoStartup) {
            startDetector(new SFPipelineConfigurationRequest(
                inactivityGap,
                fraudSpeed
            ));
        }
    }

    private void startDetector(SFPipelineConfigurationRequest sfPipelineConfigurationRequest) {
        log.debug("Making SF pipeline from request: {}", sfPipelineConfigurationRequest);
        speedFraudPipeline = new SpeedFraudPipeline(
                sfPipelineConfigurationRequest.getInactivityGap(),
                inputTopic,
                outputTopic,
                sfPipelineConfigurationRequest.getFraudSpeed(),
                kafkaConfig,
                objectMapper
        );
        log.debug("Made SF pipeline: {}", speedFraudPipeline);
        speedFraudPipeline.start();
    }

    private void stopDetector() {
        log.debug("Stopping detector");
        speedFraudPipeline.stop();
        speedFraudPipeline = null;
    }
}
