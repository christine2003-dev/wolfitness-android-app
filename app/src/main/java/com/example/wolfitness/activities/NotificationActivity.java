package com.example.wolfitness.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wolfitness.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // header buttons (unchanged) :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        // moreOptionsButton…

        // 1) Find our dynamic container:
        notificationList = findViewById(R.id.notificationListContainer);

        // 2) Load saved JSON array:
        SharedPreferences prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE);
        String json = prefs.getString("notification_list", "[]");

        try {
            JSONArray arr = new JSONArray(json);
            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String msg = obj.getString("message");
                long time = obj.getLong("time");

                // 3) Inflate an item and populate:
                View item = inflater.inflate(R.layout.item_notification, notificationList, false);
                ((TextView) item.findViewById(R.id.notificationText)).setText(msg);
                ((TextView) item.findViewById(R.id.notificationTime))
                        .setText(DateUtils.getRelativeTimeSpanString(time));

                // 4) Add a divider if not the first:
                if (i > 0) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(lp);
                    divider.setBackgroundColor(0xFFC0C0C0);
                    notificationList.addView(divider);
                }

                notificationList.addView(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
