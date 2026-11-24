# My Kafka Streams demo App

Build and run the application with a fully patched JDK 11. The Compose stack
runs a single Confluent Platform 8.4.1 Kafka broker in KRaft mode for local
development. It uses plaintext and publishes port `29092` on IPv4 loopback. Use Docker Engine 28 or later for
loopback isolation. The broker limits decompressed record bodies to 1 MiB.

The Compose configuration starts a fresh Kafka cluster. Back up existing data
and plan a ZooKeeper-to-KRaft migration before moving a broker whose data you
need to retain. See the [Kafka upgrade guide](https://kafka.apache.org/44/getting-started/upgrade/).

From the repository root, start the broker and load the sample data once it is
ready.

```sh
docker compose up -d
docker compose exec kafka sh ./run.sh
```

Start the application with `sh ./gradlew bootRun`. To send additional sample
events, open a shell in the broker container with `docker compose exec kafka sh`
and run these commands.

```sh
kafka-console-producer --bootstrap-server kafka:9092 --topic score-events < new-score-events.json
kafka-console-producer --bootstrap-server kafka:9092 --topic players --property 'parse.key=true' --property 'key.separator=|' < new-players.json
```
