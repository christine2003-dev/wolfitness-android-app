package com.example.wolfitness.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserStats {
    private String id;
    private String userId;
    private float weight;
    private float height;
    private float bmi;
    private @ServerTimestamp Date recordedAt;

    // Empty constructor for Firestore
    public UserStats() {}

    // Constructor
    public UserStats(String userId, float weight, float height) {
        this.userId = userId;
        this.weight = weight;
        this.height = height;
        this.bmi = calculateBMI(weight, height);
    }

    // Static factory method from Firestore
    public static UserStats fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        UserStats stats = document.toObject(UserStats.class);
        if (stats != null) {
            stats.id = document.getId();
        }
        return stats;
    }

    // Helper method to calculate BMI
    private float calculateBMI(float weight, float height) {
        float heightInMeters = height / 100f;
        return weight / (heightInMeters * heightInMeters);
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("weight", weight);
        map.put("height", height);
        map.put("bmi", bmi);
        // recordedAt will be set by Firestore using @ServerTimestamp
        return map;
    }

    // Getters and setters
    // [Include all getters and setters here]
}