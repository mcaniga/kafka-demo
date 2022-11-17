package com.example.kafkademo.speedValidation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SpeedValidationCache {
    private final Map<Integer, Integer> busMaxSpeed;

    public SpeedValidationCache() {
        // ideally, max speed should be fetched from management on startup, but this is enough for illustration purposes :))
        this.busMaxSpeed = new ConcurrentHashMap<>();
    }

    public int getMaxSpeed(int busId) {
        return busMaxSpeed.get(busId);
    }

    public void saveMaxSpeed(int maxSpeed, int busId) {
        this.busMaxSpeed.put(busId, maxSpeed);
    }
}
