package com.example.wolfitness.utils;

import android.content.Context;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.wolfitness.workers.MotivationalWorker;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkScheduler {
    // how many notifications per day
    private static final int DAILY_COUNT = 3;

    public static void scheduleDailyMotivation(Context ctx) {
        Calendar now = Calendar.getInstance();
        Calendar start = (Calendar) now.clone();
        start.set(Calendar.HOUR_OF_DAY, 8);    // earliest 8 AM
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 20);     // latest 8 PM

        long window = end.getTimeInMillis() - start.getTimeInMillis();
        for (int i = 0; i < DAILY_COUNT; i++) {
            // random offset from 0…window
            long randomOffset = (long)(Math.random() * window);
            long scheduleTime = start.getTimeInMillis() + randomOffset;
            long delay = scheduleTime - now.getTimeInMillis();
            if (delay < 0) delay += TimeUnit.DAYS.toMillis(1);  // if already past

            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(MotivationalWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(ctx).enqueue(req);
        }
    }
}
