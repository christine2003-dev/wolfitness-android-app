package com.example.wolfitness.repositories;

import android.util.Log;

import com.example.wolfitness.models.AuthState;
import com.example.wolfitness.models.User;
import com.example.wolfitness.models.UserPreferences;
import com.example.wolfitness.models.UserStats;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for user-related operations
 * Abstraction layer between the UI and Firebase services
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String USERS_COLLECTION = "users";
    private static final String USER_PREFERENCES_COLLECTION = "user_preferences";
    private static final String USER_STATS_COLLECTION = "user_stats";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private AuthState currentAuthState;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize auth state
        if (auth.getCurrentUser() != null) {
            currentAuthState = new AuthState(AuthState.STATE_LOGGED_IN, auth.getCurrentUser().getUid());
        } else {
            currentAuthState = new AuthState(AuthState.STATE_LOGGED_OUT);
        }
    }

    /**
     * Register a new user with email and password
     * @param email user email
     * @param password user password
     * @return Task result
     */
    public Task<AuthResult> registerUser(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Login a user with email and password
     * @param email user email
     * @param password user password
     * @return Task result
     */
    public Task<AuthResult> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Send password reset email to user
     * @param email user email
     * @return Task result
     */
    public Task<Void> resetPassword(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        auth.signOut();
        currentAuthState = new AuthState(AuthState.STATE_LOGGED_OUT);
    }

    /**
     * Get the current Firebase user
     * @return FirebaseUser or null if not logged in
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Create a new user document in Firestore
     * @param user User model
     * @return Task result
     */
    public Task<Void> createUser(User user) {
        return db.collection(USERS_COLLECTION)
                .document(user.getId())
                .set(user.toMap());
    }

    /**
     * Update a user document in Firestore
     * @param user User model
     * @return Task result
     */
    public Task<Void> updateUser(User user) {
        return db.collection(USERS_COLLECTION)
                .document(user.getId())
                .set(user.toMap(), SetOptions.merge());
    }

    /**
     * Update specific fields of a user document
     * @param userId User ID
     * @param updates Map of fields to update
     * @return Task result
     */
    public Task<Void> updateUserFields(String userId, Map<String, Object> updates) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates);
    }

    /**
     * Update a single field of a user document
     * @param userId User ID
     * @param field Field name
     * @param value Field value
     * @return Task result
     */
    public Task<Void> updateUserField(String userId, String field, Object value) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .update(field, value);
    }

    /**
     * Get a user by ID
     * @param userId User ID
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getUserById(String userId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .get();
    }

    /**
     * Update the last login timestamp for a user
     * @param userId User ID
     * @return Task result
     */
    public Task<Void> updateLastLogin(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLoginAt", System.currentTimeMillis());

        return db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates);
    }

    /**
     * Save user preferences
     * @param preferences UserPreferences model
     * @return Task result
     */
    public Task<Void> saveUserPreferences(UserPreferences preferences) {
        return db.collection(USER_PREFERENCES_COLLECTION)
                .document(preferences.getUserId())
                .set(preferences.toMap());
    }

    /**
     * Get user preferences
     * @param userId User ID
     * @return Task with DocumentSnapshot result
     */
    public Task<DocumentSnapshot> getUserPreferences(String userId) {
        return db.collection(USER_PREFERENCES_COLLECTION)
                .document(userId)
                .get();
    }

    /**
     * Update a specific user preference field
     * @param userId User ID
     * @param field Field name
     * @param value Field value
     * @return Task result
     */
    public Task<Void> updateUserPreferencesField(String userId, String field, Object value) {
        return db.collection(USER_PREFERENCES_COLLECTION)
                .document(userId)
                .update(field, value);
    }

    /**
     * Add user stats entry
     * @param stats UserStats model
     * @return Task with DocumentReference result
     */
    public Task<DocumentReference> addUserStats(UserStats stats) {
        return db.collection(USER_STATS_COLLECTION)
                .add(stats.toMap());
    }

    /**
     * Get the current auth state
     * @return Current AuthState
     */
    public AuthState getAuthState() {
        return currentAuthState;
    }

    /**
     * Set the current auth state
     * @param authState New AuthState
     */
    public void setAuthState(AuthState authState) {
        this.currentAuthState = authState;
        Log.d(TAG, "Auth state updated to: " + authState.getState());
    }
}