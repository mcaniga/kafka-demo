package com.example.kafkademo.speedValidation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "speed-validation.max-speed-listener")
@Data
public class MaxSpeedListenerProperties {
    private String topic;
}
