package com.example.miruking.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;

public class DeleteTodoDAO {
    private final MirukingDBHelper dbHelper;

    public DeleteTodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public void deleteTodo(int todoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM TODO_LOGS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.execSQL("DELETE FROM TODOS_NAGS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.execSQL("DELETE FROM ROUTINES WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.execSQL("DELETE FROM BOOKMARKS_NAGS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.execSQL("DELETE FROM BOOKMARKS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.execSQL("DELETE FROM TODOS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
