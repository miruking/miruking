package com.example.miruking.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miruking.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;


public class StatActivity extends AppCompatActivity {

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
        setContentView(R.layout.fragment_stats);

        new NavActivity();

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
        BarChart barChart = findViewById(R.id.barChart);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 2));
        entries.add(new BarEntry(1, 4));
        entries.add(new BarEntry(2, 1));
        entries.add(new BarEntry(3, 3));
        entries.add(new BarEntry(4, 5));
        entries.add(new BarEntry(5, 2));
        entries.add(new BarEntry(6, 4));

        List<String> labels = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

        BarDataSet dataSet = new BarDataSet(entries, "Test Data");
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);

        // X축 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setAxisMinimum(0f); // ★ 꼭 필요!
        xAxis.setAxisMaximum(labels.size()); // ★ 꼭 필요!
        xAxis.setDrawGridLines(false);

        // 기타 설정
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true); // ★ bar width 자동 맞춤
        barChart.animateY(500);
        barChart.invalidate();

        Log.d("ChartDebug", "Data set entry count: " + dataSet.getEntryCount());
        Log.d("ChartDebug", "BarData entry count: " + data.getEntryCount());
        Log.d("ChartDebug", "Chart has data: " + (barChart.getData() != null));

    }


    private void loadTotalStats() {
        // 예시: DB에서 전체 통계 조회
        //int completedCount = StatDao.getTotalCompleted(this);
        //int uncompletedCount = StatDao.getTotalUncompleted(this);

        totalCompleted.setText(String.valueOf(100));
        totalUncompleted.setText(String.valueOf(100));
    }

    
}
