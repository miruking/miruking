package com.example.miruking;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.ScheduleDialogManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private ScheduleDialogManager dialogManager;
    private ScheduleFragment scheduleFragment;
    private FrameLayout FragmentContainer;
    private TextView tvCurrentDate;
    private MirukingDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        FragmentContainer = findViewById(R.id.fragment_container);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        dbHelper = new MirukingDBHelper(this);
        scheduleFragment = new ScheduleFragment();
        dialogManager = new ScheduleDialogManager(this, dbHelper,  FragmentContainer, tvCurrentDate);

        // 초기 화면: ScheduleFragment
        replaceFragment(new ScheduleFragment());

        // 하단 버튼 클릭 리스너
        Button btnSchedule = findViewById(R.id.btn_schedule);
        Button btnStats = findViewById(R.id.btn_stats);

        btnSchedule.setOnClickListener(v -> replaceFragment(new ScheduleFragment()));
        btnStats.setOnClickListener(v -> replaceFragment(new StatsFragment()));

        fab.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view, Gravity.END);
            popupMenu.getMenu().add("일반");
            popupMenu.getMenu().add("D-Day");
            popupMenu.getMenu().add("루틴");

            popupMenu.setOnMenuItemClickListener(item -> {
                String type = item.getTitle().toString();
                Toast.makeText(MainActivity.this, type + " 클릭됨", Toast.LENGTH_SHORT).show();

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (currentFragment instanceof ScheduleFragment) {
                    String selectedDate = ((ScheduleFragment) currentFragment).getCurrentDate();

                    if (type.equals("일반")) {
                        dialogManager.showInputTodoDialog(selectedDate, () -> scheduleFragment.loadTodosForDate(selectedDate));
                    } else if (type.equals("D-Day")) {
                        dialogManager.showInputDdayDialog(() -> scheduleFragment.loadTodosForDate(selectedDate));
                    } else if (type.equals("루틴")) {
                        dialogManager.showInputRoutineDialog(() -> scheduleFragment.loadTodosForDate(selectedDate));
                    }
                }
                    return true;
            });

            popupMenu.show();
        });
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
