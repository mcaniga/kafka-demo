# Instea TechTalk: Kafka
Simple demo consisting of two (simulated) microservices. Application illustrates usage of Kafka with Spring Boot.

## Local installation#
- `docker-compose up -d`
- launches Zookeeper on port `22181`, single-node Kafka on port `29092` and Kafka-UI on port `8085`

## Microservices
NOTE: whole project is physically a one application for simplicity, but can be divided by packages to standalone microservices
- Configuration Management
  - stores maximum allowed speed
  - contains interface for modifying the maximum allowed speed, which fires event to `max-speed` topic
- Speed Validation
  - caches maximum allowed speed  
  - listens for maximum speed updates on `max-speed` topic
  - contains interface for validating the maximum allowed speed


## Use-cases
## Chanching Max Speed
- illustration of kafka consumer and producer
![Alt text](docs/changeMaxSpeed.png?raw=true "Title")

## Technologies
- Java: 11
- Spring Boot: 2.7.5

## Kafka UI
- view contents of kafka via simple UI
- TBD

## Swagger URL
- http://localhost:8080/swagger-ui/index.html

## Topics
- `max-speed`
    - event key: TBD
    - event content:
        - `maxSpeed`
            - specifies maximum bus speed in km/h
            - eg. `50`
    - example event
```yaml
{
   "maxSpeed": 50
}
```