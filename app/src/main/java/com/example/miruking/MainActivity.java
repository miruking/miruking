package com.example.miruking;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 초기 화면: ScheduleFragment
        replaceFragment(new ScheduleFragment());

        // 하단 버튼 클릭 리스너
        Button btnSchedule = findViewById(R.id.btn_schedule);
        Button btnStats = findViewById(R.id.btn_stats);

        btnSchedule.setOnClickListener(v -> replaceFragment(new ScheduleFragment()));
        btnStats.setOnClickListener(v -> replaceFragment(new StatsFragment()));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void refreshStatsFragment() {
        // 현재 보이는 프래그먼트가 StatsFragment인 경우에만 갱신
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

    }
}
