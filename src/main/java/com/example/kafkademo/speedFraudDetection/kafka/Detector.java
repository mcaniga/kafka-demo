package com.example.kafkademo.speedFraudDetection.kafka;

public interface Detector extends Runnable {
    void start();

    void stop();
}