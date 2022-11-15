package com.example.kafkademo.configurationManagement.service;

import com.example.kafkademo.configurationManagement.dao.ConfigurationDao;
import com.example.kafkademo.configurationManagement.dto.MaxSpeedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigurationService {
    private final ConfigurationDao configurationDao;

    public ConfigurationService(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    public MaxSpeedResponse changeMaxSpeed(int maxSpeed) {
        log.debug("Saving max speed: {}", maxSpeed);
        configurationDao.saveMaxSpeed(maxSpeed);
        return new MaxSpeedResponse(maxSpeed);
    }
}
