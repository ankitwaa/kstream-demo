#!/bin/sh
kafka-topics --bootstrap-server kafka:9092 --create --topic score-events
sleep 10
kafka-topics --bootstrap-server kafka:9092 --create --topic mapped-score-events
sleep 10
kafka-topics --bootstrap-server kafka:9092 --create --topic non-mapped-score-events
sleep 10
kafka-topics --bootstrap-server kafka:9092 --create --topic players
sleep 10
kafka-topics --bootstrap-server kafka:9092 --create --topic products
sleep 10

kafka-console-producer --bootstrap-server kafka:9092 --topic players --property 'parse.key=true' --property 'key.separator=|' < players.json
sleep 10

kafka-console-producer --bootstrap-server kafka:9092 --topic products --property 'parse.key=true' --property 'key.separator=|' < products.json
sleep 10

kafka-console-producer --bootstrap-server kafka:9092 --topic score-events < score-events.json
sleep 10
