#!/bin/sh
kafka-topics --bootstrap-server kafka:9092 --create --topic score-events
kafka-topics --bootstrap-server kafka:9092 --create --topic mapped-score-events
kafka-topics --bootstrap-server kafka:9092 --create --topic non-mapped-score-events
kafka-topics --bootstrap-server kafka:9092 --create --topic players
kafka-topics --bootstrap-server kafka:9092 --create --topic products

kafka-console-producer --bootstrap-server kafka:9092 --topic players --property 'parse.key=true' --property 'key.separator=|' < players.json

kafka-console-producer --bootstrap-server kafka:9092 --topic products --property 'parse.key=true' --property 'key.separator=|' < products.json

kafka-console-producer --bootstrap-server kafka:9092 --topic score-events < score-events.json
