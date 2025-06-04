package com.example.miruking.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.Todo;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO {
    private final MirukingDBHelper dbHelper;

    public TodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public List<Todo> getTodosForDate(String date) {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 완료 로그가 있는 일정은 제외하는 쿼리 (NOT EXISTS 사용)
        String query =
                "SELECT t.todo_ID, t.todo_name, t.todo_start_date, t.todo_end_date, " +
                        "t.todo_start_time, t.todo_end_time, t.todo_field, t.todo_delay_stack, t.todo_memo " +
                        "FROM TODOS t " +
                        "WHERE t.todo_start_date <= ? AND t.todo_end_date >= ? " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM TODO_LOGS l " +
                        "    WHERE l.todo_ID = t.todo_ID AND l.todo_state = '완료'" +
                        ") " +
                        "ORDER BY t.todo_start_time";

        Cursor cursor = db.rawQuery(query, new String[]{date, date});
        while (cursor.moveToNext()) {
            list.add(new Todo(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8)
            ));
        }
        cursor.close();
        db.close();
        return list;
    }
    public List<Todo> getTodosByDate(String date) {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 일반 일정        
        String query = "SELECT t_name FROM todos WHERE field = '일반' AND DATE('now', 'localtime') BETWEEN DATE(t_start_date) AND DATE(t_end_date) AND ( TIME('now', 'localtime') BETWEEN TIME(t_start_time) AND TIME(t_end_time) OR t_start_date = NULL ) ;";
        Cursor cursor = db.rawQuery(query, new String[]{date, date});
        while (cursor.moveToNext()) {
            list.add(new Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
        }
        cursor.close();
        db.close();
        
        
        // 북마크 일정
        query = "SELECT t.t_name, b.b_name FROM bookmarks b JOIN todos t ON b.t_id = t.t_id WHERE DATE('now', 'localtime') BETWEEN DATE(b.b_start_date) AND DATE(b.b_end_date) AND ( TIME('now', 'localtime') BETWEEN TIME(b.b_start_time) AND TIME(b.b_end_time) OR t_start_date = NULL ) ;";
        cursor = db.rawQuery(query, new String[]{date, date});
        while (cursor.moveToNext()) {
            list.add(new Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
        }
        cursor.close();
        db.close();

        // 루틴 일정
        query = "SELECT t.t_name FROM routines r JOIN todos t ON r.t_id = t.t_id WHERE r.is_active = 1 AND r.cycle IN (요일) AND ( TIME('now', 'localtime') BETWEEN TIME(t.t_start_time) AND TIME(t.t_end_time) OR t_start_date = NULL ) ;";
        cursor = db.rawQuery(query, new String[]{date, date});
        while (cursor.moveToNext()) {
            list.add(new Todo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getString(8)));
        }
        cursor.close();
        db.close();

        return list;
    }
}
