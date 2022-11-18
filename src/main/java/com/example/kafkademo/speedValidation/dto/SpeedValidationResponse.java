package com.example.kafkademo.speedValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpeedValidationResponse {
    @Schema(description = "Flag describing if violation of maximum speed occured", example = "true")
    private boolean isViolation;

    @Schema(description = "Maximum permitied speed of the bus in km/h", example = "50")
    private int maxSpeed;

    @Schema(description = "Actual speed of the bus in km/h", example = "60")
    private int speed;

    @Schema(description = "Identifier of the bus", example = "10")
    private int busId;
}
