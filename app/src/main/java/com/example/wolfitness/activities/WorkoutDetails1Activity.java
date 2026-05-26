package com.example.wolfitness.activities;

import static android.content.Intent.getIntent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wolfitness.R;
import com.example.wolfitness.models.Exercise;
import com.example.wolfitness.models.Workout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDetails1Activity extends AppCompatActivity {

    private static final String TAG = "WorkoutDetails1Activity";

    // UI Components
    private Button startWorkoutButton;
    private ImageButton backButton;
    private TextView workoutTitle, workoutDescription, difficultyText, exercisesHeader;
    private ImageView workoutCoverImage;
    private LinearLayout exerciseContainer;
    private ProgressBar progressBar;

    // Data
    private FirebaseFirestore db;
    private String workoutId;
    private Workout currentWorkout;
    private List<Exercise> exercises = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details1);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get workout ID from intent
        workoutId = getIntent().getStringExtra("WORKOUT_ID");
        if (workoutId == null) {
            workoutId = "fullbody_workout_1"; // Default fallback
        }

        // Initialize views
        initializeViews();

        // Load workout data
        loadWorkoutData();
    }

    private void initializeViews() {
        // Buttons
        startWorkoutButton = findViewById(R.id.startWorkoutButton);
        backButton = findViewById(R.id.backButton);

        // Text Views
        workoutTitle = findViewById(R.id.workout_title);
        workoutDescription = findViewById(R.id.workout_description);
        difficultyText = findViewById(R.id.difficulty_text);
        exercisesHeader = findViewById(R.id.exercises_header);

        // Images & Layouts
        workoutCoverImage = findViewById(R.id.jump_rope);
        exerciseContainer = findViewById(R.id.exercise_container);
        progressBar = findViewById(R.id.progress_bar);

        // Click listeners
        startWorkoutButton.setOnClickListener(v -> {
            if (!exercises.isEmpty()) {
                Intent intent = new Intent(WorkoutDetails1Activity.this, WorkoutTimerActivity.class);
                intent.putExtra("WORKOUT_ID", workoutId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No exercises available", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());

        // Difficulty Section Clickable
        LinearLayout difficultySection = findViewById(R.id.difficulty_section);
        if (difficultySection != null) {
            difficultySection.setOnClickListener(v -> showDifficultyOptionsDialog());
        }
    }

    private void loadWorkoutData() {
        showLoading(true);

        db.collection("workouts")
                .document(workoutId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentWorkout = document.toObject(Workout.class);
                            if (currentWorkout != null) {
                                currentWorkout.setId(document.getId());
                                updateWorkoutUI();
                                loadExercises();
                            }
                        } else {
                            Log.d(TAG, "No such workout document");
                            showError("Workout not found");
                        }
                    } else {
                        Log.w(TAG, "Error getting workout document", task.getException());
                        showError("Failed to load workout");
                    }
                });
    }

    private void loadExercises() {
        db.collection("exercises")
                .whereEqualTo("workoutId", workoutId)
                .orderBy("setNumber", Query.Direction.ASCENDING)
                .orderBy("orderInSet", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        exercises.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Exercise exercise = document.toObject(Exercise.class);
                            if (exercise != null) {
                                exercise.setId(document.getId());
                                exercises.add(exercise);
                            }
                        }
                        displayExercises();
                    } else {
                        Log.w(TAG, "Error getting exercises", task.getException());
                        showError("Failed to load exercises");
                    }
                });
    }

    private void updateWorkoutUI() {
        if (currentWorkout != null) {
            if (workoutTitle != null) {
                workoutTitle.setText(currentWorkout.getName());
            }
            if (workoutDescription != null) {
                workoutDescription.setText(String.format("%d Exercises | %s | %s",
                        currentWorkout.getTotalExercises(),
                        currentWorkout.getDuration(),
                        currentWorkout.getCaloriesBurn()));
            }
            if (difficultyText != null) {
                difficultyText.setText(currentWorkout.getDifficulty());
            }
            loadWorkoutCoverImage();
            if (exercisesHeader != null) {
                exercisesHeader.setText(String.format("Exercises (%d Sets)", currentWorkout.getNumberOfSets()));
            }
        }
    }

    private void loadWorkoutCoverImage() {
        if (currentWorkout != null && currentWorkout.getImageUrl() != null &&
                !currentWorkout.getImageUrl().isEmpty()) {
            loadImageWithPicasso(currentWorkout.getImageUrl(), workoutCoverImage,
                    R.drawable.jump_rope);
        } else {
            workoutCoverImage.setImageResource(R.drawable.jump_rope);
        }
    }

    private void displayExercises() {
        exerciseContainer.removeAllViews();
        if (exercises.isEmpty()) {
            TextView noExercisesText = new TextView(this);
            noExercisesText.setText("No exercises available for this workout");
            noExercisesText.setTextSize(16);
            noExercisesText.setPadding(20, 20, 20, 20);
            exerciseContainer.addView(noExercisesText);
            return;
        }

        int currentSet = -1;
        for (Exercise exercise : exercises) {
            if (exercise.getSetNumber() != currentSet) {
                currentSet = exercise.getSetNumber();
                addSetHeader(currentSet);
            }
            addExerciseView(exercise);
        }
    }

    private void addSetHeader(int setNumber) {
        TextView setHeader = new TextView(this);
        setHeader.setText("Set " + setNumber);
        setHeader.setTextSize(14);
        setHeader.setTextColor(getResources().getColor(android.R.color.black));
        setHeader.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, (setNumber == 1) ? dpToPx(10) : dpToPx(20), 0, 0);
        setHeader.setLayoutParams(params);
        exerciseContainer.addView(setHeader);
    }

    private void addExerciseView(Exercise exercise) {
        View exerciseView = LayoutInflater.from(this)
                .inflate(R.layout.item_exercise, exerciseContainer, false);
        ImageView exerciseImage = exerciseView.findViewById(R.id.exercise_image);
        TextView exerciseName = exerciseView.findViewById(R.id.exercise_name);
        TextView exerciseDetails = exerciseView.findViewById(R.id.exercise_details);

        exerciseName.setText(exercise.getName());
        String details = exercise.getDuration() != null && !exercise.getDuration().isEmpty()
                ? exercise.getDuration()
                : exercise.getRepetitions();
        exerciseDetails.setText(details);

        loadExerciseImage(exercise, exerciseImage);

        exerciseView.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutDetails1Activity.this, WorkoutTimerActivity.class);
            intent.putExtra("EXERCISE_ID", exercise.getId()); // Pass the exerciseId
            startActivity(intent);
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(10), 0, 0);
        exerciseView.setLayoutParams(params);
        exerciseContainer.addView(exerciseView);
    }

    private void loadExerciseImage(Exercise exercise, ImageView imageView) {
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            loadImageWithPicasso(exercise.getImageUrl(), imageView,
                    getExerciseImageResource(exercise.getName()));
        } else {
            imageView.setImageResource(getExerciseImageResource(exercise.getName()));
        }
    }

    private void loadImageWithPicasso(String imageUrl, ImageView imageView, int placeholderRes) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .fit()
                .centerCrop()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully: " + imageUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading image: " + imageUrl, e);
                    }
                });
    }

    private int getExerciseImageResource(String exerciseName) {
        switch (exerciseName.toLowerCase().replace(" ", "_").replace("-", "_")) {
            case "warm_up": return R.drawable.warmup;
            case "jumping_jack": return R.drawable.jumpingjack;
            case "skipping": return R.drawable.skipping;
            case "incline_push_ups": return R.drawable.inclinepushups;
            case "push_ups": return R.drawable.pushups;
            case "cobra_stretch": return R.drawable.cobrastretch;
            case "squats": return R.drawable.squats;
            case "arm_raises": return R.drawable.armraises;
            case "rest_and_drink": return R.drawable.restanddrink;
            default: return R.drawable.jump_rope;
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (exerciseContainer != null) {
            exerciseContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        startWorkoutButton.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Dialog to select difficulty
    private void showDifficultyOptionsDialog() {
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
        int selectedPosition = getSelectedDifficultyIndex(currentWorkout != null ? currentWorkout.getDifficulty() : "Beginner");

        new AlertDialog.Builder(this)
                .setTitle("Select Difficulty")
                .setSingleChoiceItems(difficulties, selectedPosition, (dialog, which) -> {
                    String selectedDifficulty = difficulties[which];
                    if (difficultyText != null) {
                        difficultyText.setText(selectedDifficulty);
                    }
                    if (currentWorkout != null) {
                        currentWorkout.setDifficulty(selectedDifficulty);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getSelectedDifficultyIndex(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "beginner": return 0;
            case "intermediate": return 1;
            case "advanced": return 2;
            default: return 0;
        }
    }
}