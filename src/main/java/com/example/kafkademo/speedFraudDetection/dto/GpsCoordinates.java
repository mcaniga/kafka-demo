package com.example.kafkademo.speedFraudDetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GpsCoordinates {
    private Double latitude;
    private Double longitude;
}
