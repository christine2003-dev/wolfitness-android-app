package com.example.wolfitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wolfitness.R;

public class Onboarding1Activity extends AppCompatActivity {

    private ImageView nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding1);

        nextBtn = findViewById(R.id.btn_next_onboarding1);
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Onboarding1Activity.this, Onboarding2Activity.class);
            startActivity(intent);
            finish();
        });
    }
}