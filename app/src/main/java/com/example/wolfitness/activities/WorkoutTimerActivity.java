package com.example.wolfitness.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wolfitness.R;
import com.example.wolfitness.models.Exercise;
import com.example.wolfitness.models.User;
import com.example.wolfitness.models.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.HashMap;
public class WorkoutTimerActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutTimerActivity";

    // UI Components
    private TextView timerText, exerciseNameTextView, exerciseDifficultyTextView, exerciseDescriptionTextView;
    private EditText editTextHours, editTextMinutes, editTextSeconds;
    private Button buttonStart, buttonPause, watchNowButton;
    private ImageButton backButton;

    // Timer Logic
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis = 0;

    // Sound & Vibration
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference completedWorkoutsRef;

    // Data
    private String userName = "Unknown User";
    private String workoutId, exerciseId, workoutName = "Workout", duration = "0 mins";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_timer);

        initializeViews();
        initializeSoundAndVibration();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get intent extras
        workoutId = getIntent().getStringExtra("WORKOUT_ID");
        exerciseId = getIntent().getStringExtra("EXERCISE_ID");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            completedWorkoutsRef = db.collection("completed_workouts");
            fetchUserData(currentUser.getUid());
        }

        if (exerciseId != null) {
            loadExerciseData();
        } else if (workoutId != null) {
            loadWorkoutData();
        } else {
            updateExerciseUI("Default Workout", "Easy", "Set your timer and start your workout");
        }

        setupClickListeners();
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timerText);
        exerciseNameTextView = findViewById(R.id.exerciseNameTextView);
        exerciseDifficultyTextView = findViewById(R.id.exerciseDifficultyTextView);
        exerciseDescriptionTextView = findViewById(R.id.exerciseDescriptionTextView);
        editTextHours = findViewById(R.id.editTextHours);
        editTextMinutes = findViewById(R.id.editTextMinutes);
        editTextSeconds = findViewById(R.id.editTextSeconds);
        buttonStart = findViewById(R.id.buttonStart);
        buttonPause = findViewById(R.id.buttonPause);
        watchNowButton = findViewById(R.id.watchNowButton);
        backButton = findViewById(R.id.backButton);
    }

    private void initializeSoundAndVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.completion_sound);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sound", e);
        }
    }

    private void setupClickListeners() {
        buttonStart.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            }
        });

        buttonPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            }
        });

        watchNowButton.setOnClickListener(v ->
                openVideoForExercise(exerciseNameTextView.getText().toString())
        );

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void startTimer() {
        int hours = parseInt(editTextHours.getText().toString());
        int minutes = parseInt(editTextMinutes.getText().toString());
        int seconds = parseInt(editTextSeconds.getText().toString());

        timeLeftInMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L;

        if (timeLeftInMillis <= 0) {
            Toast.makeText(this, "Please set a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            public void onFinish() {
                isRunning = false;
                timerText.setText("Done!");
                playCompletionSound();
                saveWorkoutToFirestore();
                startActivity(new Intent(WorkoutTimerActivity.this, WorkoutCompletionActivity.class));
                finish();
            }
        }.start();

        isRunning = true;
        buttonStart.setText("Running...");
        buttonStart.setEnabled(false);
        buttonPause.setEnabled(true);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        buttonStart.setText("Resume");
        buttonStart.setEnabled(true);
        buttonPause.setEnabled(false);
    }

    private void updateTimerText() {
        int totalSeconds = (int) (timeLeftInMillis / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeFormatted;
        if (hours > 0) {
            timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeFormatted = String.format("%02d:%02d", minutes, seconds);
        }

        timerText.setText(timeFormatted);
    }

    private void playCompletionSound() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            } else {
                MediaPlayer fallback = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                if (fallback != null) {
                    fallback.start();
                }
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(new long[]{0, 500, 200, 500}, -1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound or vibration", e);
        }
    }

    private void fetchUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = User.fromDocument(documentSnapshot);
                    if (user != null && user.getName() != null) {
                        userName = user.getName();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user data", e));
    }

    private void loadWorkoutData() {
        db.collection("workouts")
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Workout workout = documentSnapshot.toObject(Workout.class);
                    if (workout != null) {
                        workoutName = workout.getName();
                        duration = workout.getDuration();
                        String description = String.format("Workout with %d exercises. Duration: %s. Burn: %s cal.",
                                workout.getTotalExercises(), workout.getDuration(), workout.getCaloriesBurn());
                        updateExerciseUI(workoutName, "Various", description);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Workout data fetch failed", e));
    }

    private void loadExerciseData() {
        db.collection("exercises")
                .document(exerciseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Exercise exercise = documentSnapshot.toObject(Exercise.class);
                    if (exercise != null) {
                        workoutName = exercise.getName();
                        duration = exercise.getDuration() != null ? exercise.getDuration() : exercise.getRepetitions();
                        String difficulty = exercise.getDifficulty() != null ? exercise.getDifficulty() : "Medium";
                        if (exercise.getSetNumber() > 0) {
                            difficulty += " | Set " + exercise.getSetNumber();
                        }
                        if (duration != null && !duration.isEmpty()) {
                            difficulty += " | " + duration;
                        }
                        String description = exercise.getDescription() != null ? exercise.getDescription() : "Complete this exercise.";
                        updateExerciseUI(workoutName, difficulty, description);
                        autoFillTimerFromDuration(duration);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Exercise data fetch failed", e));
    }

    private void updateExerciseUI(String name, String difficulty, String description) {
        exerciseNameTextView.setText(name);
        exerciseDifficultyTextView.setText(difficulty);
        exerciseDescriptionTextView.setText(description);
    }

    private void autoFillTimerFromDuration(String durationStr) {
        if (durationStr == null || !durationStr.contains(":")) return;
        try {
            String[] parts = durationStr.split(":");
            switch (parts.length) {
                case 1:
                    editTextSeconds.setText(parts[0]);
                    break;
                case 2:
                    editTextMinutes.setText(parts[0]);
                    editTextSeconds.setText(parts[1]);
                    break;
                case 3:
                    editTextHours.setText(parts[0]);
                    editTextMinutes.setText(parts[1]);
                    editTextSeconds.setText(parts[2]);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to autofill timer", e);
        }
    }

    private void saveWorkoutToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || completedWorkoutsRef == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        int hours = parseInt(editTextHours.getText().toString());
        int minutes = parseInt(editTextMinutes.getText().toString());
        int seconds = parseInt(editTextSeconds.getText().toString());
        String userDurationFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        Map<String, Object> workoutData = new HashMap<>();
        workoutData.put("userName", userName);
        workoutData.put("exerciseName", workoutName);
        workoutData.put("defaultDuration", duration);
        workoutData.put("userDuration", userDurationFormatted);
        workoutData.put("timestamp", System.currentTimeMillis());

        if (workoutId != null) workoutData.put("workoutId", workoutId);
        if (exerciseId != null) workoutData.put("exerciseId", exerciseId);

        completedWorkoutsRef.add(workoutData)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Workout saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save workout", e));
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void openVideoForExercise(String exerciseName) {
        if (exerciseName == null || exerciseName.isEmpty()) {
            Toast.makeText(this, "Exercise name not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String videoUrl = getVideoUrlForExercise(exerciseName.trim());
        Log.d(TAG, "Opening video: " + videoUrl);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening video: " + e.getMessage());
            Toast.makeText(this, "Unable to open video. Please check browser or YouTube app.", Toast.LENGTH_LONG).show();
        }
    }

    private String getVideoUrlForExercise(String name) {
        switch (name.toLowerCase()) {
            case "warm up":
                return "https://www.youtube.com/watch?v=R0mMyV5OtcM ";
            case "jumping jack":
                return "https://www.youtube.com/watch?v=c4DAnQ6DtF8 ";
            case "skipping":
                return "https://www.youtube.com/watch?v=BYMyW8epc8U ";
            case "incline push-ups":
                return "https://www.youtube.com/watch?v=yAbg3_pJKvw ";
            case "push-ups":
                return "https://www.youtube.com/watch?v=HCapLWaJ_qY ";
            case "cobra stretch":
                return "https://www.youtube.com/watch?v=JDcdhTuycOI ";
            case "squats":
                return "https://www.youtube.com/watch?v=GrnV8VIhS-I ";
            case "arm raises":
                return "https://www.youtube.com/watch?v=Bqvmyni_sKQ ";
            case "rest and drink":
                return "https://www.youtube.com/watch?v=XMuK0zaqtGY ";
            default:
                return "https://www.youtube.com/watch?v=c4DAnQ6DtF8 "; // Default video
        }
    }

    @Override
    public void onBackPressed() {
        if (isRunning) {
            pauseTimer();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (isRunning) {
            pauseTimer(); // Optional: auto-pause when app goes into background
        }
        super.onPause();
    }
}