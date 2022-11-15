package com.example.kafkademo.configurationManagement.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ConfigurationDao {
    private int maxSpeed;

    public ConfigurationDao() {
        this.maxSpeed = 50;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void saveMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
