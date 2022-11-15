package com.example.kafkademo.configurationManagement.controller;

import com.example.kafkademo.configurationManagement.dto.MaxSpeedRequest;
import com.example.kafkademo.configurationManagement.dto.MaxSpeedResponse;
import com.example.kafkademo.configurationManagement.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequestMapping("/api/v1/configuration")
@Tag(name = "Actions")
@RestController
public class ConfigurationController {
    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Operation(operationId = "changeMaxSpeed", summary = "Changes max speed")
    @PostMapping(value = "/changeMaxSpeed", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<MaxSpeedResponse> changeMaxSpeed(@Valid @RequestBody MaxSpeedRequest maxSpeedRequest) {
        MaxSpeedResponse response = configurationService.changeMaxSpeed(maxSpeedRequest.getMaxSpeed());
        return ResponseEntity.ok(response);
    }
}
