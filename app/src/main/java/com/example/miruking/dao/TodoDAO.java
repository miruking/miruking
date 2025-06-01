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
}
