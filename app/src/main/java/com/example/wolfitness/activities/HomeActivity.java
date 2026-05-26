package com.example.wolfitness.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wolfitness.R;
import com.example.wolfitness.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private LinearLayout homeNav, workoutNav, profileNav;
    private ImageView homeIcon, workoutIcon, profileIcon;
    private TextView userNameTextView;
    private static final String PREFS_NAME = "WoFitnessPrefs";
    private static final String KEY_USERNAME = "username";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Repository if using repository pattern
    private UserRepository userRepository;
    private Button shopNowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize repository if using repository pattern
        userRepository = new UserRepository();


        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            // Redirect to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize the user name TextView
        userNameTextView = findViewById(R.id.user_name);

        // Load user data
        loadUserData();

        // Initialize bottom navigation
        initBottomNavigation();

        // Notification icon click
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        // Setup view more buttons
        setupViewMoreButtons();
    }

    /**
     * Load user data
     */
    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();

        // Option 1: Using direct Firebase calls
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get user name
                        String userName = documentSnapshot.getString("name");
                        if (userName != null && !userName.isEmpty()) {
                            userNameTextView.setText(userName);

                            // Save to SharedPreferences for backward compatibility
                            saveUserNameToPrefs(userName);
                        } else {
                            // Fall back to SharedPreferences
                            String sharedPrefsName = getUserNameFromPrefs();
                            userNameTextView.setText(sharedPrefsName);
                        }
                    } else {
                        // Use SharedPreferences as fallback
                        String userName = getUserNameFromPrefs();
                        userNameTextView.setText(userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    // Use SharedPreferences as fallback
                    String userName = getUserNameFromPrefs();
                    userNameTextView.setText(userName);
                });

        // Option 2: Using Repository Pattern (Uncomment if using repository)
        /*
        userRepository.getUserById(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    User user = User.fromDocument(documentSnapshot);

                    if (user != null) {
                        // Get the name field directly - this is correct
                        String userName = user.getName();
                        if (userName != null && !userName.isEmpty()) {
                            userNameTextView.setText(userName);
                            saveUserNameToPrefs(userName);
                        } else {
                            String sharedPrefsName = getUserNameFromPrefs();
                            userNameTextView.setText(sharedPrefsName);
                        }
                    } else {
                        String userName = getUserNameFromPrefs();
                        userNameTextView.setText(userName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    String userName = getUserNameFromPrefs();
                    userNameTextView.setText(userName);
                });
        */
    }

    /**
     * Initialize bottom navigation bar
     */
    private void initBottomNavigation() {
        // Get navigation elements
        homeNav = findViewById(R.id.home_nav);
        workoutNav = findViewById(R.id.workout_nav);
        profileNav = findViewById(R.id.profile_nav);
        shopNowButton = findViewById(R.id.shopNowButton);

        homeIcon = findViewById(R.id.home_icon);
        workoutIcon = findViewById(R.id.workout_icon);
        profileIcon = findViewById(R.id.profile_icon);

        // Set home icon as active
        updateNavIcons(R.id.home_icon);

        // Set click listeners for bottom navigation
        homeNav.setOnClickListener(v -> {
            // Already on home screen
            updateNavIcons(R.id.home_icon);
        });

        shopNowButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(        Intent.ACTION_VIEW,
                    Uri.parse("https://wolfitness-frontend.vercel.app/")
            );    startActivity(browserIntent);});



        workoutNav.setOnClickListener(v -> {
            // Navigate to workout details screen
            updateNavIcons(R.id.workout_icon);
            Intent intent = new Intent(HomeActivity.this, WorkoutDetails1Activity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        profileNav.setOnClickListener(v -> {
            updateNavIcons(R.id.profile_icon);
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    /**
     * Update navigation icons to highlight the selected one
     */
    private void updateNavIcons(int selectedId) {
        int gradientColorEnd = getResources().getColor(R.color.gradient_end); // #6B50F6
        int gray = getResources().getColor(R.color.gray); // #9E9E9E

        homeIcon.setColorFilter(selectedId == R.id.home_icon ? gradientColorEnd : gray);
        workoutIcon.setColorFilter(selectedId == R.id.workout_icon ? gradientColorEnd : gray);
        profileIcon.setColorFilter(selectedId == R.id.profile_icon ? gradientColorEnd : gray);
    }

    /**
     * Sets up click listeners for all "View more" buttons
     */
    private void setupViewMoreButtons() {
        MaterialButton mondayButton = findViewById(R.id.monday_view_more_button);
        MaterialButton tuesdayButton = findViewById(R.id.tuesday_view_more_button);
        MaterialButton abButton = findViewById(R.id.ab_view_more_button);

        View.OnClickListener workoutDetailsListener = v -> {
            Log.d(TAG, "View More clicked - Navigating to WorkoutDetails1Activity");
            Toast.makeText(HomeActivity.this, "Opening Workout Details", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, WorkoutDetails1Activity.class);
            startActivity(intent);
        };

        if (mondayButton != null) {
            mondayButton.setOnClickListener(workoutDetailsListener);
        } else {
            Log.w(TAG, "Monday View More button is null");
        }

        if (tuesdayButton != null) {
            tuesdayButton.setOnClickListener(workoutDetailsListener);
        } else {
            Log.w(TAG, "Tuesday View More button is null");
        }

        if (abButton != null) {
            abButton.setOnClickListener(workoutDetailsListener);
        } else {
            Log.w(TAG, "AB View More button is null");
        }
    }

    /**
     * Get the username from SharedPreferences
     */
    private String getUserNameFromPrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getString(KEY_USERNAME, "User");
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