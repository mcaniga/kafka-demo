package com.example.kafkademo.speedFraudDetection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GpsCoordinates {
    @Schema(description = "Identifier of the medium", example = "50", required = true)
    private Double latitude;
    @Schema(description = "Identifier of the medium", example = "50", required = true)
    private Double longitude;
}
