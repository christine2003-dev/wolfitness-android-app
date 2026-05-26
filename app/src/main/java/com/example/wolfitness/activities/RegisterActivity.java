package com.example.wolfitness.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wolfitness.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Spinner genderSpinner;
    private EditText dateOfBirthEditText;
    private EditText weightEditText;
    private EditText heightEditText;
    private Button nextButton;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private String userName;
    private String userEmail;
    private String userPhone;
    private boolean isEditing = false;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // SharedPreferences constants (keeping for backward compatibility)
    private static final String PREFS_NAME = "WoFitnessPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "date_of_birth";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get user data from previous activity
        userName = getIntent().getStringExtra("USER_NAME");
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        userPhone = getIntent().getStringExtra("USER_PHONE");

        if (userName == null || userName.isEmpty()) {
            userName = "User"; // Default value
        }

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login or create an account first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Check if we're in editing mode
        isEditing = getIntent().getBooleanExtra("IS_EDITING", false);

        // Initialize views
        genderSpinner = findViewById(R.id.gender_spinner);
        dateOfBirthEditText = findViewById(R.id.date_of_birth_edit_text);
        weightEditText = findViewById(R.id.weight_edit_text);
        heightEditText = findViewById(R.id.height_edit_text);
        nextButton = findViewById(R.id.next_button);

        // Change button text if in editing mode
        if (isEditing) {
            nextButton.setText("Save Changes");
        }

        // Initialize calendar and date formatter
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        // Set up date picker dialog for date of birth
        setupDatePicker();

        // Load existing data if in editing mode
        if (isEditing) {
            loadUserDataFromFirestore();
        }

        // Next/Save button click listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    // Save user data to Firestore
                    saveUserDataToFirestore();
                }
            }
        });
    }

    /**
     * Load user data from Firestore
     */
    private void loadUserDataFromFirestore() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set gender selection if available
                        String gender = documentSnapshot.getString("gender");
                        if (gender != null && !gender.isEmpty()) {
                            for (int i = 0; i < genderSpinner.getAdapter().getCount(); i++) {
                                if (genderSpinner.getAdapter().getItem(i).toString().equals(gender)) {
                                    genderSpinner.setSelection(i);
                                    break;
                                }
                            }
                        }

                        // Set date of birth if available
                        String dob = documentSnapshot.getString("dateOfBirth");
                        if (dob != null && !dob.isEmpty()) {
                            dateOfBirthEditText.setText(dob);
                            try {
                                Date date = dateFormatter.parse(dob);
                                calendar.setTime(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // Set weight if available
                        String weight = documentSnapshot.getString("weight");
                        if (weight != null && !weight.isEmpty()) {
                            weightEditText.setText(weight);
                        }

                        // Set height if available
                        String height = documentSnapshot.getString("height");
                        if (height != null && !height.isEmpty()) {
                            heightEditText.setText(height);
                        }

                        // Update userName from Firestore if needed
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            userName = name;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Fall back to SharedPreferences
                    loadExistingUserData();
                });
    }

    /**
     * Load existing user data from SharedPreferences (legacy support)
     */
    private void loadExistingUserData() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set gender selection if available
        String gender = settings.getString(KEY_GENDER, "");
        if (!gender.isEmpty()) {
            for (int i = 0; i < genderSpinner.getAdapter().getCount(); i++) {
                if (genderSpinner.getAdapter().getItem(i).toString().equals(gender)) {
                    genderSpinner.setSelection(i);
                    break;
                }
            }
        }

        // Set date of birth if available
        String dob = settings.getString(KEY_DOB, "");
        if (!dob.isEmpty()) {
            dateOfBirthEditText.setText(dob);
            try {
                Date date = dateFormatter.parse(dob);
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Set weight if available
        String weight = settings.getString(KEY_WEIGHT, "");
        if (!weight.isEmpty()) {
            weightEditText.setText(weight);
        }

        // Set height if available
        String height = settings.getString(KEY_HEIGHT, "");
        if (!height.isEmpty()) {
            heightEditText.setText(height);
        }
    }

    private void setupDatePicker() {
        // When the date field is clicked, show the date picker dialog
        dateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        // Set default date to 18 years ago if no date selected
        Calendar defaultDate = Calendar.getInstance();
        if (dateOfBirthEditText.getText().toString().isEmpty()) {
            defaultDate.add(Calendar.YEAR, -18);
        } else {
            defaultDate = calendar; // Use the stored date if available
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme, // Custom theme for the date picker
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateField();
                    }
                },
                defaultDate.get(Calendar.YEAR),
                defaultDate.get(Calendar.MONTH),
                defaultDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set minimum date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void updateDateField() {
        dateOfBirthEditText.setText(dateFormatter.format(calendar.getTime()));
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Check if date of birth is selected
        if (dateOfBirthEditText.getText().toString().trim().isEmpty()) {
            dateOfBirthEditText.setError("Please select your date of birth");
            isValid = false;
        }

        // Check if weight is entered
        if (weightEditText.getText().toString().trim().isEmpty()) {
            weightEditText.setError("Please enter your weight");
            isValid = false;
        }

        // Check if height is entered
        if (heightEditText.getText().toString().trim().isEmpty()) {
            heightEditText.setError("Please enter your height");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Save user data to Firestore
     */
    private void saveUserDataToFirestore() {
        String userId = auth.getCurrentUser().getUid();

        // Show loading state
        nextButton.setEnabled(false);
        nextButton.setText(isEditing ? "Saving..." : "Next...");

        // Create a user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        userData.put("gender", genderSpinner.getSelectedItem().toString());
        userData.put("dateOfBirth", dateOfBirthEditText.getText().toString());
        userData.put("weight", weightEditText.getText().toString().replaceAll("[^0-9.]", ""));
        userData.put("height", heightEditText.getText().toString().replaceAll("[^0-9.]", ""));

        // Add email and phone if available
        if (userEmail != null && !userEmail.isEmpty()) {
            userData.put("email", userEmail);
        }

        if (userPhone != null && !userPhone.isEmpty()) {
            userData.put("phone", userPhone);
        }

        // Calculate age and add it
        String dob = dateOfBirthEditText.getText().toString();
        int age = calculateAge(dob);
        userData.put("age", age);

        // Add timestamp
        userData.put("updatedAt", new Date());

        // Save to Firestore
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Also save user stats for tracking history
                    saveUserStatsToFirestore(userId, userData);

                    Toast.makeText(RegisterActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();

                    // Also save to SharedPreferences for backward compatibility
                    saveUserDataToPrefs();

                    if (isEditing) {
                        // Return to Profile if editing
                        finish();
                    } else {
                        // Go to GoalSelectionActivity first, which will then go to SuccessRegisterActivity
                        Intent intent = new Intent(RegisterActivity.this, GoalSelectionActivity.class);
                        intent.putExtra("USER_NAME", userName);
                        startActivity(intent);
                        finish(); // Finish this activity so user can't go back
                    }

                    // Re-enable button
                    nextButton.setEnabled(true);
                    nextButton.setText(isEditing ? "Save Changes" : "Next");
                })
                .addOnFailureListener(e -> {
                    // Re-enable button
                    nextButton.setEnabled(true);
                    nextButton.setText(isEditing ? "Save Changes" : "Next");

                    Toast.makeText(RegisterActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Save user stats to track history
     */
    private void saveUserStatsToFirestore(String userId, Map<String, Object> userData) {
        try {
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("userId", userId);

            // Get weight and height
            String weightStr = (String) userData.get("weight");
            String heightStr = (String) userData.get("height");

            if (weightStr != null && heightStr != null) {
                float weight = Float.parseFloat(weightStr);
                float height = Float.parseFloat(heightStr);
                float heightInMeters = height / 100f;
                float bmi = weight / (heightInMeters * heightInMeters);

                statsData.put("weight", weight);
                statsData.put("height", height);
                statsData.put("bmi", bmi);
                statsData.put("recordedAt", new Date());

                // Save stats
                db.collection("user_stats").add(statsData)
                        .addOnSuccessListener(documentReference -> {
                            // Stats saved successfully
                        })
                        .addOnFailureListener(e -> {
                            // Failed to save stats, but we can continue anyway
                        });
            }
        } catch (Exception e) {
            // Ignore errors in stats - they're not critical
        }
    }

    /**
     * Save user data to SharedPreferences (for backward compatibility)
     */
    private void saveUserDataToPrefs() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Save username
        editor.putString(KEY_USERNAME, userName);

        // Save gender
        String gender = genderSpinner.getSelectedItem().toString();
        editor.putString(KEY_GENDER, gender);

        // Save date of birth
        String dob = dateOfBirthEditText.getText().toString();
        editor.putString(KEY_DOB, dob);

        // Save weight (remove any non-numeric characters)
        String weight = weightEditText.getText().toString().replaceAll("[^0-9.]", "");
        editor.putString(KEY_WEIGHT, weight);

        // Save height (remove any non-numeric characters)
        String height = heightEditText.getText().toString().replaceAll("[^0-9.]", "");
        editor.putString(KEY_HEIGHT, height);

        // Apply all changes
        editor.apply();
    }

    /**
     * Calculate age from date of birth
     */
    private int calculateAge(String dateOfBirth) {
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
            return 0;
        }
    }
}