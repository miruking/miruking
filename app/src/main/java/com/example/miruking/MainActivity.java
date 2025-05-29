package com.example.miruking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.Todo;
import com.example.miruking.TodoAdapter; // <-- 어댑터는 adapter 패키지로 분리
import com.example.miruking.dao.TodoDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isCalendarVisible = false;
    private Calendar currentWeekStartDate;
    private TextView tvCurrentDate;

    private RecyclerView rvTodoList;
    private TodoAdapter todoAdapter; // 어댑터는 TodoAdapter!
    private ArrayList<Todo> todoList = new ArrayList<>();
    private TextView tvEmpty;

    private CalendarView calendarView;
    private LinearLayout weekCalendarLayout;
    private LinearLayout weekDateContainer;
    private ImageButton btnToggleCalendar, btnNextWeek, btnPrevWeek;

    private MirukingDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상단 날짜 표시
        tvCurrentDate = findViewById(R.id.tvCurrentDate);

        // 캘린더 관련 뷰
        calendarView = findViewById(R.id.calendarView);
        weekCalendarLayout = findViewById(R.id.weekCalendarLayout);
        weekDateContainer = findViewById(R.id.weekDateContainer);
        btnToggleCalendar = findViewById(R.id.btnToggleCalendar);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);

        // 일정 리스트 관련 뷰
        rvTodoList = findViewById(R.id.rvTodoList);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvTodoList.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TodoAdapter(this, todoList); // 어댑터는 TodoAdapter!
        rvTodoList.setAdapter(todoAdapter);

        // 오늘 날짜 표시
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);
        tvCurrentDate.setText(sdf.format(new Date()));

        // 주간 캘린더 초기화 (일요일부터)
        currentWeekStartDate = Calendar.getInstance();
        int todayIndex = currentWeekStartDate.get(Calendar.DAY_OF_WEEK) - 1;
        currentWeekStartDate.add(Calendar.DATE, -todayIndex);
        updateWeekCalendar(weekDateContainer);

        // 주간 캘린더 이전/다음 주 이동
        btnNextWeek.setOnClickListener(v -> {
            currentWeekStartDate.add(Calendar.DATE, 7);
            updateWeekCalendar(weekDateContainer);
        });
        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStartDate.add(Calendar.DATE, -7);
            updateWeekCalendar(weekDateContainer);
        });

        // 월간 캘린더 기본 숨김, 주간 캘린더 보임
        calendarView.setVisibility(View.GONE);
        weekCalendarLayout.setVisibility(View.VISIBLE);

        // 캘린더 토글 버튼
        btnToggleCalendar.setOnClickListener(v -> {
            isCalendarVisible = !isCalendarVisible;
            if (isCalendarVisible) {
                calendarView.setVisibility(View.VISIBLE);
                weekCalendarLayout.setVisibility(View.GONE);
            } else {
                calendarView.setVisibility(View.GONE);
                weekCalendarLayout.setVisibility(View.VISIBLE);
            }
        });

        // 월간 캘린더 날짜 선택 시
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            String selectedDate = formatDate(year, month, dayOfMonth);

            // 상단 날짜 갱신
            SimpleDateFormat sdf2 = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);
            tvCurrentDate.setText(sdf2.format(selectedCal.getTime()));

            // 해당 날짜 일정 불러오기
            loadTodosForDate(selectedDate);
        });

        // DB 헬퍼 준비
        dbHelper = new MirukingDBHelper(this);

        // 앱 시작 시 오늘 일정 불러오기
        loadTodosForDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

    }

    // 주간 캘린더 날짜 갱신
    private void updateWeekCalendar(LinearLayout container) {
        container.removeAllViews();
        Calendar temp = (Calendar) currentWeekStartDate.clone();

        for (int i = 0; i < 7; i++) {
            TextView dateText = new TextView(this);
            dateText.setText(String.valueOf(temp.get(Calendar.DAY_OF_MONTH)));
            dateText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dateText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            dateText.setPadding(0, 16, 0, 16);

            Calendar selectedCal = (Calendar) temp.clone();
            String selectedDate = formatDate(selectedCal.get(Calendar.YEAR), selectedCal.get(Calendar.MONTH), selectedCal.get(Calendar.DAY_OF_MONTH));

            dateText.setOnClickListener(v -> {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);
                tvCurrentDate.setText(sdf.format(selectedCal.getTime()));
                loadTodosForDate(selectedDate);
            });

            container.addView(dateText);
            temp.add(Calendar.DATE, 1);
        }
    }

    // yyyy-MM-dd 포맷
    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }

    // 해당 날짜의 일정 불러오기
    private void loadTodosForDate(String date) {
        todoList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT t.todo_ID, t.todo_name, t.todo_start_date, t.todo_end_date, " +
                        "t.todo_start_time, t.todo_end_time, t.todo_field, t.todo_delay_stack, t.todo_memo " +
                        "FROM TODOS t " +
                        "WHERE t.todo_start_date <= ? AND t.todo_end_date >= ? " +
                        "ORDER BY t.todo_start_time",
                new String[]{date, date}
        );
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("todo_ID"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("todo_name"));
            String startDate = cursor.getString(cursor.getColumnIndexOrThrow("todo_start_date"));
            String endDate = cursor.getString(cursor.getColumnIndexOrThrow("todo_end_date"));
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow("todo_start_time"));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow("todo_end_time"));
            String field = cursor.getString(cursor.getColumnIndexOrThrow("todo_field"));
            int delay = cursor.getInt(cursor.getColumnIndexOrThrow("todo_delay_stack"));
            String memo = cursor.getString(cursor.getColumnIndexOrThrow("todo_memo"));
            todoList.add(new Todo(id, name, startDate, endDate, startTime, endTime, field, delay, memo));
        }
        cursor.close();

        // 날짜가 바뀔 때마다 확장 상태 초기화!
        todoAdapter.collapseAllItems();

        todoAdapter.notifyDataSetChanged();

        if (todoList.isEmpty()) {
            rvTodoList.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvTodoList.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
