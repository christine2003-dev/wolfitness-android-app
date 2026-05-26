package com.example.wolfitness.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.wolfitness.R;
import com.example.wolfitness.models.User;
import com.example.wolfitness.models.UserPreferences;
import com.example.wolfitness.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private LinearLayout homeNav, workoutNav, profileNav;
    private ImageView homeIcon, workoutIcon, profileIcon;
    private SwitchCompat popupNotificationToggle;
    private TextView userNameTextView, userGoalTextView;
    private TextView userHeightTextView, userWeightTextView, userAgeTextView;
    private TextView bmiValueTextView, bmiStatusTextView;
    private View bmiIndicator;
    private LinearLayout personalDataLayout, achievementLayout, activityHistoryLayout, workoutProgressLayout;
    private LinearLayout contactUsLayout, privacyPolicyLayout, settingsLayout, logoutLayout;
    private ImageView backButton;
    private MaterialButton editButton;

    // Constants for BMI categories
    private static final float BMI_UNDERWEIGHT = 18.5f;
    private static final float BMI_NORMAL = 25f;
    private static final float BMI_OVERWEIGHT = 30f;

    // SharedPreferences constants (for backward compatibility)
    private static final String PREFS_NAME = "WoFitnessPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "date_of_birth";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";

    // Date formatter
    private SimpleDateFormat dateFormatter;

    // Repository
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Repository
        userRepository = new UserRepository();

        // Check if user is logged in
        if (userRepository.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize date formatter
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        initViews();

        // Add logout option to the layout
        addLogoutOption();

        // Load user data
        loadUserData();

        setupClickListeners();
        setupNavigationBar();
    }

    private void initViews() {
        // Bottom navigation
        homeNav = findViewById(R.id.home_nav);
        workoutNav = findViewById(R.id.workout_nav);
        profileNav = findViewById(R.id.profile_nav);

        // Navigation icons
        homeIcon = findViewById(R.id.home_icon);
        workoutIcon = findViewById(R.id.workout_icon);
        profileIcon = findViewById(R.id.profile_icon);

        // Back button
        backButton = findViewById(R.id.back_button);

        // User info views
        userNameTextView = findViewById(R.id.user_name);
        userGoalTextView = findViewById(R.id.user_goal);
        userHeightTextView = findViewById(R.id.user_height);
        userWeightTextView = findViewById(R.id.user_weight);
        userAgeTextView = findViewById(R.id.user_age);

        // BMI views
        bmiValueTextView = findViewById(R.id.bmi_value);
        bmiStatusTextView = findViewById(R.id.bmi_status);
        bmiIndicator = findViewById(R.id.bmi_indicator);

        // Settings layouts
        personalDataLayout = findViewById(R.id.personal_data_layout);
        achievementLayout = findViewById(R.id.achievement_layout);
        activityHistoryLayout = findViewById(R.id.activity_history_layout);
        workoutProgressLayout = findViewById(R.id.workout_progress_layout);
        contactUsLayout = findViewById(R.id.contact_us_layout);
        privacyPolicyLayout = findViewById(R.id.privacy_policy_layout);
        settingsLayout = findViewById(R.id.settings_layout);

        // Toggle
        popupNotificationToggle = findViewById(R.id.popup_notification_toggle);

        // Edit button
        editButton = findViewById(R.id.edit_button);
    }

    /**
     * Add a logout option to the settings section
     */
    private void addLogoutOption() {
        // Find the container that holds the settings
        LinearLayout otherSettingsContainer = (LinearLayout) settingsLayout.getParent();

        // Create a divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1)); // 1dp height
        divider.setBackgroundColor(getResources().getColor(R.color.gray));

        // Create the logout layout similar to other settings options
        logoutLayout = new LinearLayout(this);
        logoutLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        logoutLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        logoutLayout.setOrientation(LinearLayout.HORIZONTAL);
        logoutLayout.setPadding(0, 12, 0, 12); // Same padding as other options

        // Create and add the logout icon
        ImageView logoutIcon = new ImageView(this);
        logoutIcon.setLayoutParams(new LinearLayout.LayoutParams(
                24, 24)); // 24dp x 24dp
        logoutIcon.setImageResource(R.drawable.ic_logout); // Add a logout icon to your drawable resources
        logoutLayout.addView(logoutIcon);

        // Create and add the "Logout" text
        TextView logoutText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1); // weight = 1
        textParams.setMarginStart(16); // 16dp start margin
        logoutText.setLayoutParams(textParams);
        logoutText.setText("Logout");
        logoutText.setTextColor(Color.parseColor("#FF5252")); // Red text for logout
        logoutText.setTextSize(16); // 16sp text size
        logoutLayout.addView(logoutText);

        // Add the divider and logout layout to the container
        otherSettingsContainer.addView(divider);
        otherSettingsContainer.addView(logoutLayout);

        // Set click listener for logout
        logoutLayout.setOnClickListener(v -> {
            // Sign out using repository
            userRepository.signOut();

            // Clear SharedPreferences (optional)
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.apply();

            // Show toast message
            Toast.makeText(ProfileActivity.this, "You have been logged out", Toast.LENGTH_SHORT).show();

            // Return to Login screen
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("LOGGED_OUT", true);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Load user data
     */
    private void loadUserData() {
        String userId = userRepository.getCurrentUser().getUid();

        userRepository.getUserById(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    User user = User.fromDocument(documentSnapshot);

                    if (user != null) {
                        // Update UI with data from user model
                        if (user.getName() != null && !user.getName().isEmpty()) {
                            userNameTextView.setText(user.getName());
                        }

                        if (user.getGoal() != null && !user.getGoal().isEmpty()) {
                            userGoalTextView.setText(user.getGoal() + " Program");
                        }

                        if (user.getHeight() != null && !user.getHeight().isEmpty()) {
                            userHeightTextView.setText(user.getHeight() + "cm");
                        }

                        if (user.getWeight() != null && !user.getWeight().isEmpty()) {
                            userWeightTextView.setText(user.getWeight() + "kg");
                        }

                        // Show age
                        if (user.getAge() > 0) {
                            userAgeTextView.setText(user.getAge() + "yo");
                        } else if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
                            int age = calculateAge(user.getDateOfBirth());
                            userAgeTextView.setText(age + "yo");
                        }

                        // Calculate BMI if we have both height and weight
                        if (user.getHeight() != null && !user.getHeight().isEmpty() &&
                                user.getWeight() != null && !user.getWeight().isEmpty()) {
                            calculateAndSetBMI(user.getHeight(), user.getWeight());
                        }

                        // Save to SharedPreferences for backward compatibility
                        saveUserDataToPrefs(user.getName(), user.getGoal(), user.getHeight(),
                                user.getWeight(), user.getDateOfBirth());

                        // Load preferences
                        loadUserPreferences(userId);
                    } else {
                        // Fall back to SharedPreferences data
                        setupUserDataFromPrefs();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data: " + e.getMessage());
                    // Fall back to SharedPreferences data
                    setupUserDataFromPrefs();
                });
    }

    /**
     * Load user preferences
     */
    private void loadUserPreferences(String userId) {
        userRepository.getUserPreferences(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    UserPreferences preferences = UserPreferences.fromDocument(documentSnapshot);

                    if (preferences != null) {
                        // Set notification toggle from preferences
                        popupNotificationToggle.setChecked(preferences.isNotificationsEnabled());
                    } else {
                        // Default to enabled
                        popupNotificationToggle.setChecked(true);
                    }
                })
                .addOnFailureListener(e -> {
                    // Default to SharedPreferences
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    popupNotificationToggle.setChecked(settings.getBoolean("notifications_enabled", true));
                });
    }

    /**
     * Save user data to SharedPreferences for backward compatibility
     */
    private void saveUserDataToPrefs(String name, String goal, String height, String weight, String dob) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        if (name != null && !name.isEmpty()) {
            editor.putString(KEY_USERNAME, name);
        }

        if (dob != null && !dob.isEmpty()) {
            editor.putString(KEY_DOB, dob);
        }

        if (height != null && !height.isEmpty()) {
            editor.putString(KEY_HEIGHT, height);
        }

        if (weight != null && !weight.isEmpty()) {
            editor.putString(KEY_WEIGHT, weight);
        }

        editor.apply();
    }

    /**
     * Set up user data from SharedPreferences (fallback)
     */
    private void setupUserDataFromPrefs() {
        // Get user data from shared preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get username or use default
        String userName = settings.getString(KEY_USERNAME, "Christine Machhour");
        userNameTextView.setText(userName);

        // Set user goal (this could be saved in preferences too)
        userGoalTextView.setText("Lose Fat Program");

        // Get height with unit or use default
        String height = settings.getString(KEY_HEIGHT, "170");
        userHeightTextView.setText(height + "cm");

        // Get weight with unit or use default
        String weight = settings.getString(KEY_WEIGHT, "60");
        userWeightTextView.setText(weight + "kg");

        // Calculate age from date of birth
        String dob = settings.getString(KEY_DOB, "");
        int age = calculateAge(dob);
        userAgeTextView.setText(age + "yo");

        // Calculate BMI and set BMI value and status
        calculateAndSetBMI(height, weight);

        // Set notification toggle state
        popupNotificationToggle.setChecked(settings.getBoolean("notifications_enabled", true));
    }

    /**
     * Calculate BMI and set the appropriate values and status
     */
    private void calculateAndSetBMI(String heightStr, String weightStr) {
        try {
            float height = Float.parseFloat(heightStr) / 100; // Convert cm to meters
            float weight = Float.parseFloat(weightStr);

            // BMI formula: weight (kg) / (height (m) * height (m))
            float bmi = weight / (height * height);

            // Format BMI to one decimal place and replace decimal point with comma
            String bmiFormatted = String.format(Locale.US, "%.1f", bmi).replace(".", ",");
            bmiValueTextView.setText(bmiFormatted);

            // Set BMI status and color based on calculated value
            String status;
            int chartColorResId;

            if (bmi < BMI_UNDERWEIGHT) {
                status = "You are underweight";
                chartColorResId = R.color.bmi_underweight;
                updateBmiChartColor("#FFC107"); // Yellow for underweight
            } else if (bmi < BMI_NORMAL) {
                status = "You have a normal weight";
                chartColorResId = R.color.bmi_normal;
                updateBmiChartColor("#00E676"); // Green for normal
            } else if (bmi < BMI_OVERWEIGHT) {
                status = "You are overweight";
                chartColorResId = R.color.bmi_overweight;
                updateBmiChartColor("#FF9800"); // Orange for overweight
            } else {
                status = "You need to lose weight";
                chartColorResId = R.color.bmi_obese;
                updateBmiChartColor("#F44336"); // Red for obese
            }

            bmiStatusTextView.setText(status);

        } catch (NumberFormatException e) {
            // If there's an error parsing numbers, use default values
            bmiValueTextView.setText("20,1");
            bmiStatusTextView.setText("You have a normal weight");
            updateBmiChartColor("#00E676"); // Default green
        }
    }

    /**
     * Update the BMI chart color
     */
    private void updateBmiChartColor(String colorHex) {
        try {
            // Update the BMI indicator color
            if (bmiIndicator != null) {
                GradientDrawable drawable = (GradientDrawable) bmiIndicator.getBackground();
                drawable.setColor(Color.parseColor(colorHex));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If we can't update the color, just continue
        }
    }

    private void setupClickListeners() {
        // Set back button click listener to navigate to home
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finish this activity to prevent stacking
            overridePendingTransition(0, 0);
        });

        // Set edit button click listener to open the register/edit activity
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
            // Pass current username to maintain consistency
            intent.putExtra("USER_NAME", userNameTextView.getText().toString());
            intent.putExtra("IS_EDITING", true); // Flag to indicate editing mode
            startActivity(intent);
        });

        // Set click listeners for all menu items
        personalDataLayout.setOnClickListener(v -> navigateToSection("Personal Data"));
        achievementLayout.setOnClickListener(v -> navigateToSection("Achievement"));
        activityHistoryLayout.setOnClickListener(v -> navigateToSection("Activity History"));
        workoutProgressLayout.setOnClickListener(v -> navigateToSection("Workout Progress"));
        contactUsLayout.setOnClickListener(v -> navigateToSection("Contact Us"));
        privacyPolicyLayout.setOnClickListener(v -> navigateToSection("Privacy Policy"));
        settingsLayout.setOnClickListener(v -> navigateToSection("Settings"));

        // Set notification toggle listener
        popupNotificationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();

            // Update user preferences
            String userId = userRepository.getCurrentUser().getUid();
            userRepository.updateUserPreferencesField(userId, "notificationsEnabled", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Notification preference saved");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving notification preference: " + e.getMessage());
                    });

            // Also save to SharedPreferences for backward compatibility
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });
    }

    private void setupNavigationBar() {
        // Set profile icon as active
        updateNavIcons(R.id.profile_icon);

        // Set click listeners for navigation
        homeNav.setOnClickListener(v -> {
            updateNavIcons(R.id.home_icon);
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Prevent activity stacking
            overridePendingTransition(0, 0);
        });

        workoutNav.setOnClickListener(v -> {
            updateNavIcons(R.id.workout_icon);
            Toast.makeText(this, "Workout screen coming soon", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProfileActivity.this, WorkoutActivity.class);
            // startActivity(intent);
            // finish();
            // overridePendingTransition(0, 0);
        });

        profileNav.setOnClickListener(v -> {
            // Already on profile screen
            updateNavIcons(R.id.profile_icon);
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
     * Calculate age from date of birth
     */
    private int calculateAge(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return 22; // Default age if no date provided
        }

        try {
            Date birthDate = dateFormatter.parse(dateOfBirth);
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            // Check if birthday has occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 22; // Default age if parsing fails
        }
    }

    private void navigateToSection(String section) {
        // This method handles navigation to different sections
        Toast.makeText(this, section + " clicked", Toast.LENGTH_SHORT).show();

        // Implement actual navigation to other activities when they're created
        // switch (section) {
        //     case "Personal Data":
        //         startActivity(new Intent(this, PersonalDataActivity.class));
        //         break;
        //     case "Achievement":
        //         startActivity(new Intent(this, AchievementActivity.class));
        //         break;
        //     // and so on for other sections
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity (e.g., after editing)
        loadUserData();
    }
}