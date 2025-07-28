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
            ProfileManager.saveProfile(context, currentXp + 5);   // 5 XP 추가
            // 1. 완료 로그 추가
            ContentValues logValues = new ContentValues();
            logValues.put("todo_ID", todoId);
            logValues.put("todo_state", "완료");
            logValues.put("timestamp", System.currentTimeMillis());
            db.insert("TODO_LOGS", null, logValues);

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

