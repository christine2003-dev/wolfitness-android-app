package com.example.wolfitness.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for user preferences
 */
public class UserPreferences {
    private String userId;
    private boolean notificationsEnabled;
    private boolean workoutReminders;
    private String reminderTime; // e.g., "18:00" for 6 PM
    private String weightUnit; // "kg" or "lbs"
    private String heightUnit; // "cm" or "ft"
    private String theme; // "light", "dark", or "system"

    // Empty constructor for Firestore
    public UserPreferences() {
        // Initialize with default values
        this.notificationsEnabled = true;
        this.workoutReminders = true;
        this.reminderTime = "18:00";
        this.weightUnit = "kg";
        this.heightUnit = "cm";
        this.theme = "system";
    }

    // Constructor with userId
    public UserPreferences(String userId) {
        this();  // Call the empty constructor to set defaults
        this.userId = userId;
    }

    // Full constructor
    public UserPreferences(String userId, boolean notificationsEnabled, boolean workoutReminders,
                           String reminderTime, String weightUnit, String heightUnit, String theme) {
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
        this.workoutReminders = workoutReminders;
        this.reminderTime = reminderTime;
        this.weightUnit = weightUnit;
        this.heightUnit = heightUnit;
        this.theme = theme;
    }

    // Create from Firestore document
    public static UserPreferences fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        UserPreferences preferences = document.toObject(UserPreferences.class);

        // Ensure userId is set (if it wasn't part of the document data)
        if (preferences != null && preferences.userId == null) {
            preferences.userId = document.getId();
        }

        return preferences;
    }

    // Convert to Map for Firestore
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        // Don't include userId in the map as it's the document ID
        map.put("notificationsEnabled", notificationsEnabled);
        map.put("workoutReminders", workoutReminders);
        map.put("reminderTime", reminderTime);
        map.put("weightUnit", weightUnit);
        map.put("heightUnit", heightUnit);
        map.put("theme", theme);
        return map;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isWorkoutReminders() {
        return workoutReminders;
    }

    public void setWorkoutReminders(boolean workoutReminders) {
        this.workoutReminders = workoutReminders;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public void setHeightUnit(String heightUnit) {
        this.heightUnit = heightUnit;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}