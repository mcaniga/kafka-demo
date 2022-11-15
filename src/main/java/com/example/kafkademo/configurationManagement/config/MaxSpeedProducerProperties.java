package com.example.kafkademo.configurationManagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "configuration-management.max-speed-producer")
@Data
public class MaxSpeedProducerProperties {
    private Integer partitions;
    private Integer replicas;
    private String topic;
}
