package com.example.kstreamdemo.helper;

import com.example.kstreamdemo.messages.ScoreDetails;
import com.example.kstreamdemo.serdeFactory.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologyBuilderTests {

    @Test
    void skipsInvalidScoreRecordsAndRoutesSubsequentScores() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "score-routing-test");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class);

        try (TopologyTestDriver driver = new TopologyTestDriver(TopologyBuilder.mainTopology(), properties);
             Serdes.StringSerde strings = new Serdes.StringSerde();
             var details = JsonSerdes.scoreDetailsSerde()) {
            TestInputTopic<String, String> players = driver.createInputTopic(
                    "players", strings.serializer(), strings.serializer());
            TestInputTopic<String, String> scores = driver.createInputTopic(
                    "score-events", strings.serializer(), strings.serializer());
            TestOutputTopic<String, ScoreDetails> mapped = driver.createOutputTopic(
                    "mapped-score-events", strings.deserializer(), details.deserializer());
            TestOutputTopic<String, ScoreDetails> unmapped = driver.createOutputTopic(
                    "non-mapped-score-events", strings.deserializer(), details.deserializer());

            players.pipeInput("1", "{\"id\":\"1\",\"name\":\"Sample Player\"}");
            players.pipeInput("1", "{");
            scores.pipeInput((String) null);
            scores.pipeInput("{");
            scores.pipeInput("{\"score\":100}");
            assertTrue(mapped.isEmpty());
            assertTrue(unmapped.isEmpty());

            scores.pipeInput("{\"score\":100,\"player_id\":1}");
            scores.pipeInput("{\"score\":200,\"player_id\":2}");

            assertEquals("Sample Player", mapped.readValue().getPlayer().getName());
            ScoreDetails unmatched = unmapped.readValue();
            assertEquals("2", unmatched.getScoreEvent().getPlayerId());
            assertNull(unmatched.getPlayer());

            players.pipeInput("1", (String) null);
            scores.pipeInput("{\"score\":300,\"player_id\":1}");

            assertTrue(mapped.isEmpty());
            assertEquals("1", unmapped.readKeyValue().key);
        }
    }
}
