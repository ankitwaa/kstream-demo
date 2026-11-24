package com.example.kstreamdemo.serdeFactory;

import com.example.kstreamdemo.messages.Player;
import com.example.kstreamdemo.messages.Products;
import com.example.kstreamdemo.messages.ScoreDetails;
import com.example.kstreamdemo.messages.ScoreEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

public class JsonSerdes {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public static Serde<Player> playerSerde() {
        return jsonSerde(Player.class);
    }

    public static Serde<ScoreEvent> scoreEventSerde() {
        return jsonSerde(ScoreEvent.class);
    }

    public static Serde<Products> productsSerde() {
        return jsonSerde(Products.class);
    }

    public static Serde<ScoreDetails> scoreDetailsSerde() {
        return jsonSerde(ScoreDetails.class);
    }

    private static <T> Serde<T> jsonSerde(Class<T> type) {
        Serializer<T> serializer = (topic, data) -> {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                throw new SerializationException("Could not serialize " + type.getSimpleName());
            }
        };

        Deserializer<T> deserializer = (topic, data) -> {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.readValue(data, type);
            } catch (IOException ignored) {
                // Parser messages can include values from the record.
                throw new SerializationException("Invalid JSON value for " + type.getSimpleName());
            }
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }
}
