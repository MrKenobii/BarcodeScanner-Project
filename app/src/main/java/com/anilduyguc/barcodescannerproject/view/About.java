package com.anilduyguc.barcodescannerproject.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.anilduyguc.barcodescannerproject.MainActivity;
import com.anilduyguc.barcodescannerproject.R;

public class About extends AppCompatActivity {
    private Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(About.this, MainActivity.class));
        });
    }
}