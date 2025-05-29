package com.example.miruking.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;

import java.io.File;
import java.io.FileInputStream;


public class StatActivity {

    private ImageView profileImage;
    private TextView xpText;
    private ProgressBar expBar;

    private BarChart weekChart;
    private TextView totalCompleted;
    private TextView totalUncompleted;

    public StatActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        setupBottomNav("stats");

        // 1. UI 연결
        profileImage = findViewById(R.id.character_image);
        xpText = findViewById(R.id.xp_text);
        expBar = findViewById(R.id.character_progress);

        weekChart = findViewById(R.id.barChart);
        totalCompleted = findViewById(R.id.tv_completed);
        totalUncompleted = findViewById(R.id.tv_uncompleted);

        // 2. 프로필/XP 정보 불러오기
        loadProfileData();

        // 3. 통계 데이터 불러오기
        loadWeeklyStats();
        loadTotalStats();
    }

    private void loadProfileData() {
        try {
            // 파일에서 이미지 불러오기 예시
            File imageFile = new File(getFilesDir(), "profile.jpg");
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                profileImage.setImageBitmap(bitmap);
            }

            // 예시: XP 정보는 SharedPreferences에서 가져오기
            int currentXp = getSharedPreferences("profile", MODE_PRIVATE).getInt("xp", 50);
            xpText.setText("미루킹까지 " + (100 - currentXp) + "XP");
            expBar.setProgress(currentXp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWeeklyStats() {
        // DAO 또는 DB에서 일주일치 데이터 가져오기
        // 여기선 더미 데이터 예시
        BarData barData = StatsChartUtil.getDummyBarData(); // MPAndroidChart용 데이터 생성
        weekChart.setData(barData);
        weekChart.invalidate();
    }

    private void loadTotalStats() {
        // 예시: DB에서 전체 통계 조회
        int completedCount = StatsDao.getTotalCompleted(this);
        int uncompletedCount = StatsDao.getTotalUncompleted(this);

        totalCompleted.setText(String.valueOf(completedCount));
        totalUncompleted.setText(String.valueOf(uncompletedCount));
    }

    
}
