package com.example.wolfitness.repositories;

import com.example.wolfitness.models.UserStats;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository for handling user statistics
 */
public class UserStatsRepository {
    private static final String TAG = "UserStatsRepository";
    private static final String STATS_COLLECTION = "userStats";

    private FirebaseFirestore db;

    public UserStatsRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Add a new stats record
     */
    public Task<DocumentReference> addUserStats(UserStats stats) {
        return db.collection(STATS_COLLECTION).add(stats.toMap());
    }

    /**
     * Get a specific stats record
     */
    public Task<DocumentSnapshot> getUserStats(String statsId) {
        return db.collection(STATS_COLLECTION).document(statsId).get();
    }

    /**
     * Get user stats history, ordered by timestamp
     */
    public Task<QuerySnapshot> getUserStatsHistory(String userId) {
        return db.collection(STATS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("recordedAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get latest user stats
     */
    public Task<QuerySnapshot> getLatestUserStats(String userId) {
        return db.collection(STATS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("recordedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get();
    }

    /**
     * Delete a stats record
     */
    public Task<Void> deleteUserStats(String statsId) {
        return db.collection(STATS_COLLECTION).document(statsId).delete();
    }

    /**
     * Get stats from a specific date range
     */
    public Task<QuerySnapshot> getUserStatsInDateRange(String userId, long startTime, long endTime) {
        return db.collection(STATS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("recordedAt", startTime)
                .whereLessThanOrEqualTo("recordedAt", endTime)
                .orderBy("recordedAt", Query.Direction.ASCENDING)
                .get();
    }
}