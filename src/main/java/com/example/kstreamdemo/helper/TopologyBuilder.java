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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TopologyBuilder {

    public static KafkaStreams buildMainTopology(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer_group_" + UUID.randomUUID());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsConfig config = new StreamsConfig(properties);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, ScoreEvent> scoreStreams = streamsBuilder.stream("score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreEventSerde())).
                map((k,v) -> KeyValue.pair(v.getPlayerId(),v));

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

        KStream<String, ScoreDetails>[] branches =   joinScoreDetails.branch(((key, value) -> value.getPlayer() ==null),((key, value) -> value.getPlayer() != null));

        // No value presents
        branches[0].to("non-mapped-score-events", Produced.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()));


        // Value is presents
        branches[1].to("mapped-score-events", Produced.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()));

        Topology topology = streamsBuilder.build();
        return new KafkaStreams(topology, config);
    }

    public static KafkaStreams mappedReadTopology(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer_group_m_1"+  UUID.randomUUID());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsConfig config = new StreamsConfig(properties);
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream("mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde())).foreach(
                (k,v) -> {
                    System.out.println("Mapped - Score=" + v.getScoreEvent() + ", Player=" + v.getPlayer());
                }
        );
        return new KafkaStreams(streamsBuilder.build(), config);
    }

    public static KafkaStreams nonMappedReadTopology(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer_group_nm_1" +  UUID.randomUUID());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        StreamsConfig config = new StreamsConfig(properties);
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream("non-mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde())).foreach(
                (k,v) -> {
                    System.out.println("NonMapped - Score=" + v.getScoreEvent() + ", Player=" + v.getPlayer());
                }
        );
        return new KafkaStreams(streamsBuilder.build(), config);
    }

    public static KafkaStreams nonMappedRoutedTopology(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "consumer_group_nmr_1" +  UUID.randomUUID());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        StreamsConfig config = new StreamsConfig(properties);
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Player> playersStream = streamsBuilder.stream("players", Consumed.with(Serdes.String(), JsonSerdes.playerSerde()));

        KStream<String, ScoreEvent> nonMappedStream = streamsBuilder.stream("non-mapped-score-events", Consumed.with(Serdes.String(), JsonSerdes.scoreDetailsSerde()))
                .map((k,v) -> KeyValue.pair(v.getScoreEvent().getPlayerId(), v.getScoreEvent()));

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
