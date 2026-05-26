package com.example.wolfitness.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.wolfitness.repositories.UserRepository;

public class SuccessRegisterActivity extends AppCompatActivity {
    private Button goToHomeButton;
    private ImageView successImage;
    private TextView welcomeText;
    private TextView descriptionText;
    private static final String PREFS_NAME = "WoFitnessPrefs";
    private static final String KEY_USERNAME = "username";
    private String userName;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize repository
        userRepository = new UserRepository();

        goToHomeButton = findViewById(R.id.go_to_home_button);
        successImage = findViewById(R.id.success_image);
        welcomeText = findViewById(R.id.welcome_text);
        descriptionText = findViewById(R.id.description_text);

        // Try to get the name from intent extras
        userName = getIntent().getStringExtra("USER_NAME");

        // If name is not available from intent, try to get from current user in Firebase
        if (userName == null || userName.isEmpty()) {
            if (userRepository.getCurrentUser() != null) {
                String userId = userRepository.getCurrentUser().getUid();
                // Get user data from Firestore
                userRepository.getUserById(userId).addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        userName = userDoc.getString("name");
                        if (userName == null || userName.isEmpty()) {
                            userName = "User"; // Default if name field is empty
                        }
                        updateWelcomeText(userName);
                        saveUserNameToPrefs(userName);
                    } else {
                        userName = "User"; // Default if document doesn't exist
                        updateWelcomeText(userName);
                    }
                }).addOnFailureListener(e -> {
                    userName = "User"; // Default on error
                    updateWelcomeText(userName);
                });
            } else {
                userName = "User"; // Default if no current user
                updateWelcomeText(userName);
            }
        } else {
            // Name was available in the intent
            updateWelcomeText(userName);
            saveUserNameToPrefs(userName);
        }

        // Set description text
        descriptionText.setText("You are all set now, let's reach your goals together with us");

        goToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessRegisterActivity.this, HomeActivity.class);
                // Pass the username to HomeActivity
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Update the welcome text with the user's name
     */
    private void updateWelcomeText(String name) {
        welcomeText.setText(String.format("Welcome, %s", name));
    }

    /**
     * Save username to SharedPreferences for persistence
     */
    private void saveUserNameToPrefs(String userName) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_USERNAME, userName);
        editor.apply();
    }
}