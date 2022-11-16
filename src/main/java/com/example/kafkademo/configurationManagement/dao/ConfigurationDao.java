package com.example.kafkademo.configurationManagement.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class ConfigurationDao {
    private final Map<Integer, Integer> busMaxSpeed;

    public ConfigurationDao() {
        this.busMaxSpeed = new ConcurrentHashMap<>();
    }

    public int getMaxSpeed(int busId) {
        return busMaxSpeed.get(busId);
    }

    public void saveMaxSpeed(int maxSpeed, int busId) {
        this.busMaxSpeed.put(maxSpeed, busId);
    }
}
