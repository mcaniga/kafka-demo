package com.example.kafkademo.speedFraudDetection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "speed-fraud-detection.input-topic")
@Data
public class SFInputTopicProperties {
    private Integer partitions;
    private Integer replicas;
    private String topic;
}
