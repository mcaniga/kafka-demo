package com.example.kafkademo.speedValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpeedValidationRequest {
    @Schema(description = "Actual speed of the bus in km/h", example = "60", required = true)
    @NotNull
    @Min(0)
    private int speed;

    @Schema(description = "Identifier of the bus", example = "10", required = true)
    @NotNull
    private int busId;
}
