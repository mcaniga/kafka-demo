package com.example.kafkademo.speedValidation.client;

import com.example.kafkademo.configurationManagement.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// NOTE: Normally, client would do http call to Configuration Management microservice, this is simplification
@Slf4j
@Component
public class ConfigurationManagementClient {
    private final ConfigurationService configurationService;

    public ConfigurationManagementClient(
        ConfigurationService configurationService
    ) {
        this.configurationService = configurationService;
    }

    public int getMaxSpeed(int busId) {
        return configurationService.getMaxSpeed(busId);
    }
}
