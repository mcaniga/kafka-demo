package com.example.kafkademo.configurationManagement.kafka;

import com.example.kafkademo.common.dto.MaxSpeedUpdateEvent;
import com.example.kafkademo.configurationManagement.config.MaxSpeedProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MaxSpeedProducer {
    private final String topic;
    private final KafkaTemplate<String, MaxSpeedUpdateEvent> template;

    public MaxSpeedProducer(
            MaxSpeedProducerProperties properties,
            KafkaTemplate<String, MaxSpeedUpdateEvent> template
    ) {
        this.topic = properties.getTopic();
        this.template = template;
    }

    public void send(int busId) {
        MaxSpeedUpdateEvent maxSpeedUpdateEvent = new MaxSpeedUpdateEvent(busId);
        log.debug("sending payload='{}' to topic='{}'", maxSpeedUpdateEvent, topic);
        template.send(topic, String.valueOf(busId), maxSpeedUpdateEvent);
    }
}
