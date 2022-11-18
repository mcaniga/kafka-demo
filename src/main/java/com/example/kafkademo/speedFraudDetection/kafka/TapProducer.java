package com.example.kafkademo.speedFraudDetection.kafka;

import com.example.kafkademo.configurationManagement.config.MaxSpeedProducerProperties;
import com.example.kafkademo.speedFraudDetection.dto.ValidatedTap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TapProducer {
    private final String topic;
    private final KafkaTemplate<String, ValidatedTap> template;

    public TapProducer(
            MaxSpeedProducerProperties properties,
            KafkaTemplate<String, ValidatedTap> template
    ) {
        this.topic = properties.getTopic();
        this.template = template;
    }

    public void send(ValidatedTap validatedTap) {
        log.debug("sending payload='{}' to topic='{}'", validatedTap, topic);
        template.send(topic, validatedTap.getMediumId(), validatedTap);
    }
}