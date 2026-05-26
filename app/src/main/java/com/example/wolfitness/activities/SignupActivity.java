package com.example.wolfitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.wolfitness.R;
import com.example.wolfitness.models.AuthState;
import com.example.wolfitness.models.User;
import com.example.wolfitness.models.UserPreferences;
import com.example.wolfitness.repositories.UserRepository;

public class SignupActivity extends AppCompatActivity {
    private Button signUpButton;
    private TextView loginLink;
    private EditText nameEditText, phoneEditText, emailEditText, passwordEditText;
    private ImageView passwordToggle;
    private boolean passwordVisible = false;

    // Use repository instead of direct Firebase Auth reference
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize repository
        userRepository = new UserRepository();

        // Initialize views
        nameEditText = findViewById(R.id.name_input);
        phoneEditText = findViewById(R.id.phone_input);
        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);
        signUpButton = findViewById(R.id.btn_sign_up);
        loginLink = findViewById(R.id.login_link);
        passwordToggle = findViewById(R.id.password_toggle);

        // Password visibility toggle
        passwordToggle.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                // Show password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_off);
            } else {
                // Hide password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye);
            }
            // Move cursor to end of text
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Set digits-only input for phone field
        phoneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // Add digit-only filter as extra protection
        phoneEditText.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        // Only allow digits 0-9
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });

        signUpButton.setOnClickListener(v -> {
            if (!validateInputs()) {
                return;
            }

            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Show loading state
            signUpButton.setEnabled(false);
            signUpButton.setText("Creating Account...");

            // Use repository to register user
            userRepository.registerUser(email, password)
                    .addOnCompleteListener(task -> {
                        // Reset button state
                        signUpButton.setEnabled(true);
                        signUpButton.setText("Sign Up");

                        if (task.isSuccessful()) {
                            // Get the user ID
                            String userId = userRepository.getCurrentUser().getUid();

                            // Create a User model object
                            User newUser = new User(userId, name, email, phone);
                            newUser.setCreatedAt(System.currentTimeMillis());
                            newUser.setLastLoginAt(System.currentTimeMillis());
                            newUser.setNotificationsEnabled(true); // Default setting

                            // Also create UserPreferences
                            UserPreferences preferences = new UserPreferences(userId);

                            // Save the user to Firestore
                            userRepository.createUser(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Save user preferences
                                        userRepository.saveUserPreferences(preferences)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    // Update auth state
                                                    userRepository.setAuthState(new AuthState(
                                                            AuthState.STATE_PROFILE_INCOMPLETE, userId));

                                                    Toast.makeText(SignupActivity.this, "Registration successful!",
                                                            Toast.LENGTH_SHORT).show();

                                                    // Proceed to RegisterActivity
                                                    Intent intent = new Intent(SignupActivity.this, RegisterActivity.class);
                                                    intent.putExtra("USER_NAME", name);
                                                    intent.putExtra("USER_EMAIL", email);
                                                    intent.putExtra("USER_PHONE", phone);
                                                    startActivity(intent);
                                                    finish(); // Close the signup activity
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(SignupActivity.this,
                                                            "Failed to save user preferences: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignupActivity.this,
                                                "Failed to save user data: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // If sign in fails, display a message
                            Toast.makeText(SignupActivity.this, "Registration failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login link - simply navigate to LoginActivity without any automatic login
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            // Optional: finish this activity to remove it from the back stack
            finish();
        });
    }

    // Validate all inputs
    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = true;

        // Validate name
        if (name.isEmpty()) {
            nameEditText.setError("Please enter your name");
            isValid = false;
        }

        // Validate phone
        if (phone.isEmpty()) {
            phoneEditText.setError("Please enter your phone number");
            isValid = false;
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.setError("Please enter your email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordEditText.setError("Please enter a password");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    // Helper method to validate email using Android's patterns
    private boolean isValidEmail(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}