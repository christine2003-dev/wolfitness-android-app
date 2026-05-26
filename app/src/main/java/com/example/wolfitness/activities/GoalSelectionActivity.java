package com.example.wolfitness.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.wolfitness.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GoalSelectionActivity extends AppCompatActivity {

    private Button confirmButton;
    private CardView improveShapeCard;
    private CardView leanAndToneCard;
    private CardView loseFatCard;
    private String userName;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Selected goal
    private String selectedGoal = "";
    private static final String PREFS_NAME = "WoFitnessPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_selection);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get user name from intent extras
        userName = getIntent().getStringExtra("USER_NAME");
        if (userName == null || userName.isEmpty()) {
            // Try to get from SharedPreferences
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            userName = settings.getString(KEY_USERNAME, "User");
        }

        TextView titleTextView = findViewById(R.id.goal_selection_title);
        TextView subtitleTextView = findViewById(R.id.goal_selection_subtitle);
        improveShapeCard = findViewById(R.id.improve_shape_card);
        leanAndToneCard = findViewById(R.id.lean_and_tone_card);
        loseFatCard = findViewById(R.id.lose_fat_card);
        confirmButton = findViewById(R.id.confirm_button);

        titleTextView.setText("What is your goal ?");
        subtitleTextView.setText("It will help us to choose a best program for you");

        // Add goal selection functionality
        improveShapeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGoal = "Improve Shape";
                highlightSelectedCard(improveShapeCard);
            }
        });

        leanAndToneCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGoal = "Lean & Tone";
                highlightSelectedCard(leanAndToneCard);
            }
        });

        loseFatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGoal = "Lose a Fat";
                highlightSelectedCard(loseFatCard);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedGoal.isEmpty()) {
                    Toast.makeText(GoalSelectionActivity.this, "Please select a goal", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save goal to Firestore
                saveGoalToFirestore();
            }
        });
    }

    private void highlightSelectedCard(CardView selectedCard) {
        // Reset all cards to default state
        improveShapeCard.setCardElevation(4); // Using numeric value instead of "4dp"
        leanAndToneCard.setCardElevation(4);
        loseFatCard.setCardElevation(4);

        // Elevate the selected card
        selectedCard.setCardElevation(12); // Using numeric value instead of "12dp"
    }

    private void saveGoalToFirestore() {
        String userId = auth.getCurrentUser().getUid();

        confirmButton.setEnabled(false);
        confirmButton.setText("Saving...");

        // Update the user document with selected goal
        db.collection("users").document(userId)
                .update("goal", selectedGoal)
                .addOnSuccessListener(aVoid -> {
                    // If userName is still empty, try to get it from Firestore
                    if (userName == null || userName.isEmpty()) {
                        db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String name = documentSnapshot.getString("name");
                                        if (name != null && !name.isEmpty()) {
                                            userName = name;
                                        }
                                    }
                                    navigateToSuccessScreen();
                                })
                                .addOnFailureListener(e -> {
                                    // Navigate even if we couldn't get the name
                                    navigateToSuccessScreen();
                                });
                    } else {
                        // We already have the name, navigate directly
                        navigateToSuccessScreen();
                    }
                })
                .addOnFailureListener(e -> {
                    confirmButton.setEnabled(true);
                    confirmButton.setText("Confirm");

                    // If update fails (document might not exist), try to set the document
                    if (e.getMessage().contains("NOT_FOUND")) {
                        // Create a new user document with the goal
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("goal", selectedGoal);

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid2 -> {
                                    navigateToSuccessScreen();
                                })
                                .addOnFailureListener(e2 -> {
                                    Toast.makeText(GoalSelectionActivity.this, "Error saving goal: " + e2.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(GoalSelectionActivity.this, "Error saving goal: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Navigate to SuccessRegisterActivity with the user's name
     */
    private void navigateToSuccessScreen() {
        Intent intent = new Intent(GoalSelectionActivity.this, SuccessRegisterActivity.class);
        intent.putExtra("USER_NAME", userName);
        startActivity(intent);
        finish();
    }
}