package com.example.kafkademo.speedValidation.service;

import com.example.kafkademo.speedValidation.client.ConfigurationManagementClient;
import com.example.kafkademo.speedValidation.dto.SpeedValidationRequest;
import com.example.kafkademo.speedValidation.dto.SpeedValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SpeedValidationService {
    private final SpeedValidationCache speedValidationCache;
    private final ConfigurationManagementClient configurationManagementClient;

    public SpeedValidationService(
        SpeedValidationCache speedValidationCache,
        ConfigurationManagementClient configurationManagementClient
    ) {
        this.speedValidationCache = speedValidationCache;
        this.configurationManagementClient = configurationManagementClient;
    }

    public SpeedValidationResponse validate(SpeedValidationRequest speedValidationRequest) {
        log.debug("Validating speed: {}", speedValidationRequest);
        int maxSpeed = speedValidationCache.getMaxSpeed(speedValidationRequest.getBusId());
        boolean isMaxSpeedViolated = isMaxSpeedViolation(speedValidationRequest, maxSpeed);
        SpeedValidationResponse response = new SpeedValidationResponse(
            isMaxSpeedViolated,
            maxSpeed,
            speedValidationRequest.getSpeed(),
            speedValidationRequest.getBusId()
        );
        if (isMaxSpeedViolated) {
            log.debug("Max speed was violated");
            // here we can send response eg. to 'speed-violation' topic for futher processing (eg. to take action)
        }
        return response;
    }

    public void cacheMaxSpeed(int busId) {
        log.debug("Caching max speed for bus with id: {}", busId);
        int maxSpeed = configurationManagementClient.getMaxSpeed(busId);
        speedValidationCache.saveMaxSpeed(maxSpeed, busId);
    }

    private boolean isMaxSpeedViolation(SpeedValidationRequest speedValidationRequest, int maxSpeed) {
        return speedValidationRequest.getSpeed() > maxSpeed;
    }
}
