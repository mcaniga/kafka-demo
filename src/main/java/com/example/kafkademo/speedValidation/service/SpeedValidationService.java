package com.example.kafkademo.speedValidation.service;

import com.example.kafkademo.speedValidation.dto.SpeedValidationRequest;
import com.example.kafkademo.speedValidation.dto.SpeedValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpeedValidationService {
    private int maxSpeed;

    public SpeedValidationService() {
        // ideally, max speed should be fetched from management on startup, but this is enough for illustration purposes :))
        this.maxSpeed = 50;
    }

    public SpeedValidationResponse validate(SpeedValidationRequest speedValidationRequest) {
        log.debug("Validating speed: {}", speedValidationRequest);
        boolean isMaxSpeedViolated = isMaxSpeedViolation(speedValidationRequest);
        SpeedValidationResponse response = new SpeedValidationResponse();
        if (isMaxSpeedViolated) {
            log.debug("Max speed was violated");
            // here we can send response eg. to 'speed-violation' topic for futher processing (eg. to take action)
        }
        return response;
    }

    private boolean isMaxSpeedViolation(SpeedValidationRequest speedValidationRequest) {
        return speedValidationRequest.getSpeed() > maxSpeed;
    }

    // TODO: create listener, listen to 'max-speed' topic, change SpeedValidationService.maxSpeed when message comes
    public void changeMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
