package com.example.kstreamdemo.serdeFactory;

import com.example.kstreamdemo.messages.Player;
import com.example.kstreamdemo.messages.ScoreEvent;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSerdesTests {

    @Test
    void retainsNumericInputAndRoundTripsAScoreEvent() {
        try (Serde<ScoreEvent> serde = JsonSerdes.scoreEventSerde()) {
            String topic = "score-events";
            ScoreEvent event = serde.deserializer().deserialize(topic,
                    utf8("{\"score\":9000.0,\"product_id\":6,\"player_id\":1}"));

            assertEquals("9000.0", event.getScore());
            assertEquals("6", event.getProductId());
            assertEquals("1", event.getPlayerId());
            assertEquals(event, serde.deserializer().deserialize(topic, serde.serializer().serialize(topic, event)));
        }
    }

    @Test
    void preservesKafkaTombstonesAndLegacyJsonNulls() {
        try (Serde<Player> serde = JsonSerdes.playerSerde()) {
            assertNull(serde.serializer().serialize("players", null));
            assertNull(serde.deserializer().deserialize("players", (byte[]) null));
            assertNull(serde.deserializer().deserialize("players", utf8("null")));
        }
    }

    @Test
    void rejectsMalformedInputWithoutIncludingRecordContentsInTheException() {
        try (Serde<Player> serde = JsonSerdes.playerSerde()) {
            Deserializer<Player> deserializer = serde.deserializer();
            SerializationException exception = assertThrows(SerializationException.class,
                    () -> deserializer.deserialize("players", utf8("{\"name\":\"sample-value\"")));

            assertEquals("Invalid JSON value for Player", exception.getMessage());
            assertNull(exception.getCause());
            assertThrows(SerializationException.class,
                    () -> deserializer.deserialize("players", utf8("{\"id\":\"1\"} {}")));
        }
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
