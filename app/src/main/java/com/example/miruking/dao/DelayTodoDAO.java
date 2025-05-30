package com.example.miruking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import com.example.miruking.DB.MirukingDBHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DelayTodoDAO {
    private final MirukingDBHelper dbHelper;

    public DelayTodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public Pair<String, Integer> delayTodo(int todoId, String startDate, String endDate, int delayStack) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Pair<String, Integer> nagPair = null;
        try {
            // 날짜 업데이트
            ContentValues values = new ContentValues();
            values.put("todo_start_date", getNextDay(startDate));
            values.put("todo_end_date", getNextDay(endDate));
            values.put("todo_delay_stack", delayStack + 1);
            db.update("TODOS", values, "todo_ID=?", new String[]{String.valueOf(todoId)});

            // 로그 추가
            ContentValues logValues = new ContentValues();
            logValues.put("todo_ID", todoId);
            logValues.put("todo_state", "미룸");
            logValues.put("timestamp", System.currentTimeMillis());
            db.insert("TODO_LOGS", null, logValues);

            // 통계 업데이트
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            db.execSQL("UPDATE STATS SET delay_num = delay_num + 1 WHERE reference_date = ?", new String[]{today});

            // 잔소리 문구 조회
            nagPair = getNagMessage(db, todoId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return nagPair;
    }

    private String getNextDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }

    private Pair<String, Integer> getNagMessage(SQLiteDatabase db, int todoId) {
        String nagText = "오늘도 미루는구나...";
        int delayStack = 0;
        Cursor cursor = db.rawQuery(
                "SELECT n.nag_txt, t.todo_delay_stack FROM TODOS t " +
                        "LEFT JOIN TODOS_NAGS tn ON t.todo_ID = tn.todo_ID " +
                        "LEFT JOIN NAGS n ON tn.nag_ID = n.nag_ID " +
                        "WHERE t.todo_ID = ? AND n.nag_txt IS NOT NULL",
                new String[]{String.valueOf(todoId)}
        );
        if (cursor.moveToFirst()) {
            nagText = cursor.getString(0);
            delayStack = cursor.getInt(1);
        } else {
            cursor.close();
            cursor = db.rawQuery(
                    "SELECT todo_delay_stack FROM TODOS WHERE todo_ID = ?",
                    new String[]{String.valueOf(todoId)}
            );
            if (cursor.moveToFirst()) {
                delayStack = cursor.getInt(0);
            }
            cursor.close();
            cursor = db.rawQuery(
                    "SELECT nag_txt FROM NAGS ORDER BY RANDOM() LIMIT 1",
                    null
            );
            if (cursor.moveToFirst()) {
                nagText = cursor.getString(0);
            }
        }
        cursor.close();
        return new Pair<>(nagText, delayStack);
    }
}