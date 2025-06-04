package com.example.miruking;

import static com.example.miruking.utils.ProfileManager.loadProfile;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.miruking.R;
import com.example.miruking.dao.StatDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsFragment extends Fragment {

    private ImageView profileImage;
    private TextView xpText;
    private ProgressBar expBar;

    private BarChart weekChart;
    private TextView totalCompleted;
    private TextView totalUncompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // 1. UI 연결
        profileImage = view.findViewById(R.id.character_image);
        xpText = view.findViewById(R.id.xp_text);
        expBar = view.findViewById(R.id.character_progress);

        weekChart = view.findViewById(R.id.barChart);
        totalCompleted = view.findViewById(R.id.tv_completed);
        totalUncompleted = view.findViewById(R.id.tv_uncompleted);


        // 3. 통계 데이터 불러오기
        loadWeeklyStats();
        loadTotalStats();

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeXpFileIfNotExists();
        loadProfileData();  // 매개변수로 view 전달
    }

    private void initializeXpFileIfNotExists() {
        File xpFile = new File(requireContext().getFilesDir(), "profile.txt");

        if (!xpFile.exists()) {
            try (FileWriter writer = new FileWriter(xpFile)) {
                writer.write("0"); // 초기 XP 0
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProfileData() {
        ImageView characterImage = requireView().findViewById(R.id.character_image);
        TextView xpText = requireView().findViewById(R.id.xp_text);
        ProgressBar characterProgress = requireView().findViewById(R.id.character_progress);

        int currentXp = loadProfile(requireContext());

        // 이미지 변경: 100 이상이면 'miru_stand', 미만이면 'miru'
        int imageResId = currentXp >= 100 ? R.drawable.miru_stand : R.drawable.miru;
        characterImage.setImageResource(imageResId);

        // XP 텍스트 갱신
        int remainXp = Math.max(0, 100 - currentXp);
        xpText.setText("미루킹까지 " + remainXp + "XP");

        // 프로그레스바 설정
        characterProgress.setMax(100);
        characterProgress.setProgress(Math.min(currentXp, 100));
    }

    private void loadWeeklyStats() {
        // DAO에서 데이터 로딩
        StatDao statDao = new StatDao(requireContext()); // 생성자 상황에 따라 조정
        Pair<BarData, List<String>> barDataWithLabels = statDao.getWeeklyBarDataWithLabels();

        BarData data = barDataWithLabels.first;
        List<String> labels = barDataWithLabels.second;

        // 데이터 설정
        data.setBarWidth(0.8f); // 스택형 막대는 바가 겹칠 일이 없으므로 좁게 설정 가능
        weekChart.setData(data);

        XAxis xAxis = weekChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(7f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = weekChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);

        weekChart.getAxisRight().setEnabled(false);
        weekChart.getDescription().setEnabled(false);
        weekChart.setFitBars(true);
        weekChart.animateY(500);
        weekChart.invalidate();
        weekChart.setScaleEnabled(false);
        weekChart.setPinchZoom(false);
        weekChart.setDragEnabled(false);
        weekChart.setDoubleTapToZoomEnabled(false);
        weekChart.setHighlightPerDragEnabled(false);
        weekChart.setHighlightPerTapEnabled(false);

        Log.d("ChartDebug", "Data set entry count: " + data.getEntryCount());
    }

    private void loadTotalStats() {
        StatDao statDao = new StatDao(requireContext());
        int[] totalData = statDao.getTotalStats();

        int delay = totalData[0];
        int done = totalData[1];

        totalCompleted.setText(String.valueOf(done));
        totalUncompleted.setText(String.valueOf(delay));
    }
}