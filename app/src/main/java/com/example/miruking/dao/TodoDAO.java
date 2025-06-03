package com.example.miruking.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoDAO {
    private final MirukingDBHelper dbHelper;

    public TodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    public List<Todo> getTodosForDate(String date) {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String dayOfWeek = getKoreanDayOfWeek(date);

        String query =
                "SELECT todo_ID, todo_name, todo_start_date, todo_end_date, " +
                        "todo_start_time, todo_end_time, todo_field, todo_delay_stack, todo_memo " +
                        "FROM TODOS " +
                        "WHERE (" +
                        "   (todo_field = '일반' AND todo_start_date <= ? AND todo_end_date >= ?) " + // 일반 일정
                        "   OR (todo_field = 'd-day' AND todo_end_date = ?) " +                         // D-Day
                        "   OR (todo_field = 'routine' AND is_active = 1 AND todo_start_date <= ? AND todo_end_date >= ? AND cycle LIKE '%' || ? || '%')" + // 루틴
                        ") " +
                        "AND NOT EXISTS ( " +
                        "   SELECT 1 FROM TODO_LOGS l WHERE l.todo_ID = TODOS.todo_ID AND l.todo_state = '완료'" +
                        ") " +
                        "ORDER BY todo_start_time";

        String[] params = {date, date, date, date, date, dayOfWeek};
        Cursor cursor = db.rawQuery(query, params);

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

    // 날짜를 한글 요일로 변환 (예: 2023-10-20 → "금")
    private String getKoreanDayOfWeek(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date d = sdf.parse(date);
            return new SimpleDateFormat("E", Locale.KOREAN).format(d);
        } catch (Exception e) {
            return "";
        }
    }
}
