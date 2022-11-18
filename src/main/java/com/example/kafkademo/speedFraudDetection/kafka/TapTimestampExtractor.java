package com.example.kafkademo.speedFraudDetection.kafka;

import com.example.kafkademo.speedFraudDetection.dto.ValidatedTap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class TapTimestampExtractor implements TimestampExtractor {
    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long previousTimestamp) {
        var value = consumerRecord.value();
        if (value instanceof ValidatedTap) {
            ValidatedTap tap = (ValidatedTap) value;
            return tap.getTimestamp().toEpochMilli();
        }
        return -1;
    }
}
