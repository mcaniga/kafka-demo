package com.example.kafkademo.speedFraudDetection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TapAccumulator {

    private TreeMap<Instant, ValidatedTap> taps;
    private ValidatedTap fraudTap;
}