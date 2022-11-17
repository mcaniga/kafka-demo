package com.example.kafkademo.configurationManagement.kafka;

import com.example.kafkademo.configurationManagement.config.MaxSpeedProducerProperties;
import com.example.kafkademo.common.dto.MaxSpeedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MaxSpeedProducer {
    private final String topic;
    private final KafkaTemplate<String, MaxSpeedResponse> template;

    public MaxSpeedProducer(
            MaxSpeedProducerProperties properties,
            KafkaTemplate<String, MaxSpeedResponse> template
    ) {
        this.topic = properties.getTopic();
        this.template = template;
    }

    public void send(MaxSpeedResponse maxSpeedResponse) {
        log.debug("sending payload='{}' to topic='{}'", maxSpeedResponse, topic);
        // TODO: specify key to support concurrency with group-id
        template.send(topic, maxSpeedResponse);
    }
}
