package com.example.miruking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteTodoDAO {
    private final MirukingDBHelper dbHelper;

    public CompleteTodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public void completeTodo(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. 완료 로그 추가
            ContentValues logValues = new ContentValues();
            logValues.put("todo_ID", todoId);
            logValues.put("todo_state", "완료");
            logValues.put("timestamp", System.currentTimeMillis());
            db.insert("TODO_LOGS", null, logValues);

            // 2. TODOS 테이블에서 일정 삭제
            db.delete("TODOS", "todo_ID = ?", new String[]{String.valueOf(todoId)});

            // 3. 통계 업데이트 (기존 코드 유지)
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            db.execSQL(
                    "INSERT OR REPLACE INTO STATS (reference_date, done_num) " +
                            "VALUES (?, COALESCE((SELECT done_num FROM STATS WHERE reference_date = ?), 0) + 1)",
                    new String[]{today, today}
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

}

