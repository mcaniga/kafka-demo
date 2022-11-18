package com.example.kafkademo.speedFraudDetection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SFPipelineConfigurationRequest {
    @Schema(description = "Defines maximum difference between timestamps of two taps to be in same window. Defined in hours", example = "1")
    @NotNull
    private Integer inactivityGap;

    @Schema(description = "Defines the threshold for non-fradulent speed in km/h", example = "50")
    @NotNull
    private Integer fraudSpeed;
}
