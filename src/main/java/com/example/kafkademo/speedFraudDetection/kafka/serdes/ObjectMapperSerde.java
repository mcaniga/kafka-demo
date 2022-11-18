package com.example.kafkademo.speedFraudDetection.kafka.serdes;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * A {@link Serde} that (de-)serializes JSON using Jackson's ObjectMapper.
 * Source: https://github.com/quarkusio/quarkus/blob/main/extensions/kafka-client/runtime/src/main/java/io/quarkus/kafka/client/serialization/ObjectMapperSerde.java
 */
public class ObjectMapperSerde<T> implements Serde<T> {

    private final ObjectMapperSerializer<T> serializer;
    private final ObjectMapperDeserializer<T> deserializer;

    public ObjectMapperSerde(Class<T> type) {
        this(type, new ObjectMapper());
    }

    public ObjectMapperSerde(Class<T> type, ObjectMapper objectMapper) {
        serializer = new ObjectMapperSerializer<T>(objectMapper);
        deserializer = new ObjectMapperDeserializer<T>(type, objectMapper);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return deserializer;
    }
}
