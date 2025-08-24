package com.example.miruking.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
//import com.example.miruking.activities.ScheduleRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleDialogManager {

    private final Context context;
    private final MirukingDBHelper dbHelper;
    private final FrameLayout fragmentContainer;
    private final TextView tvCurrentDate;

    public interface OnScheduleUpdatedListener {
        void onUpdated(int newTodoId);
    }

    public ScheduleDialogManager(Context context, MirukingDBHelper dbHelper, FrameLayout fragmentContainer, TextView tvCurrentDate) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.fragmentContainer = fragmentContainer;
        this.tvCurrentDate = tvCurrentDate;
    }

    public enum ScheduleType {
        GENERAL, DDAY, CREATE, ROUTINE
    }

    public void showScheduleDialog(@Nullable Todo todo, @Nullable String selectedDate, ScheduleType type, OnScheduleUpdatedListener listener) {
        boolean isEdit = (todo != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(isEdit ? "일정 수정" : "일정 추가");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_schedule, null);
        TodoViewHolder holder = initTodoView(view);

        if (isEdit) {
            populateTodoView(holder, todo);
        } else if (selectedDate != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate);
                holder.startCal.setTime(date);
                holder.endCal.setTime(date);
            } catch (Exception ignored) {}
        }

        setDateTimePicker(holder.buttonStartDateTime, holder.startCal, holder.editTextStartDateTime);
        setDateTimePicker(holder.buttonEndDateTime, holder.endCal, holder.editTextEndDateTime);
        setupNagToggle(holder.editTextNag, holder.toggleNagButton);

        builder.setView(view);
        builder.setPositiveButton(isEdit ? "수정 완료" : "저장", (dialog, which) -> {
            long todoId;
            if (isEdit) {
                todoId = updateTodoInDB(holder, todo.getTodoId(), type);
            } else {
                todoId = saveTodoToDB(holder, type);
            }
            if (listener != null && todoId != -1) listener.onUpdated((int) todoId);
            Toast.makeText(context, isEdit ? "일정이 수정되었습니다!" : "일정이 추가되었습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }
    public void showDdayDialog(@Nullable Todo todo, @Nullable String selectedDate, OnScheduleUpdatedListener listener) {
        boolean isEdit = (todo != null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(isEdit ? "D-Day 수정" : "D-Day 추가");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_dday, null);
        EditText editTextTitle = view.findViewById(R.id.editTextDdayTitle);
        Button buttonEndDateTime = view.findViewById(R.id.buttonDdayEndDateTime);
        EditText editTextNag = view.findViewById(R.id.includeCustomNagLayout).findViewById(R.id.editTextCustomNag);
        ImageButton toggleNagButton = view.findViewById(R.id.includeCustomNagLayout).findViewById(R.id.buttonToggleCustomNag);

        setupNagToggle(editTextNag, toggleNagButton);

        Calendar endCal = Calendar.getInstance();
        if (!isEdit && selectedDate != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate);
                endCal.setTime(date);
            } catch (Exception ignored) {}
        }

        setDateTimePicker(buttonEndDateTime, endCal, buttonEndDateTime); // 버튼에도 날짜 표시

        if (isEdit) {
            editTextTitle.setText(todo.getTodoName());
            buttonEndDateTime.setText(todo.getTodoEndDate() + " " + todo.getTodoEndTime());

            // 커스텀 잔소리 로드
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT nag_custom FROM CUSTOM_NAGS WHERE todo_ID = ?", new String[]{String.valueOf(todo.getTodoId())});
            if (cursor.moveToFirst()) {
                editTextNag.setText(cursor.getString(0));
                editTextNag.setVisibility(View.VISIBLE);
            }
            cursor.close();
        }

        builder.setView(view);
        builder.setPositiveButton(isEdit ? "수정 완료" : "저장", (dialog, which) -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("todo_name", editTextTitle.getText().toString());
            values.put("todo_memo", "");
            values.put("todo_start_date", formatDate(endCal)); // D-Day는 시작일 = 종료일
            values.put("todo_start_time", "00:00");
            values.put("todo_end_date", formatDate(endCal));
            values.put("todo_end_time", formatTime(endCal));
            values.put("todo_field", "d-day");

            long id;
            if (isEdit) {
                db.update("TODOS", values, "todo_ID=?", new String[]{String.valueOf(todo.getTodoId())});
                id = todo.getTodoId();
            } else {
                id = db.insert("TODOS", null, values);
            }

            // 잔소리 저장
            String nag = editTextNag.getText().toString();
            if (!nag.trim().isEmpty()) {
                ContentValues nagValues = new ContentValues();
                nagValues.put("todo_ID", id);
                nagValues.put("nag_custom", nag);
                db.insertWithOnConflict("CUSTOM_NAGS", null, nagValues, SQLiteDatabase.CONFLICT_REPLACE);
            }

            if (listener != null) listener.onUpdated((int) id);
            Toast.makeText(context, isEdit ? "D-Day가 수정되었습니다!" : "D-Day가 추가되었습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    public void showRoutineDialog(@Nullable Routine routine,
                                  @Nullable String selectedDate, // 현재는 사용 안 함. 시그니처 맞추기 용
                                  @Nullable OnScheduleUpdatedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(routine == null ? "루틴 추가" : "루틴 수정");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_routine, null);

        // 공통 UI
        EditText editTextTitle = view.findViewById(R.id.editTextRoutineTitle);
        EditText editTextMemo  = view.findViewById(R.id.editTextRoutineMemo);
        Switch   switchActive  = view.findViewById(R.id.switchRoutineActive);
        LinearLayout daySelector = view.findViewById(R.id.layoutDaySelector);
        Button togglePresetButton = view.findViewById(R.id.buttonTogglePreset);
        LinearLayout presetContainer = view.findViewById(R.id.layoutPresetContainer);

        // ----- 커스텀 잔소리(레이아웃이 include이든 직결이든 둘 다 대응) -----
        EditText editTextNag;
        ImageButton toggleNagButton;
        View nagRoot = view.findViewById(R.id.includeCustomNagLayout);
        if (nagRoot != null) {
            editTextNag = nagRoot.findViewById(R.id.editTextCustomNag);
            toggleNagButton = nagRoot.findViewById(R.id.buttonToggleCustomNag);
        } else {
            editTextNag = view.findViewById(R.id.editTextCustomNag);
            toggleNagButton = view.findViewById(R.id.buttonToggleCustomNag);
        }
        if (toggleNagButton != null && editTextNag != null) {
            toggleNagButton.setOnClickListener(v -> {
                editTextNag.setVisibility(editTextNag.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });
        }

        // ----- 요일 선택 UI -----
        final boolean[] selectedDays = new boolean[7];
        final String[] days = {"일","월","화","수","목","금","토"};
        Button[] dayButtons = new Button[7];

        for (int i = 0; i < 7; i++) {
            Button btn = new Button(context);
            btn.setText(days[i]);
            btn.setAllCaps(false);
            btn.setTextColor(Color.BLACK);
            btn.setBackgroundColor(Color.DKGRAY);
            final int index = i;
            btn.setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];
                btn.setBackgroundColor(selectedDays[index]
                        ? ContextCompat.getColor(context, R.color.blueDark)
                        : Color.DKGRAY);
                btn.setTextColor(selectedDays[index] ? Color.WHITE : Color.BLACK);
            });
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btn.setLayoutParams(params);
            daySelector.addView(btn);
            dayButtons[i] = btn;
        }

        // ----- 프리셋 루틴 버튼 -----
        String[] presetRoutines = {"아침 운동", "명상", "독서", "정리정돈"};
        for (String preset : presetRoutines) {
            Button presetBtn = new Button(context);
            presetBtn.setText(preset);
            presetBtn.setAllCaps(false);
            presetBtn.setTextColor(Color.BLACK);
            presetBtn.setBackgroundColor(Color.LTGRAY);
            presetBtn.setTextSize(14);
            presetBtn.setPadding(16, 8, 16, 8);
            presetBtn.setMinHeight(0);
            presetBtn.setMinimumHeight(0);
            presetBtn.setHeight(80);
            presetBtn.setOnClickListener(v -> editTextTitle.setText(preset));
            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 8, 0, 0);
            presetBtn.setLayoutParams(p);
            presetContainer.addView(presetBtn);
        }
        togglePresetButton.setOnClickListener(v -> {
            if (presetContainer.getVisibility() == View.GONE) {
                presetContainer.setVisibility(View.VISIBLE);
                togglePresetButton.setText("프리셋 닫기");
            } else {
                presetContainer.setVisibility(View.GONE);
                togglePresetButton.setText("프리셋 열기");
            }
        });

        // ----- 수정 모드일 때 값 바인딩 -----
        if (routine != null) {
            editTextTitle.setText(routine.getTitle());
            editTextMemo.setText(routine.getMemo());
            switchActive.setChecked(routine.isActive());

            // cycle 파싱 후 요일 미리 선택
            String[] cycleDays = routine.getCycle() != null ? routine.getCycle().split(",") : new String[0];
            for (int i = 0; i < days.length; i++) {
                for (String d : cycleDays) {
                    if (days[i].equals(d.trim())) {
                        selectedDays[i] = true;
                        dayButtons[i].setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));
                        dayButtons[i].setTextColor(Color.WHITE);
                        break;
                    }
                }
            }

            // 기존 커스텀 잔소리 있으면 채워 놓기
            try (SQLiteDatabase rdb = dbHelper.getReadableDatabase();
                 Cursor c = rdb.rawQuery(
                         "SELECT nag_custom FROM CUSTOM_NAGS WHERE todo_ID = ?",
                         new String[]{String.valueOf(routine.getId())})) {
                if (c.moveToFirst() && editTextNag != null) {
                    editTextNag.setText(c.getString(0));
                    editTextNag.setVisibility(View.VISIBLE);
                }
            }
        }

        builder.setView(view);

        // ----- 저장/수정 버튼 -----
        builder.setPositiveButton(routine == null ? "저장" : "수정 완료", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String memo  = editTextMemo.getText().toString();
            boolean isActive = switchActive.isChecked();
            String customNag = (editTextNag != null) ? editTextNag.getText().toString() : "";

            StringBuilder cycle = new StringBuilder();
            for (int i = 0; i < selectedDays.length; i++) {
                if (selectedDays[i]) {
                    if (cycle.length() > 0) cycle.append(",");
                    cycle.append(days[i]);
                }
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            if (routine == null) {
                // ===== 신규 추가 =====
                // 1) TODOS 등록
                ContentValues todoValues = new ContentValues();
                todoValues.put("todo_name", title);
                todoValues.put("todo_memo", memo);
                todoValues.put("todo_field", "routine"); // 기존 코드와 동일 필드 사용
                todoValues.put("todo_delay_stack", 0);
                long todoId = db.insert("TODOS", null, todoValues); // 신규 일정 ID
                // 2) ROUTINES 등록
                ContentValues routineValues = new ContentValues();
                routineValues.put("todo_ID", todoId);
                routineValues.put("cycle", cycle.toString());
                routineValues.put("is_active", isActive ? 1 : 0);
                db.insert("ROUTINES", null, routineValues);
                // 3) CUSTOM_NAGS upsert
                if (!customNag.trim().isEmpty()) {
                    ContentValues nagValues = new ContentValues();
                    nagValues.put("todo_ID", todoId);
                    nagValues.put("nag_custom", customNag);
                    db.insertWithOnConflict("CUSTOM_NAGS", null, nagValues, SQLiteDatabase.CONFLICT_REPLACE);
                }
                if (listener != null) listener.onUpdated((int) todoId);
                Toast.makeText(context, "루틴이 추가되었습니다!", Toast.LENGTH_SHORT).show();

            } else {
                // ===== 수정 =====
                // TODOS 갱신
                ContentValues todoValues = new ContentValues();
                todoValues.put("todo_name", title);
                todoValues.put("todo_memo", memo);
                db.update("TODOS", todoValues, "todo_ID=?",
                        new String[]{String.valueOf(routine.getId())});

                // ROUTINES 갱신
                ContentValues routineValues = new ContentValues();
                routineValues.put("cycle", cycle.toString());
                routineValues.put("is_active", isActive ? 1 : 0);
                db.update("ROUTINES", routineValues, "todo_ID=?",
                        new String[]{String.valueOf(routine.getId())});

                // CUSTOM_NAGS upsert / delete
                if (!customNag.trim().isEmpty()) {
                    ContentValues nagValues = new ContentValues();
                    nagValues.put("todo_ID", routine.getId());
                    nagValues.put("nag_custom", customNag);
                    db.insertWithOnConflict("CUSTOM_NAGS", null, nagValues, SQLiteDatabase.CONFLICT_REPLACE);
                } else {
                    db.delete("CUSTOM_NAGS", "todo_ID = ?", new String[]{String.valueOf(routine.getId())});
                }

                if (listener != null) listener.onUpdated(routine.getId());
                Toast.makeText(context, "루틴 수정 완료!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private TodoViewHolder initTodoView(View view) {
        TodoViewHolder holder = new TodoViewHolder();
        holder.editTextTitle = view.findViewById(R.id.editTextTitle);
        holder.editTextStartDateTime = view.findViewById(R.id.editTextStartDateTime);
        holder.editTextEndDateTime = view.findViewById(R.id.editTextEndDateTime);
        holder.editTextMemo = view.findViewById(R.id.editTextMemo);
        holder.editTextNag = view.findViewById(R.id.editTextCustomNag);
        holder.buttonStartDateTime = view.findViewById(R.id.buttonSelectStartDateTime);
        holder.buttonEndDateTime = view.findViewById(R.id.buttonSelectEndDateTime);
        holder.toggleNagButton = view.findViewById(R.id.buttonToggleCustomNag);
        holder.startCal = Calendar.getInstance();
        holder.endCal = Calendar.getInstance();
        return holder;
    }

    private void populateTodoView(TodoViewHolder holder, Todo todo) {
        holder.editTextTitle.setText(todo.getTodoName());
        holder.editTextMemo.setText(todo.getTodoMemo());
        holder.editTextStartDateTime.setText(todo.getTodoStartDate() + " " + todo.getTodoStartTime());
        holder.editTextEndDateTime.setText(todo.getTodoEndDate() + " " + todo.getTodoEndTime());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nag_custom FROM CUSTOM_NAGS WHERE todo_ID = ?",
                new String[]{String.valueOf(todo.getTodoId())});
        if (cursor.moveToFirst()) {
            holder.editTextNag.setText(cursor.getString(0));
            holder.editTextNag.setVisibility(View.VISIBLE);
        }
        cursor.close();
    }

    private void setDateTimePicker(Button trigger, Calendar cal, TextView target) {
        trigger.setOnClickListener(v -> {
            // 1) 날짜 선택
            DatePickerDialog dp = new DatePickerDialog(
                    v.getContext(),
                    (view, y, m, d) -> {
                        cal.set(Calendar.YEAR, y);
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.DAY_OF_MONTH, d);

                        // 2) 시간 선택
                        TimePickerDialog tp = new TimePickerDialog(
                                v.getContext(),
                                (timeView, h, min) -> {
                                    cal.set(Calendar.HOUR_OF_DAY, h);
                                    cal.set(Calendar.MINUTE, min);
                                    // 표시 (Button도 TextView라 setText 가능)
                                    target.setText(formatDateTime(cal));
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                        );
                        tp.show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });
    }

    // 날짜/시간 포맷 유틸
    private String formatDateTime(Calendar cal) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return fmt.format(cal.getTime());
    }

    private void setupNagToggle(EditText editText, ImageButton toggleBtn) {
        toggleBtn.setOnClickListener(v -> {
            editText.setVisibility(editText.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
    }

    private long saveTodoToDB(TodoViewHolder holder, ScheduleType type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("todo_name", holder.editTextTitle.getText().toString());
        values.put("todo_memo", holder.editTextMemo.getText().toString());
        values.put("todo_start_date", formatDate(holder.startCal));
        values.put("todo_start_time", formatTime(holder.startCal));
        values.put("todo_end_date", formatDate(holder.endCal));
        values.put("todo_end_time", formatTime(holder.endCal));
        values.put("todo_field", type.name().toLowerCase());
        values.put("todo_delay_stack", 0);
        long insertedId = db.insert("TODOS", null, values);

        if (type == ScheduleType.ROUTINE) {
            ContentValues routine = new ContentValues();
            routine.put("todo_ID", insertedId);
            routine.put("cycle", "월화수목금토일");
            db.insert("ROUTINES", null, routine);
        }

        String nag = holder.editTextNag.getText().toString();
        if (!nag.trim().isEmpty()) {
            ContentValues nagValues = new ContentValues();
            nagValues.put("todo_ID", insertedId);
            nagValues.put("nag_custom", nag);
            db.insertWithOnConflict("CUSTOM_NAGS", null, nagValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return insertedId;
    }

    private long updateTodoInDB(TodoViewHolder holder, int todoId, ScheduleType type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("todo_name", holder.editTextTitle.getText().toString());
        values.put("todo_memo", holder.editTextMemo.getText().toString());
        values.put("todo_start_date", formatDate(holder.startCal));
        values.put("todo_start_time", formatTime(holder.startCal));
        values.put("todo_end_date", formatDate(holder.endCal));
        values.put("todo_end_time", formatTime(holder.endCal));

        db.update("TODOS", values, "todo_ID=?", new String[]{String.valueOf(todoId)});

        String nag = holder.editTextNag.getText().toString();
        ContentValues nagValues = new ContentValues();
        nagValues.put("todo_ID", todoId);
        nagValues.put("nag_custom", nag);
        db.insertWithOnConflict("CUSTOM_NAGS", null, nagValues, SQLiteDatabase.CONFLICT_REPLACE);

        return todoId;
    }

    private static class TodoViewHolder {
        EditText editTextTitle, editTextStartDateTime, editTextEndDateTime, editTextMemo, editTextNag;
        Button buttonStartDateTime, buttonEndDateTime;
        ImageButton toggleNagButton;
        Calendar startCal, endCal;
    }
    //삭제 기능
    /*public void deleteTodo(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("TODOS", "todo_ID=?", new String[]{String.valueOf(todoId)});
    }*/
    private void addBookmarkView(LinearLayout container, String title, String startDateTime, String endDateTime) {
        View bookmarkView = LayoutInflater.from(context).inflate(R.layout.bookmark_item, container, false);

        EditText editBookmarkTitle = bookmarkView.findViewById(R.id.editTextBookmarkTitle);
        Button buttonStart = bookmarkView.findViewById(R.id.buttonBookmarkStartDateTime);
        Button buttonEnd = bookmarkView.findViewById(R.id.buttonBookmarkEndDateTime);

        editBookmarkTitle.setText(title);
        buttonStart.setText(startDateTime.isEmpty() ? "시작 선택" : startDateTime);
        buttonEnd.setText(endDateTime.isEmpty() ? "종료 선택" : endDateTime);

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

     buttonStart.setOnClickListener(v -> {
            showDateTimePicker(startCal, (date, time) -> buttonStart.setText(date + " " + time));
        });

        buttonEnd.setOnClickListener(v -> {
            showDateTimePicker(endCal, (date, time) -> buttonEnd.setText(date + " " + time));
        });

        container.addView(bookmarkView);
    }

    //필요 기능
    private void showDateTimePicker(Calendar calendar, DateTimeCallback callback) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(context, (dp, year, month, day) -> {
            new TimePickerDialog(context, (tp, hour, minute) -> {
                calendar.set(year, month, day, hour, minute);
                String dateStr = formatDate(calendar);
                String timeStr = formatTime(calendar);
                callback.onDateTimeSelected(dateStr, timeStr);
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }
    public interface DateTimeCallback {
        void onDateTimeSelected(String date, String time);
    }
    private String formatDate(Calendar cal) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    private String formatTime(Calendar cal) {
        return String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

}
