package com.example.kstreamdemo;

import com.example.kstreamdemo.helper.TopologyBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KstreamDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KstreamDemoApplication.class, args);
		System.out.println("lag gaye....");

		KafkaStreams kafkaStreams = TopologyBuilder.buildMainTopology();
		kafkaStreams.start();

		KafkaStreams mappedStreams = TopologyBuilder.mappedReadTopology();
		mappedStreams.start();

        KafkaStreams nonMappedStreams = TopologyBuilder.nonMappedReadTopology();
        nonMappedStreams.start();

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
