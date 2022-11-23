package com.example.kafkademo.speedFraudDetection.service;

public class GpsUtils {
    private final static double AVERAGE_RADIUS_OF_EARTH_IN_KMH = 6371;

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Haversine formula assumes that Earth is a perfect sphere, so small error is possible
     */
    public static double calculateDistanceInKmh(double userLat, double userLng, double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                   (Math.cos(Math.toRadians(userLat))) *
                   (Math.cos(Math.toRadians(venueLat))) *
                   (Math.sin(lngDistance / 2)) *
                   (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return AVERAGE_RADIUS_OF_EARTH_IN_KMH * c;
    }
}