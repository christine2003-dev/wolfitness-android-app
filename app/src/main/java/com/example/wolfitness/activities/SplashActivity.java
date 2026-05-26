package com.example.wolfitness.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.wolfitness.R;
import com.example.wolfitness.utils.NotificationHelper;
import com.example.wolfitness.utils.WorkScheduler;
import com.example.wolfitness.workers.MotivationalWorker;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private Button getStartedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_splash);

        // 1) Create the notification channel
        NotificationHelper.createNotificationChannel(this);

        // 2) Request POST_NOTIFICATIONS on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQUEST_NOTIFICATION_PERMISSION
                );
            } else {
                initNotificationWork();
            }
        } else {
            // Pre-TIRAMISU: permission granted by default
            initNotificationWork();
        }

        // 3) Continue splash flow
        getStartedBtn = findViewById(R.id.btn_get_started);
        getStartedBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, Onboarding1Activity.class));
            finish();
        });
    }

    /**
     * Schedules daily motivational notifications and a series of test notifications.
     */
    private void initNotificationWork() {
        // A) Schedule your 3 random daily motivational notifications
        WorkScheduler.scheduleDailyMotivation(this);

        // B) Enqueue test notifications at 10s, 40s, 70s, 100s, 130s, and 160s
        for (int i = 0; i < 6; i++) {
            long delaySeconds = 10 + (i * 30);
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MotivationalWorker.class)
                    .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(this).enqueue(request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initNotificationWork();
            } else {
                Toast.makeText(this,
                        "Notification permission denied. Please enable in Settings.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
