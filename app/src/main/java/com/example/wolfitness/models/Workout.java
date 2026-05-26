package com.example.wolfitness.models;

public class Workout {
    private String id;
    private String name;
    private String description;
    private String difficulty;
    private int totalExercises;
    private String duration;
    private String caloriesBurn;
    private String coverImageUrl;
    private int numberOfSets;

    public Workout() {
        // Required empty constructor for Firestore
    }

    public Workout(String id, String name, String description, String difficulty,
                   int totalExercises, String duration, String caloriesBurn,
                   String coverImageUrl, int numberOfSets) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.totalExercises = totalExercises;
        this.duration = duration;
        this.caloriesBurn = caloriesBurn;
        this.coverImageUrl = coverImageUrl;
        this.numberOfSets = numberOfSets;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTotalExercises() { return totalExercises; }
    public void setTotalExercises(int totalExercises) { this.totalExercises = totalExercises; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getCaloriesBurn() { return caloriesBurn; }
    public void setCaloriesBurn(String caloriesBurn) { this.caloriesBurn = caloriesBurn; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public int getNumberOfSets() { return numberOfSets; }
    public void setNumberOfSets(int numberOfSets) { this.numberOfSets = numberOfSets; }

    public String getImageUrl() {
        return coverImageUrl;
    }
}