package com.example.wolfitness.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.wolfitness.R;
import com.example.wolfitness.activities.NotificationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationHelper {
    private static final String CHANNEL_ID   = "motivational_channel";
    private static final String CHANNEL_NAME = "Daily Motivation";
    private static final String PREFS_NAME   = "notifications";
    private static final String PREFS_KEY    = "notification_list";

    /**
     * Call this once at app startup to register the notification channel.
     */
    public static void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Daily motivational notifications");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Builds and fires a notification, then saves its message & timestamp
     * into SharedPreferences so they can be shown in-app later.
     */
    public static void showNotification(Context ctx, String title, String message) {
        // 1) Build the PendingIntent to open NotificationActivity
        Intent intent = new Intent(ctx, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2) Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)    // your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // 3) Post it
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), builder.build());
        }

        // 4) Persist it in SharedPreferences
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(PREFS_KEY, "[]");
        try {
            JSONArray arr = new JSONArray(existing);
            JSONObject obj = new JSONObject();
            obj.put("message", message);
            obj.put("time", System.currentTimeMillis());
            arr.put(obj);
            prefs.edit()
                    .putString(PREFS_KEY, arr.toString())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
