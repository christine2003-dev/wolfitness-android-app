package com.example.wolfitness.models;

public class Exercise {
    private String id;
    private String name;
    private String description; // 👀 Newly added
    private String difficulty; // 👀 Newly added
    private String duration;
    private String repetitions;
    private String imageUrl;
    private String workoutId;
    private int setNumber;
    private int orderInSet;

    public Exercise() {
        // Required empty constructor for Firestore
    }

    public Exercise(String id, String name, String description, String duration,
                    String repetitions, String imageUrl, String workoutId,
                    int setNumber, int orderInSet) {
        this.id = id;
        this.name = name;
        this.description = description; // 👀 Newly added
        this.difficulty = difficulty; // 👀 Newly added
        this.duration = duration;
        this.repetitions = repetitions;
        this.imageUrl = imageUrl;
        this.workoutId = workoutId;
        this.setNumber = setNumber;
        this.orderInSet = orderInSet;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { // 👀 Getter for description
        return description;
    }

    public void setDescription(String description) { // 👀 Setter for description
        this.description = description;
    }
    public String getDifficulty() { // 👀 Getter for description
        return difficulty;
    }

    public void setDifficulty(String difficulty) { // 👀 Setter for description
        this.difficulty = difficulty;
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(String repetitions) {
        this.repetitions = repetitions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getOrderInSet() {
        return orderInSet;
    }

    public void setOrderInSet(int orderInSet) {
        this.orderInSet = orderInSet;
    }
}