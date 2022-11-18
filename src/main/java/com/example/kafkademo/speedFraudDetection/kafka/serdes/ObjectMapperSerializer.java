package com.example.kafkademo.speedFraudDetection.kafka.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class ObjectMapperSerializer<T> implements Serializer<T> {
    public static final String NULL_AS_NULL_CONFIG = "json.serialize.null-as-null";

    private final ObjectMapper objectMapper;

    private boolean nullAsNull = false;

    public ObjectMapperSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (configs.containsKey(NULL_AS_NULL_CONFIG) && Boolean.parseBoolean((String) configs.get(NULL_AS_NULL_CONFIG))) {
            nullAsNull = true;
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (nullAsNull && data == null) {
            return null;
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            objectMapper.writeValue(output, data);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }
}