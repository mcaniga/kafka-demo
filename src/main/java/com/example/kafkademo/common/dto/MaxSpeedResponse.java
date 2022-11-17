package com.example.kafkademo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MaxSpeedResponse {
    private int maxSpeed;
    private int busId;
}
