# Instea TechTalk: Kafka
Simple demo consisting of two (simulated) microservices. Application illustrates usage of Kafka with Spring Boot.

## Microservices
NOTE: whole project is physically a one application for simplicity, but can be divided by packages to standalone microservices
- Configuration Management
  - TBD
- Speed Validation
  - TBD

## Technologies
- Java: 11
- Spring Boot: 2.7.5

## Kafka UI
- view contents of kafka via simple UI
- TBD

## Swagger URL
- TBD

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