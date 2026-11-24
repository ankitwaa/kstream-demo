# My Kafka Streams demo App

Build and run the application with a fully patched JDK 11. The Compose stack
runs a single Confluent Platform 8.4.1 Kafka broker in KRaft mode for local
development. It uses plaintext and publishes port `29092` on IPv4 loopback. Use Docker Engine 28 or later for
loopback isolation. The broker limits decompressed record bodies to 1 MiB.

The Compose configuration starts a fresh Kafka cluster. Back up existing data
and plan a ZooKeeper-to-KRaft migration before moving a broker whose data you
need to retain. See the [Kafka upgrade guide](https://kafka.apache.org/44/getting-started/upgrade/).

From the repository root, use the same Compose project name as the old stack
(reuse `-p` or `COMPOSE_PROJECT_NAME` if you set it). After any required backup
or migration, start the broker and remove services left by the old Compose file.
This removes the old ZooKeeper and topic-creation containers for that project.

```sh
docker compose up -d --remove-orphans
docker compose ps -a
docker ps --format '{{.Names}} {{.Ports}}'
docker compose port kafka 29092
```

Confirm that the Compose project lists only Kafka, that no running container
lists a host mapping with `:32181->`, and that the Kafka port command prints
`127.0.0.1:29092`.
To also check for a host listener outside Docker, run
`lsof -nP -iTCP:32181 -sTCP:LISTEN` on the Docker host. It should produce no
listener output. Investigate any remaining listener before continuing.

Once Kafka is ready, load the sample data with
`docker compose exec kafka sh ./run.sh`.

Start the application with `sh ./gradlew bootRun`. To send additional sample
events, open a shell in the broker container with `docker compose exec kafka sh`
and run these commands.

```sh
kafka-console-producer --bootstrap-server kafka:9092 --topic score-events < new-score-events.json
kafka-console-producer --bootstrap-server kafka:9092 --topic players --property 'parse.key=true' --property 'key.separator=|' < new-players.json
```

## Security review

The review covered the Java entry point, stream topologies and JSON serdes,
packaged runtime dependencies, Compose, the Gradle wrapper configuration,
and the sample-data scripts. The topology now skips malformed JSON and
filters incomplete score events before routing. The serdes use fixed target
types and omit parser details from exception messages. Event logs omit
record contents.

- **Kafka Streams and clients** — The upgrade from 3.0.0 to 4.4.0 includes
  the fix for CVE-2026-111551, which can exhaust memory in affected Streams
  deserialization paths. The packaged codecs include LZ4 1.11.4 (fixes
  native library replacement from a shared temporary directory,
  CVE-2026-106451) and Snappy 1.1.10.9 (fixes unsafe range checks that can
  crash the JVM, CVE-2026-116590). The demo uses Streams and does not run
  Kafka Connect. [Apache Kafka advisories](https://kafka.apache.org/community/cve-list/)
  [LZ4 release](https://github.com/yawkat/lz4-java/releases/tag/v1.11.4)
  [Snappy advisory](https://github.com/xerial/snappy-java/security/advisories/GHSA-xqrx-qh46-34m7)
- **Jackson** — The BOM resolves core and databind to 2.21.8. This includes
  the databind fix for CVE-2026-110879, which affects applications accepting
  class-name type IDs through polymorphic validation. These serdes use
  concrete target types with default typing disabled.
  [Jackson advisory](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph)
- **Compose images** — The former Confluent 6.0.0 broker and ZooKeeper images
  are removed, eliminating the ZooKeeper service and its host port from
  the Compose configuration. The replacement broker tag `8.4.1-2-ubi9` includes Confluent's
  fix for the Snappy broker denial of service (CONFSA-2026-32) and the
  patched amd64 JDK for CVE-2026-114938 (CONFSA-2026-37). Confluent identifies
  its arm64 images as unaffected by that JDK issue. The Compose broker
  also sets a 1 MiB decompression cap for Kafka CVE-2026-114349. Local
  processes and containers on the Compose network can still reach this
  plaintext broker. [Confluent 8.4.1 security release notes](https://support.confluent.io/hc/en-us/articles/55284741804308-Security-Release-Notes-for-CP-8-4-1-Out-of-band-Critical-Patch-Release)
  [Confluent JDK advisory](https://support.confluent.io/hc/en-us/articles/55810725278740-CONFSA-2026-37-CVE-2026-114938-Confluent-Cloud-and-Confluent-Platform-OpenJDK-vulnerability-Java4Shell)
  [Apache Kafka advisories](https://kafka.apache.org/community/cve-list/)
- **Remaining dependency risk** — Spring Boot 2.6.0 is past open-source
  support. Its Spring Framework 5.3.13 version falls in the
  CVE-2022-22965 range. The packaged app has no Spring MVC, WebFlux,
  or servlet container. It contains the Log4j API and SLF4J bridge
  without the Log4j Core component affected by CVE-2021-44228.
  Logback 1.2.7 falls in the CVE-2021-42550 range, which requires write
  access to logging configuration and a reload. SnakeYAML 1.29 remains
  a configuration dependency. Kafka payloads go through Jackson and
  the repository supplies properties configuration. Keep external
  logging and application configuration trusted and plan a migration
  to a supported Spring Boot release with the corresponding JDK and
  Gradle upgrades. [Spring Boot support](https://spring.io/blog/2022/11/24/spring-boot-2-6-14-available-now/)
  [Spring advisory](https://spring.io/security/cve-2022-22965/)
  [Apache Log4j advisories](https://logging.apache.org/security.html)
  [Logback advisory](https://logback.qos.ch/news-archive.html)
  [Spring Boot SnakeYAML assessment](https://github.com/spring-projects/spring-boot/issues/33457)

The Gradle build, five tests, runtime dependency resolution, and packaged
JAR inventory were checked on Corretto 11. Static inspection confirmed that
Compose defines only Kafka and publishes only `127.0.0.1:29092`. Docker
Hub returned Linux amd64 and arm64 manifests for the configured tag. The
advisory review was manual. A full dependency scan and an image package
scan remain pending. Live image startup, orphan removal and port
verification, and a broker-backed smoke test require a Docker-enabled host.
