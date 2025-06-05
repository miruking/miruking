package com.example.miruking;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.ScheduleDialogManager;
import com.example.miruking.activities.Todo;
import com.example.miruking.utils.AlarmReceiver;
import com.example.miruking.utils.NotificationTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private boolean isCalendarVisible = false;
    private Calendar currentWeekStartDate;
    private TextView tvCurrentDate;

    private RecyclerView rvTodoList;
    private TodoAdapter todoAdapter;
    private ArrayList<Todo> todoList = new ArrayList<>();
    private TextView tvEmpty;

    private CalendarView calendarView;
    private LinearLayout weekCalendarLayout;
    private LinearLayout weekDateContainer;
    private ImageButton btnToggleCalendar, btnNextWeek, btnPrevWeek;

    private MirukingDBHelper dbHelper;

    // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
    private String selectedDate;
    //수정 메뉴(25.06.02)
    private ScheduleDialogManager dialogManager;
    private FrameLayout fragmentContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        calendarView = view.findViewById(R.id.calendarView);
        weekCalendarLayout = view.findViewById(R.id.weekCalendarLayout);
        weekDateContainer = view.findViewById(R.id.weekDateContainer);
        btnToggleCalendar = view.findViewById(R.id.btnToggleCalendar);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
        btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        rvTodoList = view.findViewById(R.id.rvTodoList);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        //수정 메뉴(25.06.02)
        dbHelper = new MirukingDBHelper(getContext());
        dialogManager = new ScheduleDialogManager(getContext(), dbHelper, fragmentContainer, tvCurrentDate);
        todoAdapter = new TodoAdapter(getContext(), todoList, dbHelper, dialogManager);

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);

        // DialogManager 초기화 (필요시 MainActivity에서 전달받거나 직접 생성)
        dbHelper = new MirukingDBHelper(requireContext());
        dialogManager = new ScheduleDialogManager(requireContext(), dbHelper, null, tvCurrentDate);

        fab.setOnClickListener(fabView -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), fabView, Gravity.END);
            popupMenu.getMenu().add("일반");
            popupMenu.getMenu().add("D-Day");
            popupMenu.getMenu().add("루틴");

            popupMenu.setOnMenuItemClickListener(item -> {
                String type = item.getTitle().toString();
                Toast.makeText(requireContext(), type + " 클릭됨", Toast.LENGTH_SHORT).show();

                String selectedDate = getCurrentDateForDb();

                if (type.equals("일반")) {
                    dialogManager.showInputTodoDialog(selectedDate, newTodoId -> {
                        loadTodosForDate(selectedDate);
                        if (!NotificationTracker.getSentTodaySet(requireContext()).contains(String.valueOf(newTodoId))) {
                            AlarmReceiver.sendNotifications(requireContext());
                        }
                    });
                } else if (type.equals("D-Day")) {
                    dialogManager.showInputDdayDialog(newTodoId -> {
                        loadTodosForDate(selectedDate);
                        if (!NotificationTracker.getSentTodaySet(requireContext()).contains(String.valueOf(newTodoId))) {
                            AlarmReceiver.sendNotifications(requireContext());
                        }
                    });
                } else if (type.equals("루틴")) {
                    dialogManager.showInputRoutineDialog(newTodoId -> {
                        loadTodosForDate(selectedDate);
                        if (!NotificationTracker.getSentTodaySet(requireContext()).contains(String.valueOf(newTodoId))) {
                            AlarmReceiver.sendNotifications(requireContext());
                        }
                    });
                }
                return true;
            });

            popupMenu.show();
        });

        rvTodoList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTodoList.setAdapter(todoAdapter);

        currentWeekStartDate = Calendar.getInstance();
        int todayIndex = currentWeekStartDate.get(Calendar.DAY_OF_WEEK) - 1;
        currentWeekStartDate.add(Calendar.DATE, -todayIndex);
        updateWeekCalendar(weekDateContainer);

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStartDate.add(Calendar.DATE, 7);
            updateWeekCalendar(weekDateContainer);
        });

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStartDate.add(Calendar.DATE, -7);
            updateWeekCalendar(weekDateContainer);
        });

        calendarView.setVisibility(View.GONE);
        weekCalendarLayout.setVisibility(View.VISIBLE);

        btnToggleCalendar.setOnClickListener(v -> {
            isCalendarVisible = !isCalendarVisible;
            // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
            calendarView.setVisibility(isCalendarVisible ? View.VISIBLE : View.GONE);
            weekCalendarLayout.setVisibility(isCalendarVisible ? View.GONE : View.VISIBLE);
        });

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
            selectedDate = formatDate(year, month, dayOfMonth);
            tvCurrentDate.setText(new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(selectedCal.getTime()));
            loadTodosForDate(selectedDate);
        });

        // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(new Date()));
        loadTodosForDate(selectedDate);
    }

    private void updateWeekCalendar(LinearLayout container) {
        container.removeAllViews();
        Calendar temp = (Calendar) currentWeekStartDate.clone();

        for (int i = 0; i < 7; i++) {
            TextView dateText = new TextView(getContext());
            dateText.setText(String.valueOf(temp.get(Calendar.DAY_OF_MONTH)));
            dateText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dateText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            dateText.setPadding(0, 16, 0, 16);

            Calendar selectedCal = (Calendar) temp.clone();
            // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
            String dateStr = formatDate(
                    selectedCal.get(Calendar.YEAR),
                    selectedCal.get(Calendar.MONTH),
                    selectedCal.get(Calendar.DAY_OF_MONTH)
            );

            dateText.setOnClickListener(v -> {
                // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
                selectedDate = dateStr;
                tvCurrentDate.setText(new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(selectedCal.getTime()));
                loadTodosForDate(selectedDate);
            });

            container.addView(dateText);
            temp.add(Calendar.DATE, 1);
        }
    }

    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }

    private void setCurrentDate(String dbDate) {
        // dbDate: yyyy-MM-dd
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dbDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);
            tvCurrentDate.setText(sdf.format(date));
        } catch (ParseException e) {
            tvCurrentDate.setText(dbDate); // fallback
        }
    }

    private String getCurrentDateForDb() {
        // tvCurrentDate의 값을 yyyy-MM-dd 형식으로 변환
        try {
            String display = tvCurrentDate.getText().toString();
            Date date = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).parse(display);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            // fallback: 오늘 날짜 반환
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }
    }

    void loadTodosForDate(String date) {
        // 일정 입력 시 앱 튕김 문제 수정(25.06.02)
        if (dbHelper == null) {
            Log.e("ScheduleFragment", "DB 헬퍼가 초기화되지 않았습니다.");
            return;
        }

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

    public String getCurrentDate() {
        // 일정 저장 후 바로 리스트가 갱신되진 않는 문제 수정(25.06.02)
        return selectedDate != null ? selectedDate : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
//수정 메뉴 관련 오류 수정(25.06.02)
    public void setFragmentContainer(FrameLayout fragmentContainer) {
        this.fragmentContainer = fragmentContainer;
    }
}
