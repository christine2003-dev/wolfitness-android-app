package com.example.wolfitness.repositories;

import com.example.wolfitness.models.UserPreferences;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository for handling user preferences
 */
public class UserPreferencesRepository {
    private static final String TAG = "UserPreferencesRepository";
    private static final String PREFERENCES_COLLECTION = "userPreferences";

    private FirebaseFirestore db;

    public UserPreferencesRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get user preferences by user ID
     */
    public Task<DocumentSnapshot> getUserPreferences(String userId) {
        return db.collection(PREFERENCES_COLLECTION).document(userId).get();
    }

    /**
     * Save or update user preferences
     */
    public Task<Void> saveUserPreferences(UserPreferences preferences) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(preferences.getUserId())
                .set(preferences.toMap());
    }

    /**
     * Update a single preference
     */
    public Task<Void> updatePreference(String userId, String key, Object value) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(userId)
                .update(key, value);
    }

    /**
     * Update notification settings
     */
    public Task<Void> updateNotificationSettings(String userId, boolean enabled) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(userId)
                .update("notificationsEnabled", enabled);
    }

    /**
     * Update workout reminder settings
     */
    public Task<Void> updateWorkoutReminders(String userId, boolean enabled, String reminderTime) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(userId)
                .update(
                        "workoutReminders", enabled,
                        "reminderTime", reminderTime
                );
    }

    /**
     * Update units preference
     */
    public Task<Void> updateUnits(String userId, String weightUnit, String heightUnit) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(userId)
                .update(
                        "weightUnit", weightUnit,
                        "heightUnit", heightUnit
                );
    }

    /**
     * Update theme preference
     */
    public Task<Void> updateTheme(String userId, String theme) {
        return db.collection(PREFERENCES_COLLECTION)
                .document(userId)
                .update("theme", theme);
    }
}