# Instea TechTalk: Kafka
Simple demo consisting of three (simulated) microservices. Application illustrates usage of Kafka with Spring Boot.

## Local installation
- set IDE to use Java 11
- `docker-compose up -d`
  - launches Zookeeper on port `22181`, single-node Kafka on port `29092` and Kafka-UI on port `8085`

## Microservices
NOTE: whole project is physically a one application for simplicity, but can be divided by packages to standalone microservices
NOTE2: Kafka specific classes are in `kafka` packages in each microservice
- Configuration Management
  - stores maximum allowed speed
  - contains interface for modifying the maximum allowed speed, which fires event to `max-speed` topic
- Speed Validation
  - caches maximum allowed speed  
  - listens for maximum speed updates on `max-speed` topic
  - contains interface for validating the maximum allowed speed
- Speed Fraud Detection
    - contains interface for sending taps
    - listens to `validated-tap` topic and writes fradulent taps to `fraud-taps`
    - identification of fradulent taps is done via Kafka Streams

## Use-cases
## Changing Max Speed
- illustration of kafka consumer and producer
- advantage of Kafka in this use-case
   - possible to have multiple speed validators, management does need to know about them
   - multiple speed validators are handled by Kafka - using group id
![Alt text](docs/changeMaxSpeed.png?raw=true "Title")
## Validating Max Speed
- advantage of Kafka in this use-case
    - possible to send events in case of speed violation
![Alt text](docs/validateMaxSpeed.png?raw=true "Title")
## Speed Fraud Detection
- illustration of kafka streams
![Alt text](docs/speedFraudDetection.png?raw=true "Title")

## Technologies
- Java: 11
- Spring Boot: 2.7.5

## Kafka UI
- view contents of kafka via simple UI
- http://localhost:8085/

## Swagger URL
- http://localhost:8080/swagger-ui/index.html

## Topics
- `max-speed`
    - event content:
        - `busId`
            - specifies which bus was updated
            - eg. `10`
    - example event
```yaml
{
   "busId": 10
}
```

- `validated-tap`
  - event key: mediumId
  - event content:
    - `mediumId`
      - specifies identifier of the medium"
      - eg. `50`
    - `gpsCoordinates`
      - specifies coordinates with `latitude` and `longitude`
      - example bellow
    - `timestamp`
      - specifies timestamp of the tap in ISO-8601
      - eg. `"2022-01-10T15:23:44Z"`
  - example event
```yaml
{
  "mediumId": 50,
  "gpsCoordinates": {
    "latitude": 50.42,
    "longitude": 60.13
  },
  "timestamp": "2022-01-10T15:23:44Z"
}
```

- `fraud-tap`
  - event key: mediumId  
  - event content:
    - `mediumId`
      - specifies identifier of the medium"
      - eg. `50`
    - `gpsCoordinates`
      - specifies coordinates with `latitude` and `longitude`
      - example bellow
    - `timestamp`
      - specifies timestamp of the tap in ISO-8601
      - eg. `"2022-01-10T15:23:44Z"`
  - example event
```yaml
{
   "mediumId": 50,
   "gpsCoordinates": {
    "latitude": 50.42,
    "longitude": 60.13
   },
   "timestamp": "2022-01-10T15:23:44Z"
}
```