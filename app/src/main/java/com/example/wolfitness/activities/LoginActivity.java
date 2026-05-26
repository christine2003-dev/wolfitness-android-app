package com.example.wolfitness.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wolfitness.R;
import com.example.wolfitness.models.AuthState;
import com.example.wolfitness.models.User;
import com.example.wolfitness.repositories.UserRepository;

public class LoginActivity extends AppCompatActivity {

    TextView forgotPasswordLink;
    Button loginButton;
    EditText emailEditText, passwordEditText;
    ImageView passwordToggle;
    boolean passwordVisible = false;

    // Repository for user operations
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UserRepository
        userRepository = new UserRepository();

        // Initialize views
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        passwordToggle = findViewById(R.id.password_toggle);

        if (forgotPasswordLink != null) {
            forgotPasswordLink.setPaintFlags(forgotPasswordLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

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

        // Forgot password functionality
        forgotPasswordLink.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Please enter your email to reset password");
                return;
            }

            // Show loading
            forgotPasswordLink.setEnabled(false);

            // Send password reset email
            userRepository.resetPassword(email)
                    .addOnCompleteListener(task -> {
                        forgotPasswordLink.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = "Failed to send reset email";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        TextView signUpText = findViewById(R.id.sign_up_text);
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Set the OnClickListener for the login button
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty()) {
                emailEditText.setError("Please enter your email");
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Please enter your password");
                return;
            }

            // Show loading state (optional)
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            // Sign in with Firebase using repository
            userRepository.loginUser(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        // Re-enable button
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");

                        if (task.isSuccessful()) {
                            // Sign in success
                            String userId = userRepository.getCurrentUser().getUid();

                            // Update last login time
                            userRepository.updateLastLogin(userId);

                            // Check user profile completion status
                            userRepository.getUserById(userId).addOnSuccessListener(userDoc -> {
                                User user = User.fromDocument(userDoc);
                                AuthState authState;

                                if (user == null) {
                                    // User document doesn't exist in Firestore
                                    authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                                    navigateBasedOnAuthState(authState);
                                    return;
                                }

                                // Check if profile is complete
                                if (user.getHeight() == null || user.getWeight() == null ||
                                        user.getGender() == null || user.getDateOfBirth() == null) {
                                    authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                                } else {
                                    // Go directly to home page, skipping goal selection
                                    authState = new AuthState(AuthState.STATE_LOGGED_IN, userId);
                                }

                                // Store auth state and navigate accordingly
                                userRepository.setAuthState(authState);
                                navigateBasedOnAuthState(authState);
                            }).addOnFailureListener(e -> {
                                // Error fetching user, assume profile incomplete
                                AuthState authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                                userRepository.setAuthState(authState);
                                navigateBasedOnAuthState(authState);
                            });

                            Toast.makeText(LoginActivity.this, "Login successful!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message
                            Toast.makeText(LoginActivity.this, "Login failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                            // Set logged out state
                            userRepository.setAuthState(new AuthState(AuthState.STATE_LOGGED_OUT));
                        }
                    });
        });

        // Check if the user is trying to log in after being logged out
        if (getIntent().getBooleanExtra("LOGGED_OUT", false)) {
            Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBasedOnAuthState(AuthState authState) {
        Intent intent;

        if (authState.isProfileComplete()) {
            // Redirect to Home activity, skipping goal selection
            intent = new Intent(LoginActivity.this, HomeActivity.class);
        } else {
            // Profile incomplete
            intent = new Intent(LoginActivity.this, RegisterActivity.class);
            // Pass user data if we have it
            if (userRepository.getCurrentUser() != null) {
                intent.putExtra("USER_EMAIL", userRepository.getCurrentUser().getEmail());
            }
        }

        startActivity(intent);
        finish(); // Close the LoginActivity after navigating
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in and navigate accordingly
        if (userRepository.getCurrentUser() != null) {
            AuthState cachedState = userRepository.getAuthState();
            if (cachedState != null && cachedState.isLoggedIn()) {
                navigateBasedOnAuthState(cachedState);
            } else {
                // Get latest status
                String userId = userRepository.getCurrentUser().getUid();
                userRepository.getUserById(userId).addOnSuccessListener(userDoc -> {
                    User user = User.fromDocument(userDoc);
                    AuthState authState;

                    if (user == null) {
                        authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                    } else if (!user.hasCompleteProfile()) {
                        authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                    } else {
                        // Skip goal selection
                        authState = new AuthState(AuthState.STATE_LOGGED_IN, userId);
                    }

                    userRepository.setAuthState(authState);
                    navigateBasedOnAuthState(authState);
                }).addOnFailureListener(e -> {
                    // Error fetching user, assume they need to complete profile
                    AuthState authState = new AuthState(AuthState.STATE_PROFILE_INCOMPLETE, userId);
                    userRepository.setAuthState(authState);
                    navigateBasedOnAuthState(authState);
                });
            }
        }
    }
}