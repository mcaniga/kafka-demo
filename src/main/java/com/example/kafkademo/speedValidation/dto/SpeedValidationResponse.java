package com.example.kafkademo.speedValidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpeedValidationResponse {
    boolean isViolation;
    private int maxSpeed;
    private int speed;
    private long busId;
}
