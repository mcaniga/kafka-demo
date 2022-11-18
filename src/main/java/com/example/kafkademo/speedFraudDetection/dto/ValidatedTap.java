package com.example.kafkademo.speedFraudDetection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidatedTap {
    @Schema(description = "Identifier of the medium", example = "50", required = true)
    private String mediumId;
    @Schema(description = "GPS coordinates of tap", implementation = GpsCoordinates.class)
    private GpsCoordinates gpsCoordinates;
    @Schema(description = "Timestamp of the tap in ISO-8601", example = "2022-01-10T15:23:44Z", required = true)
    private Instant timestamp;
}