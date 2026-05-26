package com.example.wolfitness.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;          // Firebase UID (from Authentication)
    private String name;        // User's full name
    private String email;       // User's email address
    private String phone;       // User's phone number
    private String gender;      // User's gender (from spinner)
    private String dateOfBirth; // Date of birth (dd-MM-yyyy format)
    private String weight;      // Weight in kg
    private String height;      // Height in cm
    private String goal;        // Selected fitness goal
    private int age;            // Calculated age (optional, can be derived)
    private String profileImageUrl; // URL to profile image (optional)
    private boolean notificationsEnabled; // Notification preferences
    private long createdAt;     // Account creation timestamp
    private long lastLoginAt;   // Last login timestamp

    // Empty constructor required for Firestore
    public User() {}

    // Minimal constructor for signup
    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    // Static factory method to create from Firestore
    public static User fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        User user = document.toObject(User.class);
        if (user != null) {
            user.id = document.getId(); // Ensure ID is set from document
        }
        return user;
    }

    // Convert to Map for Firestore
    @Exclude // Excludes this method from Firestore serialization
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("phone", phone);
        map.put("gender", gender);
        map.put("dateOfBirth", dateOfBirth);
        map.put("weight", weight);
        map.put("height", height);
        map.put("goal", goal);
        map.put("age", age);
        map.put("profileImageUrl", profileImageUrl);
        map.put("notificationsEnabled", notificationsEnabled);
        map.put("createdAt", createdAt);
        map.put("lastLoginAt", lastLoginAt);

        // Don't include id as it's the document ID in Firestore
        return map;
    }

    // Calculate BMI (useful for profile)
    @Exclude
    public float calculateBMI() {
        try {
            float heightInMeters = Float.parseFloat(height) / 100f;
            float weightInKg = Float.parseFloat(weight);
            return weightInKg / (heightInMeters * heightInMeters);
        } catch (NumberFormatException | NullPointerException e) {
            return 0f; // Default value if calculation fails
        }
    }

    // Get BMI category
    @Exclude
    public String getBMICategory() {
        float bmi = calculateBMI();
        if (bmi < 18.5f) return "Underweight";
        if (bmi < 25f) return "Normal";
        if (bmi < 30f) return "Overweight";
        return "Obese";
    }

    // Check if the user has a complete profile
    @Exclude
    public boolean hasCompleteProfile() {
        return height != null && !height.isEmpty() &&
                weight != null && !weight.isEmpty() &&
                gender != null && !gender.isEmpty() &&
                dateOfBirth != null && !dateOfBirth.isEmpty();
    }

    // Getters and setters for all fields
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}