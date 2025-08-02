package com.example.miruking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.utils.ProfileManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
Cursor 처리 방식 개선:	    중복 제거, 각 쿼리마다 열고 닫기 일관성 유지
지역 변수 정리:	            불필요한 변수 제거, 선언 위치 최적화
날짜 처리 함수 사용:	        getNextDay() 함수 활용 일관성 강화
getNagMessage() 정리:       분기 명확화, 중복 제거, 커서 안정성 확보
SQL 쿼리 가독성 향상:	       쿼리 정렬 및 라인 정리
통계(STATS) 업데이트 정리:	   로직 간소화, 조건 분기 명확화
트랜잭션 처리 명확화:	       커밋/종료/알림 구분 처리
*/
public class DelayTodoDAO {
    private final MirukingDBHelper dbHelper;
    private final Context context;

    public DelayTodoDAO(Context context) {
        this.context = context;
        this.dbHelper = new MirukingDBHelper(context);
    }

    /**
     * 할 일 연기 처리: 날짜, 로그, 통계, 잔소리 메시지 처리
     */
    public Pair<String, Integer> delayTodo(int todoId, String startDate, String endDate, int delayStack) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Pair<String, Integer> nagPair = null;

        try {
            reduceXp();  // XP 차감
            updateTodoDates(db, todoId, startDate, endDate, delayStack);
            insertTodoLog(db, todoId);
            updateStats(db);
            nagPair = getNagMessage(db, todoId);  // 잔소리 메시지 반환

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            NotificationManagerCompat.from(context).cancel(todoId);
        }

        return nagPair;
    }

    private void reduceXp() {
        int currentXp = ProfileManager.loadProfile(context);
        ProfileManager.saveProfile(context, currentXp - 5);  // XP 5 차감
    }

    private void updateTodoDates(SQLiteDatabase db, int todoId, String startDate, String endDate, int delayStack) {
        ContentValues values = new ContentValues();
        values.put("todo_start_date", getNextDay(startDate));
        values.put("todo_end_date", getNextDay(endDate));
        values.put("todo_delay_stack", delayStack + 1);

        db.update("TODOS", values, "todo_ID = ?", new String[]{String.valueOf(todoId)});
    }

    private void insertTodoLog(SQLiteDatabase db, int todoId) {
        ContentValues logValues = new ContentValues();
        logValues.put("todo_ID", todoId);
        logValues.put("todo_state", "미룸");
        logValues.put("timestamp", System.currentTimeMillis());
        db.insert("TODO_LOGS", null, logValues);
    }

    private void updateStats(SQLiteDatabase db) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery("SELECT stat_num FROM STATS WHERE reference_date = ?", new String[]{today});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            db.execSQL("UPDATE STATS SET delay_num = delay_num + 1 WHERE reference_date = ?", new Object[]{today});
        } else {
            ContentValues statsValues = new ContentValues();
            statsValues.put("reference_date", today);
            statsValues.put("done_num", 0);
            statsValues.put("delay_num", 1);
            db.insert("STATS", null, statsValues);
        }
    }

    private Pair<String, Integer> getNagMessage(SQLiteDatabase db, int todoId) {
        // 1. 사용자 맞춤 잔소리
        Cursor cursor = db.rawQuery(
                "SELECT nag_custom, " +
                        "(SELECT todo_delay_stack FROM TODOS WHERE todo_ID = ?) AS delay_stack " +
                        "FROM CUSTOM_NAGS WHERE todo_ID = ?",
                new String[]{String.valueOf(todoId), String.valueOf(todoId)}
        );

        if (cursor.moveToFirst()) {
            String nagText = cursor.getString(0);
            int delayStack = cursor.getInt(1);
            cursor.close();
            return new Pair<>(nagText, delayStack);
        }
        cursor.close();

        // 2. 기존 잔소리 연결
        cursor = db.rawQuery(
                "SELECT n.nag_txt, t.todo_delay_stack FROM TODOS t " +
                        "LEFT JOIN TODOS_NAGS tn ON t.todo_ID = tn.todo_ID " +
                        "LEFT JOIN NAGS n ON tn.nag_ID = n.nag_ID " +
                        "WHERE t.todo_ID = ? AND n.nag_txt IS NOT NULL",
                new String[]{String.valueOf(todoId)}
        );

        if (cursor.moveToFirst()) {
            String nagText = cursor.getString(0);
            int delayStack = cursor.getInt(1);
            cursor.close();
            return new Pair<>(nagText, delayStack);
        }
        cursor.close();

        // 3. 랜덤 잔소리 및 스택 조회
        int delayStack = 0;
        String nagText = "오늘도 미루는구나...";

        cursor = db.rawQuery("SELECT todo_delay_stack FROM TODOS WHERE todo_ID = ?", new String[]{String.valueOf(todoId)});
        if (cursor.moveToFirst()) {
            delayStack = cursor.getInt(0);
        }
        cursor.close();

        cursor = db.rawQuery("SELECT nag_txt FROM NAGS ORDER BY RANDOM() LIMIT 1", null);
        if (cursor.moveToFirst()) {
            nagText = cursor.getString(0);
        }
        cursor.close();

        return new Pair<>(nagText, delayStack);
    }

    private String getNextDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }
}
