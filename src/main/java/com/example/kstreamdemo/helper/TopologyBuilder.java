package com.example.kstreamdemo.helper;

import com.example.kstreamdemo.messages.Player;
import com.example.kstreamdemo.messages.Products;
import com.example.kstreamdemo.messages.ScoreDetails;
import com.example.kstreamdemo.messages.ScoreEvent;
import com.example.kstreamdemo.serdeFactory.JsonSerdes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TopologyBuilder {

    private static StreamsConfig streamsConfig(String applicationIdPrefix) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationIdPrefix + UUID.randomUUID());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class);

        return new StreamsConfig(properties);
    }

    public static KafkaStreams buildMainTopology() {
        return new KafkaStreams(mainTopology(), streamsConfig("consumer_group_"));
    }

    static Topology mainTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, ScoreEvent> scoreStreams = streamsBuilder.stream("score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreEventSerde()))
                .filter((key, event) -> event != null && event.getPlayerId() != null)
                .map((key, event) -> KeyValue.pair(event.getPlayerId(), event));

        KTable<String, Player> kTableProduct = streamsBuilder.table("players", Consumed.with(Serdes.String(), JsonSerdes.playerSerde()));
        GlobalKTable<String, Products> globalKTableProduct = streamsBuilder.globalTable("products", Consumed.with(Serdes.String(), JsonSerdes.productsSerde()));

        KStream<String, ScoreDetails> joinScoreDetails =  scoreStreams.leftJoin(kTableProduct, (lv, rv) -> {
           // System.out.println("Mapping............lv:="+ lv + ", rv=" + rv);
            ScoreDetails scoreDetail = new ScoreDetails();
            scoreDetail.setScoreEvent(lv);
            scoreDetail.setPlayer(rv);
            return scoreDetail;
        }, Joined.with(Serdes.String(), JsonSerdes.scoreEventSerde(), JsonSerdes.playerSerde()));

        // joinScoreDetails.foreach((k,v) -> System.out.println("Joinded....player="+ v.getPlayer() + ", score=" + v.getScoreEvent()));
        // scoreStreams.foreach((k,v) -> System.out.println("Score Event....key"+ k + ", value=" + v));

        joinScoreDetails.split()
                .branch((key, details) -> details.getPlayer() == null,
                        Branched.withConsumer(stream -> stream.to("non-mapped-score-events",
                                Produced.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))))
                .defaultBranch(Branched.withConsumer(stream -> stream.to("mapped-score-events",
                        Produced.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))));

        return streamsBuilder.build();
    }

    public static KafkaStreams mappedReadTopology(){
        StreamsConfig config = streamsConfig("consumer_group_m_1");
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream("mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))
                .filter((key, details) -> details != null && details.getScoreEvent() != null)
                .foreach((key, details) -> System.out.println("Mapped score event received"));
        return new KafkaStreams(streamsBuilder.build(), config);
    }

    public static KafkaStreams nonMappedReadTopology(){
        StreamsConfig config = streamsConfig("consumer_group_nm_1");
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream("non-mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))
                .filter((key, details) -> details != null && details.getScoreEvent() != null)
                .foreach((key, details) -> System.out.println("NonMapped score event received"));
        return new KafkaStreams(streamsBuilder.build(), config);
    }

    public static KafkaStreams nonMappedRoutedTopology(){
        StreamsConfig config = streamsConfig("consumer_group_nmr_1");
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Player> playersStream = streamsBuilder.stream("players", Consumed.with(Serdes.String(), JsonSerdes.playerSerde()));

        KStream<String, ScoreEvent> nonMappedStream = streamsBuilder.stream("non-mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))
                .filter((key, details) -> details != null && details.getScoreEvent() != null
                        && details.getScoreEvent().getPlayerId() != null)
                .map((key, details) -> KeyValue.pair(details.getScoreEvent().getPlayerId(), details.getScoreEvent()));

        playersStream.join(nonMappedStream, (lv, rv) -> {
            ScoreDetails scoreDetails = new ScoreDetails();
            scoreDetails.setScoreEvent(rv);
            scoreDetails.setPlayer(lv);
            return scoreDetails;
        }, JoinWindows.of(Duration.ofMinutes(5)), StreamJoined.with(Serdes.String(), JsonSerdes.playerSerde(), JsonSerdes.scoreEventSerde()))
                .to("mapped-score-events", Produced.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()));

        /*streamsBuilder.stream("non-mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))
                .map((k,v) -> KeyValue.pair((String)null, v.getScoreEvent()))
                    .to("score-events", Produced.with(Serdes.String(), JsonSerdes.scoreEventSerde()));*/
        return new KafkaStreams(streamsBuilder.build(), config);
    }
}
