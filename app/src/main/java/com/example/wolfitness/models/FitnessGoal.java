package com.example.wolfitness.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FitnessGoal {
    public static final String IMPROVE_SHAPE = "Improve Shape";
    public static final String LEAN_AND_TONE = "Lean & Tone";
    public static final String LOSE_FAT = "Lose a Fat";

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String recommendedWorkoutType;

    // Empty constructor for Firestore
    public FitnessGoal() {}

    // Constructor
    public FitnessGoal(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Static factory method from Firestore
    public static FitnessGoal fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        FitnessGoal goal = document.toObject(FitnessGoal.class);
        if (goal != null) {
            goal.id = document.getId();
        }
        return goal;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("imageUrl", imageUrl);
        map.put("recommendedWorkoutType", recommendedWorkoutType);
        return map;
    }

    // Getters and setters
    // [Include all getters and setters here]
}