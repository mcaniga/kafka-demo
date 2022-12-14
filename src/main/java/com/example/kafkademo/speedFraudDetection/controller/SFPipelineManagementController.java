package com.example.kafkademo.speedFraudDetection.controller;

import com.example.kafkademo.speedFraudDetection.dto.SFPipelineConfigurationRequest;
import com.example.kafkademo.speedFraudDetection.dto.SFPipelineConfigurationResponse;
import com.example.kafkademo.speedFraudDetection.dto.ValidatedTap;
import com.example.kafkademo.speedFraudDetection.kafka.TapProducer;
import com.example.kafkademo.speedFraudDetection.service.SFPipelineManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequestMapping("/api/v1/fraudConfiguration")
@Tag(name = "Speed Fraud Detection Microservice")
@RestController
public class SFPipelineManagementController {
    private final SFPipelineManagementService sfPipelineManagementService;
    private final TapProducer tapProducer;

    public SFPipelineManagementController(
        SFPipelineManagementService sfPipelineManagementService,
        TapProducer tapProducer
    ) {
        this.sfPipelineManagementService = sfPipelineManagementService;
        this.tapProducer = tapProducer;
    }

    @Operation(operationId = "changeSpeedFraudConfiguration", summary = "Changes speed fraud configuration")
    @PostMapping(value = "/changeSpeedFraudConfiguration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<SFPipelineConfigurationResponse> changeSpeedFraudConfiguration(
            @Valid @RequestBody SFPipelineConfigurationRequest sfPipelineConfigurationRequest
    ) {
        sfPipelineManagementService.restartDetector(sfPipelineConfigurationRequest);
        SFPipelineConfigurationResponse response = new SFPipelineConfigurationResponse(
            sfPipelineConfigurationRequest.getInactivityGap(),
            sfPipelineConfigurationRequest.getFraudSpeed()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(operationId = "sendTap", summary = "Sends Tap")
    @PostMapping(value = "/sendTap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<ValidatedTap> sendTap(
            @Valid @RequestBody ValidatedTap validatedTap
    ) {
        tapProducer.send(validatedTap);
        return ResponseEntity.ok(validatedTap);
    }
}
