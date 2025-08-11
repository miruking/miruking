package com.example.miruking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miruking.MainActivity;
import com.example.miruking.R;

/*
중복 제거:
navigateIfNeeded() 메서드로 공통 로직 분리

문자열 상수화:
"schedule" → TAB_SCHEDULE, "stats" → TAB_STATS 로 바꾸어 오타 방지 및 유지보수성 증가

navigateIfNeeded 메서드 분리:
두 버튼 클릭 리스너에서 공통 동작(탭 이름 비교 + 화면 전환)을 추출하여 중복 제거

가독성과 명확성 향상
 */
public class NavActivity extends AppCompatActivity {

    private static final String TAB_SCHEDULE = "schedule";
    private static final String TAB_STATS = "stats";

    protected void setupBottomNav(String currentTab) {
        Button homeButton = findViewById(R.id.home_button);
        Button statButton = findViewById(R.id.stat_button);

        homeButton.setOnClickListener(v ->
                navigateIfNeeded(currentTab, TAB_SCHEDULE, MainActivity.class)
        );

        statButton.setOnClickListener(v ->
                navigateIfNeeded(currentTab, TAB_STATS, StatActivity.class)
        );
    }

    private void navigateIfNeeded(String currentTab, String targetTab, Class<?> targetActivity) {
        if (!currentTab.equals(targetTab)) {
            Intent intent = new Intent(this, targetActivity);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}
