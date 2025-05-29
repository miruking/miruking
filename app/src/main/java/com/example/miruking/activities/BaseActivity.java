package com.example.miruking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class NavActivity {
    protected void setupBottomNav(String currentTab) {
        LinearLayout navSchedule = findViewById(R.id.nav_schedule);
        LinearLayout navStats = findViewById(R.id.nav_stats);

        TextView textSchedule = findViewById(R.id.nav_text_schedule);
        TextView textStats = findViewById(R.id.nav_text_stats);

        // 선택된 탭 강조 색상 적용
        if ("schedule".equals(currentTab)) {
            textSchedule.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
        } else if ("stats".equals(currentTab)) {
            textStats.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
        }

        navSchedule.setOnClickListener(v -> {
            if (!"schedule".equals(currentTab)) {
                Intent intent = new Intent(this, ScheduleActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // 이전 Activity 제거
            }
        });

        navStats.setOnClickListener(v -> {
            if (!"stats".equals(currentTab)) {
                Intent intent = new Intent(this, StatsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}
