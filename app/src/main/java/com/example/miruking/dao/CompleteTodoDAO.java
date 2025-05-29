package com.example.miruking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteTodoDAO {
    private MirukingDBHelper dbHelper;

    public CompleteTodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public void insertCompleteLog(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues logValues = new ContentValues();
        logValues.put("todo_ID", todoId);
        logValues.put("todo_state", "완료");
        logValues.put("timestamp", System.currentTimeMillis());
        db.insert("TODO_LOGS", null, logValues);
        db.close();
    }

    public void updateStats() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        db.execSQL("UPDATE STATS SET done_num = done_num + 1 WHERE reference_date = ?", new String[]{today});
        db.close();
    }
}
