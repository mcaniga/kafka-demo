package com.example.kafkademo.speedFraudDetection.kafka;

import com.example.kafkademo.speedFraudDetection.config.SFInputTopicProperties;
import com.example.kafkademo.speedFraudDetection.config.SFOutputTopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@Configuration
public class SFTopicConfiguration {
    private final SFInputTopicProperties sfInputTopicProperties;
    private final SFOutputTopicProperties sfOutputTopicProperties;

    public SFTopicConfiguration(
        SFInputTopicProperties sfInputTopicProperties,
        SFOutputTopicProperties sfOutputTopicProperties
    ) {
        log.info(
                "SFTopicConfiguration constructed with sfInputTopicProperties: {} and sfOutputTopicProperties: {}",
                sfInputTopicProperties,
                sfOutputTopicProperties

        );
        this.sfInputTopicProperties = sfInputTopicProperties;
        this.sfOutputTopicProperties = sfOutputTopicProperties;
    }

    @Bean
    @ConditionalOnProperty(value = "speed-fraud-detection.inputTopic.autoStartup", havingValue = "true", matchIfMissing = false)
    public NewTopic validatedTapTopic() {
        log.debug("Creating validatedTapTopic bean");
        return TopicBuilder.name(sfInputTopicProperties.getTopic())
                .partitions(sfInputTopicProperties.getPartitions())
                .replicas(sfInputTopicProperties.getReplicas())
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "speed-fraud-detection.outputTopic.autoStartup", havingValue = "true", matchIfMissing = false)
    public NewTopic speedFraudTopic() {
        log.debug("Creating speedFraudTopic bean");
        return TopicBuilder.name(sfOutputTopicProperties.getTopic())
                .partitions(sfOutputTopicProperties.getPartitions())
                .replicas(sfOutputTopicProperties.getReplicas())
                .build();
    }
}
