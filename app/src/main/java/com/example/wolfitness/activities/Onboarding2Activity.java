package com.example.wolfitness.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView; // Import ImageView
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wolfitness.R;

public class Onboarding2Activity extends AppCompatActivity {

    private ImageView nextBtn; // Declare ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nextBtn = findViewById(R.id.btn_next_onboarding2); // Initialize ImageView
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Onboarding2Activity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });
    }
}