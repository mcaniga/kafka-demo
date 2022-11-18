package com.example.kafkademo.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MaxSpeedUpdateEvent {
    @Schema(description = "Identifier of the bus", example = "10")
    private int busId;
}
