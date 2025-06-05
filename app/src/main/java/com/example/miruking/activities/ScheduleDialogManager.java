package com.example.miruking.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
//import com.example.miruking.activities.ScheduleRepository;

import java.util.Calendar;
import java.util.Locale;

public class ScheduleDialogManager {

    private final Context context;
    private final MirukingDBHelper dbHelper;
    private final FrameLayout FragmentContainer;
    private final TextView tvCurrentDate;
    final Calendar bookmarkStartCal = Calendar.getInstance();
    final Calendar bookmarkEndCal = Calendar.getInstance();

    public interface OnScheduleUpdatedListener {
        void onUpdated(int newTodoId);
    }


    public ScheduleDialogManager(Context context, MirukingDBHelper dbHelper, FrameLayout fragmentContainer, TextView tvCurrentDate) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.FragmentContainer = fragmentContainer;
        this.tvCurrentDate = tvCurrentDate;
    }
    //수정 다이얼로그 관련
    public void showInputTodoDialog(String date, OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("일정 추가");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_schedule, null);

        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextStartDateTime = view.findViewById(R.id.editTextStartDateTime);
        EditText editTextEndDateTime = view.findViewById(R.id.editTextEndDateTime);
        EditText editTextMemo = view.findViewById(R.id.editTextMemo);
        Button buttonStartDateTime = view.findViewById(R.id.buttonSelectStartDateTime);
        Button buttonEndDateTime = view.findViewById(R.id.buttonSelectEndDateTime);

        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();

        // 날짜 선택 버튼 이벤트
        buttonStartDateTime.setOnClickListener(v -> {
            showDateTimePicker(startCal, (dateStr, timeStr) -> {
                editTextStartDateTime.setText(dateStr + " " + timeStr);
            });
        });

        buttonEndDateTime.setOnClickListener(v -> {
            showDateTimePicker(endCal, (dateStr, timeStr) -> {
                editTextEndDateTime.setText(dateStr + " " + timeStr);
            });
        });

        builder.setView(view);
        builder.setPositiveButton("저장", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String memo = editTextMemo.getText().toString();

            String startDate = formatDate(startCal);
            String startTime = formatTime(startCal);
            String endDate = formatDate(endCal);
            String endTime = formatTime(endCal);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("todo_name", title);
            values.put("todo_start_date", startDate);
            values.put("todo_start_time", startTime);
            values.put("todo_end_date", endDate);
            values.put("todo_end_time", endTime);
            values.put("todo_memo", memo);
            values.put("todo_field", "일반"); // 일반 일정
            values.put("todo_delay_stack", 0);

            long insertedId = db.insert("TODOS", null, values); // ✅ 수정: insert 후 ID 받아오기

            if (listener != null && insertedId != -1) {
                listener.onUpdated((int) insertedId); // ✅ 수정: ID를 전달하며 콜백 실행
            }

            Toast.makeText(context, "일정이 추가되었습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }
    public void showInputDdayDialog(OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("D-DAY 추가");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_dday, null);

        EditText editTextTitle = view.findViewById(R.id.editTextDdayTitle);
        Button buttonEndDateTime = view.findViewById(R.id.buttonDdayEndDateTime);
        Button buttonAddBookmark = view.findViewById(R.id.buttonAddBookmark);
        Button buttonRemoveBookmark = view.findViewById(R.id.buttonRemoveBookmark);
        LinearLayout bookmarkContainer = view.findViewById(R.id.layoutBookmarkContainer);

        final Calendar endCal = Calendar.getInstance();
        buttonEndDateTime.setText("날짜 선택");

        // 종료 날짜/시간 선택
        buttonEndDateTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(context, (dp, year, month, day) -> {
                new TimePickerDialog(context, (tp, hour, minute) -> {
                    endCal.set(year, month, day, hour, minute);
                    String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    buttonEndDateTime.setText(dateStr + " " + timeStr);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        buttonAddBookmark.setOnClickListener(v -> {
            addBookmarkView(bookmarkContainer, "", "", "");
        });

        buttonRemoveBookmark.setOnClickListener(v -> {
            int count = bookmarkContainer.getChildCount();
            if (count > 0) bookmarkContainer.removeViewAt(count - 1);
        });

        builder.setView(view);
        builder.setPositiveButton("저장", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DAY_OF_MONTH));
            String endTime = String.format(Locale.getDefault(), "%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // D-DAY 저장
            ContentValues ddayValues = new ContentValues();
            ddayValues.put("todo_name", title);
            ddayValues.put("todo_start_date", endDate);   // ✅ 추가
            ddayValues.put("todo_start_time", endTime);   // ✅ 추가
            ddayValues.put("todo_end_date", endDate);
            ddayValues.put("todo_end_time", endTime);
            ddayValues.put("todo_field", "d-day");
            ddayValues.put("todo_memo", "");
            ddayValues.put("todo_delay_stack", 0);
            long ddayId = db.insert("TODOS", null, ddayValues);

            for (int i = 0; i < bookmarkContainer.getChildCount(); i++) {
                View bmView = bookmarkContainer.getChildAt(i);
                EditText bmTitleView = bmView.findViewById(R.id.editTextBookmarkTitle);
                Button startButton = bmView.findViewById(R.id.buttonBookmarkStartDateTime);
                Button endButton = bmView.findViewById(R.id.buttonBookmarkEndDateTime);

                String bmTitle = bmTitleView.getText().toString();
                String startDateTime = startButton.getText().toString();
                String endDateTime = endButton.getText().toString();

                if (startDateTime.contains(" ") && endDateTime.contains(" ")) {
                    String b_startDate = startDateTime.split(" ")[0];
                    String b_startTime = startDateTime.split(" ")[1];
                    String b_endDate = endDateTime.split(" ")[0];
                    String b_endTime = endDateTime.split(" ")[1];

                    ContentValues bmValues = new ContentValues();
                    bmValues.put("bookmark_num", i);
                    bmValues.put("bookmark_name", bmTitle);
                    bmValues.put("bookmark_start_date", b_startDate);
                    bmValues.put("bookmark_start_time", b_startTime);
                    bmValues.put("bookmark_end_date", b_endDate);
                    bmValues.put("bookmark_end_time", b_endTime);
                    bmValues.put("todo_ID", ddayId);

                    db.insert("BOOKMARKS", null, bmValues);
                }
            }

            if (listener != null && ddayId != -1) {
                listener.onUpdated((int) ddayId);
            }

            Toast.makeText(context, "D-DAY가 추가되었습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    public void showInputRoutineDialog(OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("루틴 추가");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_routine, null);
        EditText editTextTitle = view.findViewById(R.id.editTextRoutineTitle);
        EditText editTextMemo = view.findViewById(R.id.editTextRoutineMemo);
        Switch switchActive = view.findViewById(R.id.switchRoutineActive);
        LinearLayout daySelector = view.findViewById(R.id.layoutDaySelector);

        final boolean[] selectedDays = new boolean[7];
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        Button[] buttons = new Button[7];

        for (int i = 0; i < 7; i++) {
            Button dayBtn = new Button(context);
            dayBtn.setText(days[i]);
            dayBtn.setAllCaps(false);
            dayBtn.setTextColor(Color.BLACK);
            dayBtn.setBackgroundColor(Color.DKGRAY);
            final int index = i;

            dayBtn.setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];
                dayBtn.setBackgroundColor(selectedDays[index] ? ContextCompat.getColor(context, R.color.blueDark) : Color.DKGRAY);
                dayBtn.setTextColor(selectedDays[index] ? Color.WHITE : Color.BLACK);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            dayBtn.setLayoutParams(params);
            daySelector.addView(dayBtn);
            buttons[i] = dayBtn;
        }

        builder.setView(view);
        // 루틴 추가 다이얼로그 저장 버튼 클릭 리스너
        builder.setPositiveButton("저장", (dialog, which) -> {
            SQLiteDatabase db = null;
            long todoId = 0;
            try {
                // 1. 입력값 추출
                String title = editTextTitle.getText().toString();
                String memo = editTextMemo.getText().toString();
                boolean isActive = switchActive.isChecked();

                // 2. cycle 문자열 생성
                StringBuilder cycle = new StringBuilder();
                for (int i = 0; i < selectedDays.length; i++) {
                    if (selectedDays[i]) {
                        if (cycle.length() > 0) cycle.append(",");
                        cycle.append(days[i]); // days 배열은 ["월", "화", ..., "토"]로 가정
                    }
                }
                Log.d("CycleDebug", "생성된 cycle: " + cycle.toString());

                // 3. DB 연결
                db = dbHelper.getWritableDatabase();
                db.beginTransaction(); // 트랜잭션 시작

                // 4. TODOS 테이블에 저장
                ContentValues routineValues = new ContentValues();
                routineValues.put("todo_name", title);
                routineValues.put("todo_memo", memo);
                routineValues.put("todo_field", "routine");
                routineValues.put("todo_delay_stack", 0);
                routineValues.put("todo_start_date", "2025-01-01");
                routineValues.put("todo_end_date", "2099-12-31");
                routineValues.put("todo_start_time", "00:00");
                routineValues.put("todo_end_time", "23:59");
                routineValues.put("cycle", cycle.toString());
                routineValues.put("is_active", isActive ? 1 : 0);

                todoId = db.insert("TODOS", null, routineValues);
                Log.d("InsertDebug", "TODOS insert 결과: " + todoId);

                if (todoId == -1) {
                    throw new Exception("TODOS 테이블 삽입 실패");
                }

                // 5. ROUTINES 테이블에 저장 (필요시)
                ContentValues routineExtra = new ContentValues();
                routineExtra.put("todo_ID", todoId);
                routineExtra.put("cycle", cycle.toString());
                routineExtra.put("is_active", isActive ? 1 : 0);

                long routineRow = db.insert("ROUTINES", null, routineExtra);
                Log.d("InsertDebug", "ROUTINES insert 결과: " + routineRow);

                db.setTransactionSuccessful(); // 트랜잭션 성공
                Toast.makeText(context, "루틴이 추가되었습니다!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e("DatabaseError", "루틴 추가 오류", e);
                Toast.makeText(context, "루틴 추가 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (db != null) {
                    db.endTransaction(); // 트랜잭션 종료
                    db.close();
                }
                if (listener != null && todoId != -1) {
                    listener.onUpdated((int) todoId);
                }

            }
        });


        builder.setNegativeButton("취소", null);
        builder.show();
    }
    // -------------------------
    // 일반 일정 수정
    // -------------------------
    public void showUpdateTodoDialog(Todo todo, View cardView, OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("일정 수정");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_schedule, null);
        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextMemo = view.findViewById(R.id.editTextMemo);
        EditText editTextStartDateTime = view.findViewById(R.id.editTextStartDateTime);
        EditText editTextEndDateTime = view.findViewById(R.id.editTextEndDateTime);
        Button buttonStartDateTime = view.findViewById(R.id.buttonSelectStartDateTime);
        Button buttonEndDateTime = view.findViewById(R.id.buttonSelectEndDateTime);

        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();

        editTextTitle.setText(todo.getTodoId());
        editTextStartDateTime.setText(todo.getTodoStartDate() + " " + todo.getTodoStartTime());
        editTextEndDateTime.setText(todo.getTodoEndDate() + " " + todo.getTodoEndTime());
        editTextMemo.setText(todo.getTodoMemo());

        buttonStartDateTime.setOnClickListener(v -> {
            showDateTimePicker(startCal, (date, time) -> {
                editTextStartDateTime.setText(date + " " + time);
            });
        });

        buttonEndDateTime.setOnClickListener(v -> {
            showDateTimePicker(endCal, (date, time) -> {
                editTextEndDateTime.setText(date + " " + time);
            });
        });

        builder.setView(view);
        builder.setPositiveButton("수정 완료", (dialog, which) -> {
            String newTitle = editTextTitle.getText().toString();
            String newMemo = editTextMemo.getText().toString();
            String startDateTime = editTextStartDateTime.getText().toString().trim();
            String endDateTime = editTextEndDateTime.getText().toString().trim();;

            String[] startParts = startDateTime.split(" ");
            String[] endParts = endDateTime.split(" ");

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("todo_name", newTitle);
            values.put("todo_memo", newMemo);
            if (startParts.length == 2 && endParts.length == 2) {
                values.put("todo_start_date", startParts[0]);
                values.put("todo_start_time", startParts[1]);
                values.put("todo_end_date", endParts[0]);
                values.put("todo_end_time", endParts[1]);
            }
            db.update("TODOS", values, "todo_ID=?", new String[]{String.valueOf(todo.getTodoId())});

            FragmentContainer.removeView(cardView);
            if (listener != null) listener.onUpdated(todo.getTodoId());


            Toast.makeText(context, "수정 완료!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // -------------------------
    // D-DAY 수정
    // -------------------------
    public void showUpdateDdayDialog(Dday dday, View cardView, OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("D-DAY 수정");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_dday, null);

        EditText editTextTitle = view.findViewById(R.id.editTextDdayTitle);
        Button buttonEndDateTime = view.findViewById(R.id.buttonDdayEndDateTime);
        Button buttonAddBookmark = view.findViewById(R.id.buttonAddBookmark);
        Button buttonRemoveBookmark = view.findViewById(R.id.buttonRemoveBookmark);
        LinearLayout bookmarkContainer = view.findViewById(R.id.layoutBookmarkContainer);

        // ⏰ 종료 시간 초기값 설정
        editTextTitle.setText(dday.getTitle());
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.YEAR, Integer.parseInt(dday.getEndDate().split("-")[0]));
        endCal.set(Calendar.MONTH, Integer.parseInt(dday.getEndDate().split("-")[1]) - 1);
        endCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dday.getEndDate().split("-")[2]));
        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dday.getEndTime().split(":")[0]));
        endCal.set(Calendar.MINUTE, Integer.parseInt(dday.getEndTime().split(":")[1]));

        buttonEndDateTime.setText(dday.getEndDate() + " " + dday.getEndTime());

        // 📅 날짜 및 시간 선택기
        buttonEndDateTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(context, (dp, year, month, day) -> {
                new TimePickerDialog(context, (tp, hour, minute) -> {
                    endCal.set(year, month, day, hour, minute);
                    String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    buttonEndDateTime.setText(dateStr + " " + timeStr);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        /*
        // 🌟 기존 북마크 불러오기
        List<Bookmark> bookmarks = new ScheduleRepository(context).getBookmarkListByTodoId(dday.getId());
        for (Bookmark bm : bookmarks) {
            addBookmarkView(bookmarkContainer,
                    bm.getTitle(),
                    bm.getStartDate() + " " + bm.getStartTime(),
                    bm.getEndDate() + " " + bm.getEndTime()
            );
        }*/

        // ➕ 북마크 추가 버튼
        buttonAddBookmark.setOnClickListener(v -> {
            addBookmarkView(bookmarkContainer, "", "", "");
        });

        // ➖ 북마크 삭제 버튼
        buttonRemoveBookmark.setOnClickListener(v -> {
            int count = bookmarkContainer.getChildCount();
            if (count > 0) bookmarkContainer.removeViewAt(count - 1);
        });

        builder.setView(view);
        builder.setPositiveButton("수정 완료", (dialog, which) -> {
            String newTitle = editTextTitle.getText().toString();
            String newEndDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DAY_OF_MONTH));
            String newEndTime = String.format(Locale.getDefault(), "%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // ✅ 1. DDAY UPDATE
            ContentValues ddayValues = new ContentValues();
            ddayValues.put("todo_name", newTitle);
            ddayValues.put("todo_end_date", newEndDate);
            ddayValues.put("todo_end_time", newEndTime);
            db.update("TODOS", ddayValues, "todo_ID=?", new String[]{String.valueOf(dday.getId())});

            // ✅ 2. BOOKMARKS DELETE → RE-INSERT
            db.delete("BOOKMARKS", "todo_ID=?", new String[]{String.valueOf(dday.getId())});

            for (int i = 0; i < bookmarkContainer.getChildCount(); i++) {
                View bmView = bookmarkContainer.getChildAt(i);
                EditText bmTitle = bmView.findViewById(R.id.editTextBookmarkTitle);
                Button bmStartBtn = bmView.findViewById(R.id.buttonBookmarkStartDateTime);
                Button bmEndBtn = bmView.findViewById(R.id.buttonBookmarkEndDateTime);

                String title = bmTitle.getText().toString();
                String[] startSplit = bmStartBtn.getText().toString().split(" ");
                String[] endSplit = bmEndBtn.getText().toString().split(" ");

                if (startSplit.length != 2 || endSplit.length != 2) continue;

                ContentValues bmValues = new ContentValues();
                bmValues.put("bookmark_name", title);
                bmValues.put("bookmark_start_date", startSplit[0]);
                bmValues.put("bookmark_start_time", startSplit[1]);
                bmValues.put("bookmark_end_date", endSplit[0]);
                bmValues.put("bookmark_end_time", endSplit[1]);
                bmValues.put("bookmark_delay_stack", 0);
                bmValues.put("todo_ID", dday.getId());
                db.insert("BOOKMARKS", null, bmValues);
            }

            FragmentContainer.removeView(cardView);
            if (listener != null) listener.onUpdated(dday.getId());

            Toast.makeText(context, "D-DAY 수정 완료!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }


    // -------------------------
    // 루틴 수정
    // -------------------------
    public void showUpdateRoutineDialog(Routine routine, View cardView, OnScheduleUpdatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("루틴 수정");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogue_input_routine, null);
        EditText title = view.findViewById(R.id.editTextRoutineTitle);
        EditText memo = view.findViewById(R.id.editTextRoutineMemo);
        Switch switchActive = view.findViewById(R.id.switchRoutineActive);
        LinearLayout daySelector = view.findViewById(R.id.layoutDaySelector);

        title.setText(routine.getTitle());
        memo.setText(routine.getMemo());
        switchActive.setChecked(routine.isActive());

        final boolean[] selectedDays = new boolean[7];
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        Button[] buttons = new Button[7];
        String[] cycleDays = routine.getCycle().split(",");

        for (int i = 0; i < days.length; i++) {
            Button btn = new Button(context);
            btn.setText(days[i]);
            btn.setAllCaps(false);
            btn.setTextColor(Color.BLACK);
            btn.setBackgroundColor(Color.DKGRAY);
            final int index = i;

            for (String d : cycleDays) {
                if (d.equals(days[i])) {
                    selectedDays[i] = true;
                    btn.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_500));
                    btn.setTextColor(Color.WHITE);
                    break;
                }
            }

            btn.setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];
                btn.setBackgroundColor(selectedDays[index] ? ContextCompat.getColor(context, R.color.purple_500) : Color.DKGRAY);
                btn.setTextColor(selectedDays[index] ? Color.WHITE : Color.BLACK);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btn.setLayoutParams(params);
            daySelector.addView(btn);
            buttons[i] = btn;
        }

        builder.setView(view);
        builder.setPositiveButton("수정 완료", (dialog, which) -> {
            String newTitle = title.getText().toString();
            String newMemo = memo.getText().toString();
            boolean isActive = switchActive.isChecked();

            StringBuilder cycleBuilder = new StringBuilder();
            for (int i = 0; i < selectedDays.length; i++) {
                if (selectedDays[i]) {
                    if (cycleBuilder.length() > 0) cycleBuilder.append(",");
                    cycleBuilder.append(days[i]);
                }
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues todoValues = new ContentValues();
            todoValues.put("todo_name", newTitle);
            todoValues.put("todo_memo", newMemo);
            db.update("TODOS", todoValues, "todo_ID=?", new String[]{String.valueOf(routine.getId())});

            ContentValues routineValues = new ContentValues();
            routineValues.put("cycle", cycleBuilder.toString());
            routineValues.put("is_active", isActive ? 1 : 0);
            db.update("ROUTINES", routineValues, "todo_ID=?", new String[]{String.valueOf(routine.getId())});

            FragmentContainer.removeView(cardView);
            if (listener != null) listener.onUpdated(routine.getId());

            Toast.makeText(context, "루틴 수정 완료!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
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
    public void delete(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("BOOKMARKS", "todo_ID=?", new String[]{String.valueOf(todoId)});
        db.delete("TODOS", "todo_ID=?", new String[]{String.valueOf(todoId)});
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
