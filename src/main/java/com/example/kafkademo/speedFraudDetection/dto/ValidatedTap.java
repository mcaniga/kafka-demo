package com.example.kafkademo.speedFraudDetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidatedTap {
    private String bot;
    private GpsCoordinates gpsCoordinates;
    private Instant date;
}