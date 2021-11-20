# My Kafka Streams demo App

```sh
# Go to the root folder of application
cd ..../kstream-demo

# Down the application
C:/../kstream-demo> docker-compose down

# Kill all containers 
C:/../kstream-demo> docker container prune

# Start all containers
C:/../kstream-demo> docker-compose up -d

# log into the broker, which is where the kafka console scripts live
C:/../kstream-demo> docker-compose exec kafka bash

# produce test data to topics
$ /run.sh

# produce new score-events
$ kafka-console-producer --bootstrap-server kafka:9092 --topic score-events < new-score-events.json

#produce new players
$ kafka-console-producer --bootstrap-server kafka:9092 --topic players --property 'parse.key=true' --property 'key.separator=|' < new-players.json
```