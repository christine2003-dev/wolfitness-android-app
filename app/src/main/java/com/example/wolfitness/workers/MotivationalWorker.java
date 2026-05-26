package com.example.wolfitness.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.wolfitness.utils.NotificationHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MotivationalWorker extends Worker {
    private static final List<String> MESSAGES = Arrays.asList(
            "Keep pushing toward your goals!",
            "Every step counts—let’s move today!",
            "You’re stronger than you think!",
            "Believe in yourself and all you can achieve!",
            "One workout at a time—keep going!",
            "Consistency is the key to success!",
            "You’re doing amazing—don’t stop now!",
            "Small progress is still progress!",
            "Your future self will thank you!",
            "Stay focused and keep moving forward!",
            "Every rep brings you closer to your goal!",
            "You’ve got this—keep up the great work!",
            "Push yourself because no one else will!"
    );
    private final Random random = new Random();

    public MotivationalWorker(@NonNull Context context,
                              @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Ensure the channel exists
        NotificationHelper.createNotificationChannel(getApplicationContext());

        // Pick and show a random message
        String msg = MESSAGES.get(random.nextInt(MESSAGES.size()));
        NotificationHelper.showNotification(getApplicationContext(),
                "Time to get moving!", msg);

        return Result.success();
    }
}
