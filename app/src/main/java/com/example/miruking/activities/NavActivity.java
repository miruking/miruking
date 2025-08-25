package com.example.miruking.activities;

import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miruking.InfoFragment;
import com.example.miruking.MainActivity;
import com.example.miruking.R;

public class NavActivity extends AppCompatActivity {

    protected void setupBottomNav(String currentTab) {
        Button homeButton = findViewById(R.id.home_button);
        Button statButton = findViewById(R.id.stat_button);
        Button infoButton = findViewById(R.id.info_button);

        homeButton.setOnClickListener(v -> {
            if (!"schedule".equals(currentTab)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        statButton.setOnClickListener(v -> {
            if (!"stats".equals(currentTab)) {
                Intent intent = new Intent(this, StatActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        infoButton.setOnClickListener(v -> {
            if (!"info".equals(currentTab)) {
                Intent intent = new Intent(this, InfoFragment.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}

