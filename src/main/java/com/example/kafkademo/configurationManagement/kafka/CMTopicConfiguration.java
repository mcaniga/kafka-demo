package com.example.kafkademo.configurationManagement.kafka;

import com.example.kafkademo.configurationManagement.config.MaxSpeedProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@Configuration
public class CMTopicConfiguration {
    private final MaxSpeedProducerProperties maxSpeedProducerProperties;

    public CMTopicConfiguration(
        MaxSpeedProducerProperties maxSpeedProducerProperties
    ) {
        log.info(
                "TopicConfiguration constructed with maxSpeedProducerProperties: {}",
                maxSpeedProducerProperties
        );
        this.maxSpeedProducerProperties = maxSpeedProducerProperties;
    }

    @Bean
    @ConditionalOnProperty(value = "configuration-management.max-speed-producer.autoStartup", havingValue = "true", matchIfMissing = false)
    public NewTopic maxSpeedTopic() {
        log.debug("Creating maxSpeedTopic bean");
        return TopicBuilder.name(maxSpeedProducerProperties.getTopic())
                .partitions(maxSpeedProducerProperties.getPartitions())
                .replicas(maxSpeedProducerProperties.getReplicas())
                .build();
    }
}
