package com.example.kafkademo.configurationManagement.service;

import com.example.kafkademo.configurationManagement.dao.ConfigurationDao;
import com.example.kafkademo.common.dto.MaxSpeedResponse;
import com.example.kafkademo.configurationManagement.kafka.MaxSpeedProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigurationService {
    private final ConfigurationDao configurationDao;
    private final MaxSpeedProducer maxSpeedProducer;

    public ConfigurationService(
        ConfigurationDao configurationDao,
        MaxSpeedProducer maxSpeedProducer
    ) {
        this.configurationDao = configurationDao;
        this.maxSpeedProducer = maxSpeedProducer;
    }

    public MaxSpeedResponse changeMaxSpeed(int maxSpeed, int busId) {
        log.debug("Saving max speed: {}", maxSpeed);
        configurationDao.saveMaxSpeed(maxSpeed, busId);
        MaxSpeedResponse maxSpeedResponse = new MaxSpeedResponse(maxSpeed, busId);
        maxSpeedProducer.send(maxSpeedResponse);
        return maxSpeedResponse;
    }

    public int getMaxSpeed(int busId) {
        log.debug("Fetching max speed for bus: {}", busId);
        return configurationDao.getMaxSpeed(busId);
    }
}
