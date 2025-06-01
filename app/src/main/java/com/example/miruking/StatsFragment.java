package com.example.miruking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.miruking.activities.NavActivity;

public class StatsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 팀원의 activity_stat.xml을 프래그먼트 레이아웃으로 사용
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NavActivity의 주요 초기화 로직을 여기에 통합
        // 예시:
        // NavActivity navActivity = new NavActivity();
        // navActivity.initStatsView(view, ...);
        // 또는 NavActivity의 메서드를 직접 호출 (정적 메서드로 변경 필요)
    }
}
