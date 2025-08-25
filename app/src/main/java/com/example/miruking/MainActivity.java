package com.example.miruking;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.ScheduleDialogManager;
import com.example.miruking.utils.AlarmReceiver;
import com.example.miruking.utils.AppStarter;
import com.example.miruking.utils.NotificationTracker;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private ScheduleDialogManager dialogManager;
    private ScheduleFragment scheduleFragment;
    private FrameLayout FragmentContainer;
    private TextView tvCurrentDate;
    private MirukingDBHelper dbHelper;

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();
        initButtons();
        checkAndRequestNotificationPermission();
    }
    private void initFragments(){
        FragmentContainer = findViewById(R.id.fragment_container);
        dbHelper = new MirukingDBHelper(this);
        scheduleFragment = new ScheduleFragment();
        dialogManager = new ScheduleDialogManager(this, dbHelper, FragmentContainer, tvCurrentDate);

        // 초기 화면: ScheduleFragment
        replaceFragment(new ScheduleFragment());
    }
    private void initButtons(){
        // 하단 버튼 클릭 리스너
        Button btnSchedule = findViewById(R.id.home_button);
        Button btnStats = findViewById(R.id.stat_button);
        Button btnInfo = findViewById(R.id.info_button);

        btnSchedule.setOnClickListener(v -> replaceFragment(new ScheduleFragment()));
        btnStats.setOnClickListener(v -> replaceFragment(new StatsFragment()));
        btnInfo.setOnClickListener(v -> replaceFragment(new InfoFragment()));

    }
    private void initNotificationLogic() {
        AppStarter.scheduleDailyWorker(getApplicationContext());

        if (!NotificationTracker.isTodayNotificationSent(getApplicationContext())) {
            // 오늘 보낸 적 없을 때만 알림 발송
            AlarmReceiver.sendNotifications(getApplicationContext());
            NotificationTracker.markTodayNotificationAsSent(getApplicationContext());
        }
    }
    private void checkAndRequestNotificationPermission() {
        // 권한 먼저 체크하고 알림 관련 코드 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
                return;
            }
        }
        initNotificationLogic();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initNotificationLogic(); // 권한 허용 시 실행
            } else {
                // 권한 거부됨
            }
        }
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

