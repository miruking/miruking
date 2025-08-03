package com.example.miruking.dao;

import static java.lang.Math.min;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.utils.ProfileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteTodoDAO {
    private final MirukingDBHelper dbHelper;
    private Context context;

    public CompleteTodoDAO(Context context) {
        this.context = context;
        dbHelper = new MirukingDBHelper(context);
    }

    public void completeTodo(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int currentXp = ProfileManager.loadProfile(context); // 현재 XP 불러오기
            ProfileManager.saveProfile(context, currentXp - 5);   // 5 XP 감소
            // 1. 완료 로그 추가
            ContentValues logValues = new ContentValues();
            logValues.put("todo_ID", todoId);
            logValues.put("todo_state", "완료");
            logValues.put("timestamp", System.currentTimeMillis());
            db.insert("TODO_LOGS", null, logValues);

            //stats에 완료 횟수 추가(25.06.06)
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Cursor cursor = db.rawQuery("SELECT stat_num FROM STATS WHERE reference_date = ?", new String[]{today});
            if (cursor.moveToFirst()) {
                db.execSQL("UPDATE STATS SET done_num = done_num + 1 WHERE reference_date = ?", new Object[]{today});
            } else {
                ContentValues statsValues = new ContentValues();
                statsValues.put("reference_date", today);
                statsValues.put("done_num", 1);
                statsValues.put("delay_num", 0);
                db.insert("STATS", null, statsValues);
            }
            cursor.close();

            // 2. TODOS 테이블에서 일정 삭제
            db.delete("TODOS", "todo_ID = ?", new String[]{String.valueOf(todoId)});

            NotificationManagerCompat.from(context).cancel(todoId);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

}

