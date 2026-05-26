package com.example.wolfitness.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String USERS_COLLECTION = "users";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;

    public FirebaseHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Get current user
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Check if user is logged in
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Sign up new user
    public void signUpUser(String email, String password, OnCompleteListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User registration successful");
                        listener.onSuccess();
                    } else {
                        Log.w(TAG, "User registration failed", task.getException());
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    // Sign in existing user
    public void signInUser(String email, String password, OnCompleteListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User sign in successful");
                        listener.onSuccess();
                    } else {
                        Log.w(TAG, "User sign in failed", task.getException());
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }

    // Sign out user
    public void signOutUser() {
        mAuth.signOut();
    }

    // Save user profile data
    public void saveUserProfile(String userName, String gender, String dob,
                                String weight, String height, OnCompleteListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userName", userName);
            userProfile.put("email", user.getEmail());
            userProfile.put("gender", gender);
            userProfile.put("dateOfBirth", dob);
            userProfile.put("weight", weight);
            userProfile.put("height", height);
            userProfile.put("lastUpdated", System.currentTimeMillis());

            db.collection(USERS_COLLECTION).document(userId)
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User profile saved successfully");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error saving user profile", e);
                        listener.onFailure(e.getMessage());
                    });
        } else {
            listener.onFailure("User not authenticated");
        }
    }

    // Get user profile data
    public void getUserProfile(OnDataListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection(USERS_COLLECTION).document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            listener.onDataReceived(documentSnapshot.getData());
                        } else {
                            listener.onDataNotFound();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error getting user profile", e);
                        listener.onError(e.getMessage());
                    });
        } else {
            listener.onError("User not authenticated");
        }
    }

    // Interfaces for callbacks
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnDataListener {
        void onDataReceived(Map<String, Object> data);
        void onDataNotFound();
        void onError(String error);
    }
}