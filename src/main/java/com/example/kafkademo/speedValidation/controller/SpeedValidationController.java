package com.example.kafkademo.speedValidation.controller;

import com.example.kafkademo.speedValidation.dto.SpeedValidationRequest;
import com.example.kafkademo.speedValidation.dto.SpeedValidationResponse;
import com.example.kafkademo.speedValidation.service.SpeedValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequestMapping("/api/v1/speedValidation")
@Tag(name = "Speed Validation Microservice")
@RestController
public class SpeedValidationController {
    private final SpeedValidationService speedValidationService;

    public SpeedValidationController(SpeedValidationService speedValidationService) {
        this.speedValidationService = speedValidationService;
    }

    @Operation(operationId = "validate", summary = "Validates max speed")
    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<SpeedValidationResponse> validate(@Valid @RequestBody SpeedValidationRequest speedValidationRequest) {
        SpeedValidationResponse response = speedValidationService.validate(speedValidationRequest);
        return ResponseEntity.ok(response);
    }
}
