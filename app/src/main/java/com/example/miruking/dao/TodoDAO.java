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

/*
가독성 향상:	    SQL 쿼리에 주석을 달아 로직 이해를 돕고, 명확한 변수명 사용

주석 추가	:       메서드 및 주요 블록에 설명 주석 추가하여 유지보수 용이

재사용 고려:	    getKoreanDayOfWeek()를 재활용해 요일 처리 통일

안정성 향상:	    cursor.isNull() 체크 등 null 안전 처리

모듈화 개선:	    DAO 내부에서 DB 접근 및 Cursor 처리 일관성 유지
 */
public class TodoDAO {
    private final MirukingDBHelper dbHelper;

    // 생성자: DBHelper를 초기화
    public TodoDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    /**
     * 특정 날짜에 해당하는 모든 Todo 목록을 가져온다.
     * 일반, d-day, 루틴 항목 중 완료되지 않은 항목만 필터링한다.
     *
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @return Todo 리스트
     */
    public List<Todo> getTodosForDate(String date) {
        List<Todo> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String dayOfWeek = getKoreanDayOfWeek(date); // 요일: "월", "화" 등

        // ✅ 일반, d-day, 루틴 항목을 조건에 따라 조회하고 완료된 항목 제외
        String query =
                "SELECT todo_ID, todo_name, todo_start_date, todo_end_date, " +
                        "todo_start_time, todo_end_time, todo_field, todo_delay_stack, todo_memo " +
                        "FROM TODOS " +
                        "WHERE (" +
                        "   (todo_field = '일반' AND todo_start_date <= ? AND todo_end_date >= ?) " + // 일반
                        "   OR (todo_field = 'd-day' AND todo_end_date = ?) " +                       // d-day
                        "   OR (todo_field = 'routine' AND is_active = 1 AND todo_start_date <= ? AND todo_end_date >= ? AND cycle LIKE '%' || ? || '%')" + // 루틴
                        ") " +
                        "AND NOT EXISTS ( " +
                        "   SELECT 1 FROM TODO_LOGS l WHERE l.todo_ID = TODOS.todo_ID AND l.todo_state = '완료'" +
                        ") " +
                        "ORDER BY todo_start_time";

        String[] params = {date, date, date, date, date, dayOfWeek};
        Cursor cursor = db.rawQuery(query, params);

        // 결과 Cursor에서 Todo 객체 생성
        while (cursor.moveToNext()) {
            list.add(new Todo(
                    cursor.getInt(0),   // todo_ID
                    cursor.getString(1), // todo_name
                    cursor.getString(2), // todo_start_date
                    cursor.getString(3), // todo_end_date
                    cursor.getString(4), // todo_start_time
                    cursor.getString(5), // todo_end_time
                    cursor.getString(6), // todo_field
                    cursor.getInt(7),    // todo_delay_stack
                    cursor.getString(8)  // todo_memo
            ));
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * 알림(푸시) 전용 DTO 목록을 반환
     * 북마크 정보 포함, 일반/d-day/루틴 항목 조회
     *
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @return NotificationDTO 리스트
     */
    public List<NotificationDTO> getNotificationItemsByDate(String date) {
        List<NotificationDTO> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String dayOfWeek = getKoreanDayOfWeek(date);

        // ✅ 1. 일반, d-day 항목 (북마크 포함)
        String query1 =
                "SELECT " +
                        "t.todo_ID, t.todo_name, t.todo_memo, t.todo_start_date, t.todo_end_date, t.todo_delay_stack, " +
                        "b.bookmark_name, b.bookmark_num " +
                        "FROM TODOS t " +
                        "LEFT JOIN BOOKMARKS b ON t.todo_ID = b.todo_ID " + // 북마크 유무에 관계없이 LEFT JOIN
                        "WHERE (t.todo_field = '일반' OR t.todo_field = 'd-day') " +
                        "AND DATE(?) BETWEEN DATE(t.todo_start_date) AND DATE(t.todo_end_date)";

        Cursor cursor1 = db.rawQuery(query1, new String[]{date});
        while (cursor1.moveToNext()) {
            int t_id = cursor1.getInt(0);
            String title = cursor1.getString(1);
            String description = cursor1.getString(2);
            String startDate = cursor1.getString(3);
            String endDate = cursor1.getString(4);
            int delayStack = cursor1.getInt(5);
            String bookmarkName = cursor1.getString(6);
            int b_id = cursor1.isNull(7) ? 0 : cursor1.getInt(7); // 북마크가 없을 경우 0 처리

            list.add(new NotificationDTO(
                    t_id, title, description, (b_id > 0),
                    startDate, endDate, delayStack,
                    bookmarkName, b_id
            ));
        }
        cursor1.close();

        // ✅ 2. 루틴 항목 (북마크 없음)
        String query3 =
                "SELECT t.todo_ID, t.todo_name, t.todo_memo, t.todo_start_date, t.todo_end_date, t.todo_delay_stack " +
                        "FROM ROUTINES r JOIN TODOS t ON r.todo_ID = t.todo_ID " +
                        "WHERE r.is_active = 1 AND r.cycle LIKE '%' || ? || '%'";

        Cursor cursor3 = db.rawQuery(query3, new String[]{dayOfWeek});
        while (cursor3.moveToNext()) {
            int t_id = cursor3.getInt(0);
            String title = cursor3.getString(1);
            String description = cursor3.getString(2);
            String startDate = cursor3.getString(3);
            String endDate = cursor3.getString(4);
            int delayStack = cursor3.getInt(5);

            list.add(new NotificationDTO(
                    t_id, title, description, false,
                    startDate, endDate, delayStack,
                    null, null
            ));
        }
        cursor3.close();
        db.close();
        return list;
    }

    /**
     * 날짜를 기반으로 요일("월", "화", ...) 반환
     *
     * @param date yyyy-MM-dd 형식의 문자열
     * @return 요일 문자열
     */
    private String getKoreanDayOfWeek(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date d = sdf.parse(date);
            return new SimpleDateFormat("E", Locale.KOREAN).format(d); // 예: "월", "화"
        } catch (Exception e) {
            return "";
        }
    }
}
