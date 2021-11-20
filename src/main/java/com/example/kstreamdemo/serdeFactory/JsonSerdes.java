package com.example.kstreamdemo.serdeFactory;

import com.example.kstreamdemo.messages.Player;
import com.example.kstreamdemo.messages.Products;
import com.example.kstreamdemo.messages.ScoreDetails;
import com.example.kstreamdemo.messages.ScoreEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

public class JsonSerdes {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static Serde<Player> playerSerde() {
        Serializer<Player> serializer = (topic, data) -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };

        Deserializer<Player> deserializer = (topic, data) -> {
            try {
                return objectMapper.readValue(data, Player.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<ScoreEvent> scoreEventSerde() {
        Serializer<ScoreEvent> serializer = (topic, data) -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };

        Deserializer<ScoreEvent> deserializer = (topic, data) -> {
            try {
                return objectMapper.readValue(data, ScoreEvent.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Products> productsSerde() {
        Serializer<Products> serializer = (topic, data) -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };

        Deserializer<Products> deserializer = (topic, data) -> {
            try {
                return objectMapper.readValue(data, Products.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<ScoreDetails> scoreDetailsSerde() {
        Serializer<ScoreDetails> serializer = (topic, data) -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        };

        Deserializer<ScoreDetails> deserializer = (topic, data) -> {
            try {
                return objectMapper.readValue(data, ScoreDetails.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        return Serdes.serdeFrom(serializer, deserializer);
    }

}
