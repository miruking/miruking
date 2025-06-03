package com.example.miruking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.dao.StatDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class StatsFragment extends Fragment {

    private ImageView profileImage;
    private TextView xpText;
    private ProgressBar expBar;
    private BarChart weekChart;
    private TextView totalCompleted;
    private TextView totalUncompleted;
    private MirukingDBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. UI 컴포넌트 초기화
        profileImage = view.findViewById(R.id.character_image);
        xpText = view.findViewById(R.id.xp_text);
        expBar = view.findViewById(R.id.character_progress);
        weekChart = view.findViewById(R.id.barChart);
        totalCompleted = view.findViewById(R.id.tv_completed);
        totalUncompleted = view.findViewById(R.id.tv_uncompleted);

        // 2. DB 헬퍼 초기화
        dbHelper = new MirukingDBHelper(requireContext());

        // 3. 초기 데이터 로드
        loadProfileData();
        loadWeeklyStats();
        loadTotalStats();
    }

    /**
     * 통계 데이터를 다시 로드하는 메서드 (MainActivity에서 호출)
     */
    public void refreshData() {
        if (getView() != null) {
            loadProfileData();   // 프로필 데이터 다시 로드
            loadWeeklyStats();   // 주간 통계 다시 로드
            loadTotalStats();    // 총 완료/미룬 횟수 다시 로드
        }
    }

    private void loadProfileData() {
        try {
            // 프로필 이미지 로드
            File imageFile = new File(requireContext().getFilesDir(), "profile.jpg");
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                profileImage.setImageBitmap(bitmap);
            }

            // XP 데이터 로드 (예시)
            int currentXp = requireContext()
                    .getSharedPreferences("profile", 0)
                    .getInt("xp", 50);

            xpText.setText("미루킹까지 " + (100 - currentXp) + "XP");
            expBar.setProgress(currentXp);

        } catch (Exception e) {
            Log.e("StatsFragment", "프로필 로딩 오류", e);
        }
    }

    private void loadWeeklyStats() {
        StatDao statDao = new StatDao(dbHelper.getReadableDatabase());

        // 주간 통계 데이터 가져오기 (실제 DB)
        Pair<BarData, List<String>> chartData = statDao.getWeeklyBarDataWithLabels();

        // 차트 설정
        weekChart.setData(chartData.first);

        XAxis xAxis = weekChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(chartData.second));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(chartData.second.size());
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(chartData.second.size());
        xAxis.setDrawGridLines(false);

        weekChart.getAxisRight().setEnabled(false);
        weekChart.getDescription().setEnabled(false);
        weekChart.setFitBars(true);
        weekChart.animateY(500);
        weekChart.invalidate();
    }

    private void loadTotalStats() {
        StatDao statDao = new StatDao(dbHelper.getReadableDatabase());

        int[] stats = statDao.getTotalStats();
        totalCompleted.setText(String.valueOf(stats[1])); // done_num
        totalUncompleted.setText(String.valueOf(stats[0])); // delay_num
    }

    @Override
    public void onDestroyView() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroyView();
    }
}
