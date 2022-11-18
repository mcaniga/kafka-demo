package com.example.kafkademo.speedValidation.kafka;

import com.example.kafkademo.common.dto.MaxSpeedResponse;
import com.example.kafkademo.common.dto.MaxSpeedUpdateEvent;
import com.example.kafkademo.configurationManagement.config.MaxSpeedProducerProperties;
import com.example.kafkademo.speedValidation.service.SpeedValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MaxSpeedListener {
    private final String topic;
    private final KafkaTemplate<String, MaxSpeedUpdateEvent> template;
    private final SpeedValidationService speedValidationService;

    public MaxSpeedListener(
            MaxSpeedProducerProperties properties,
            KafkaTemplate<String, MaxSpeedUpdateEvent> template,
            SpeedValidationService speedValidationService
    ) {
        this.topic = properties.getTopic();
        this.template = template;
        this.speedValidationService = speedValidationService;
    }

    @KafkaListener(
            id = "maxSpeedListener",
            topics = "${speed-validation.max-speed-listener.topic}",
            groupId = "${speed-validation.max-speed-listener.groupId}",
            autoStartup = "${speed-validation.max-speed-listener.autoStartup}"
    )
    public void updateMaxSpeed(ConsumerRecord<?, MaxSpeedUpdateEvent> consumerRecord) {
        log.debug("Processing updateMaxSpeed kafka message: {}", consumerRecord);
        MaxSpeedUpdateEvent maxSpeedUpdateEvent = consumerRecord.value();
        int busId = maxSpeedUpdateEvent.getBusId();
        speedValidationService.cacheMaxSpeed(busId);
    }
}
