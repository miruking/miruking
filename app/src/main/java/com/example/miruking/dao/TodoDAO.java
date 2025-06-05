package com.example.miruking.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.Todo;
import com.example.miruking.utils.NotificationDTO;

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
    public List<NotificationDTO> getNotificationItemsByDate(String date) {
        List<NotificationDTO> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String dayOfWeek = getKoreanDayOfWeek(date); // "월", "화", ...

        // ✅ 1. 일반 일정
        String query1 =
                "SELECT todo_ID, todo_name, todo_memo, todo_start_date, todo_end_date, todo_delay_stack " +
                        "FROM TODOS " +
                        "WHERE todo_field = '일반' " +
                        "AND DATE(?) BETWEEN DATE(todo_start_date) AND DATE(todo_end_date) " +
                        "AND (TIME('now', 'localtime') BETWEEN TIME(todo_start_time) AND TIME(todo_end_time) " +
                        "     OR todo_start_date IS NULL)";
        Cursor cursor1 = db.rawQuery(query1, new String[]{date});
        while (cursor1.moveToNext()) {
            int t_id = cursor1.getInt(0);
            String title = cursor1.getString(1);
            String description = cursor1.getString(2);
            String startDate = cursor1.getString(3);
            String endDate = cursor1.getString(4);
            int delayStack = cursor1.getInt(5);

            list.add(new NotificationDTO(t_id, title, description, false,
                    startDate, endDate, delayStack, null, null));
        }
        cursor1.close();

        // ✅ 2. 북마크 일정
        String query2 =
                "SELECT t.todo_ID, t.todo_name, b.bookmark_memo, b.bookmark_start_date, b.bookmark_end_date, " +
                        "b.bookmark_delay_stack, b.bookmark_name, b.bookmark_num " +
                        "FROM BOOKMARKS b JOIN TODOS t ON b.todo_ID = t.todo_ID " +
                        "WHERE DATE(?) BETWEEN DATE(b.bookmark_start_date) AND DATE(b.bookmark_end_date) " +
                        "AND (TIME('now', 'localtime') BETWEEN TIME(b.bookmark_start_time) AND TIME(b.bookmark_end_time) " +
                        "     OR b.bookmark_start_date IS NULL)";
        Cursor cursor2 = db.rawQuery(query2, new String[]{date});
        while (cursor2.moveToNext()) {
            int t_id = cursor2.getInt(0);
            String title = cursor2.getString(1);
            String description = cursor2.getString(2);
            String startDate = cursor2.getString(3);
            String endDate = cursor2.getString(4);
            int delayStack = cursor2.getInt(5);
            String bookmarkName = cursor2.getString(6);
            int b_id = cursor2.getInt(7);

            list.add(new NotificationDTO(t_id, title, description, true,
                    startDate, endDate, delayStack, bookmarkName, b_id));
        }
        cursor2.close();

        // ✅ 3. 루틴 일정
        String query3 =
                "SELECT t.todo_ID, t.todo_name, t.todo_memo, t.todo_start_date, t.todo_end_date, t.todo_delay_stack " +
                        "FROM ROUTINES r JOIN TODOS t ON r.todo_ID = t.todo_ID " +
                        "WHERE r.is_active = 1 AND r.cycle LIKE '%' || ? || '%' " +
                        "AND (TIME('now', 'localtime') BETWEEN TIME(t.todo_start_time) AND TIME(t.todo_end_time) " +
                        "     OR t.todo_start_date IS NULL)";
        Cursor cursor3 = db.rawQuery(query3, new String[]{dayOfWeek});
        while (cursor3.moveToNext()) {
            int t_id = cursor3.getInt(0);
            String title = cursor3.getString(1);
            String description = cursor3.getString(2);
            String startDate = cursor3.getString(3);
            String endDate = cursor3.getString(4);
            int delayStack = cursor3.getInt(5);

            list.add(new NotificationDTO(t_id, title, description, false,
                    startDate, endDate, delayStack, null, null));
        }
        cursor3.close();

        db.close();
        return list;
    }

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
