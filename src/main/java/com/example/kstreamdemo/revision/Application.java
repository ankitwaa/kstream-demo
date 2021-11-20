package com.example.kstreamdemo.revision;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;


public class Application {

    public static void main(String[] args) {
        System.out.println("lag gaye....");

        KafkaStreams kafkaStreams = TopologyBuilder.buildMainTopology();
        kafkaStreams.start();

        KafkaStreams mappedStreams = TopologyBuilder.mappedReadTopology();
        mappedStreams.start();

        /*KafkaStreams nonMappedStreams = TopologyBuilder.nonMappedReadTopology();
        nonMappedStreams.start();*/

        KafkaStreams nonMappedRouteStreams = TopologyBuilder.nonMappedRoutedTopology();
        nonMappedRouteStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            kafkaStreams.close();
            mappedStreams.close();
            //nonMappedStreams.close();
            nonMappedRouteStreams.close();
        }));
        System.out.println("Describe-------");
    }
}
