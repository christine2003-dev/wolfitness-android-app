package com.example.wolfitness.repositories;

import com.example.wolfitness.models.FitnessGoal;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository for fitness goal-related operations
 */
public class GoalRepository {
    private static final String GOALS_COLLECTION = "goals";

    private FirebaseFirestore db;

    public GoalRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Get all available fitness goals
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getAllGoals() {
        return db.collection(GOALS_COLLECTION)
                .get();
    }

    /**
     * Get a fitness goal by name
     * @param name Goal name
     * @return Task with QuerySnapshot result
     */
    public Task<QuerySnapshot> getGoalByName(String name) {
        return db.collection(GOALS_COLLECTION)
                .whereEqualTo("name", name)
                .get();
    }

    /**
     * Create a new fitness goal
     * @param goal FitnessGoal model
     * @return Task result
     */
    public Task<Void> createGoal(FitnessGoal goal) {
        return db.collection(GOALS_COLLECTION)
                .document()
                .set(goal.toMap());
    }
}