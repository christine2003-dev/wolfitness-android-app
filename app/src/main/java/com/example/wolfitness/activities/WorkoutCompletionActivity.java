package com.example.wolfitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wolfitness.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WorkoutCompletionActivity extends AppCompatActivity {

    private Button backToHomeButton;
    private ImageView completionImage;
    private TextView congratulationText;
    private TextView quoteText;
    private TextView authorText;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_completion);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Apply system UI insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind views
        backToHomeButton = findViewById(R.id.back_to_home_button);
        completionImage = findViewById(R.id.completion_image);
        congratulationText = findViewById(R.id.congratulation_text);
        quoteText = findViewById(R.id.quote_text);
        authorText = findViewById(R.id.author_text);

        // Set quote and author
        quoteText.setText("Exercises is king and nutrition is queen. Combine the two and you will have a kingdom");

        // Load user data and update greeting
        loadUserName();

        // Back to home button
        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutCompletionActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to this screen
        });
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = "User";

        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userName = currentUser.getDisplayName();
            } else {
                String email = currentUser.getEmail();
                if (email != null && !email.isEmpty()) {
                    int atIndex = email.indexOf("@");
                    if (atIndex > 0) {
                        userName = email.substring(0, atIndex); // e.g., john@example.com → john
                    } else {
                        userName = email; // fallback
                    }
                }
            }
        }

        congratulationText.setText("Congratulations, " + userName + "! You Have Finished Your Workout");
    }
}