package com.example.miruking.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.example.miruking.DB.MirukingDBHelper;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class StatDao {
    private final MirukingDBHelper dbHelper;

    public StatDao(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    // ✅ 1. 일주일치 통계 (delay_num, done_num)
    public Pair<BarData, List<String>> getWeeklyBarDataWithLabels() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        String sql = "SELECT reference_date, delay_num, done_num FROM stats " +
                "WHERE reference_date BETWEEN date('now', '-6 days') AND date('now') " +
                "ORDER BY reference_date";

        Cursor cursor = db.rawQuery(sql, null);
        int index = 0;
        while (cursor.moveToNext()) {
            String date = cursor.getString(0); // reference_date
            int done = cursor.getInt(2);// done_num

            entries.add(new BarEntry(index, done));
            labels.add(date.substring(5)); // 예: "MM-DD" 형식으로 표시
            index++;
        }
        cursor.close();

        BarDataSet set = new BarDataSet(entries, "최근 7일 완료 수");
        set.setStackLabels(new String[]{"완료", "미룸"});
        BarData data = new BarData(set);
        return new Pair<>(data, labels);
    }

    // ✅ 2. 전체 통계 (SUM)
    public int[] getTotalStats() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int[] result = new int[]{0, 0}; // [delay, done]

        Cursor delayCursor = db.rawQuery(
                "SELECT COUNT(*) FROM TODO_LOGS WHERE todo_state = '미룸'", null);
        if (delayCursor.moveToFirst()) {
            result[0] = delayCursor.getInt(0);
        }
        delayCursor.close();

        // 완료 개수
        Cursor doneCursor = db.rawQuery(
                "SELECT COUNT(*) FROM TODO_LOGS WHERE todo_state = '완료'", null);
        if (doneCursor.moveToFirst()) {
            result[1] = doneCursor.getInt(0);
        }
        doneCursor.close();

        return result;
    }

    // ✅ 3. 하루 통계 INSERT
    public void insertDailyStat() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 미룸 개수 (밀리초 → 초 → 날짜 비교)
        String delayQuery = "SELECT COUNT(*) FROM TODO_LOGS " +
                "WHERE todo_state = '미룸' AND date(timestamp / 1000, 'unixepoch') = date('now', '-1 day')";
        Cursor delayCursor = db.rawQuery(delayQuery, null);
        int delayNum = delayCursor.moveToFirst() ? delayCursor.getInt(0) : 0;
        delayCursor.close();

        // 완료 개수
        String doneQuery = "SELECT COUNT(*) FROM TODO_LOGS " +
                "WHERE todo_state = '완료' AND date(timestamp / 1000, 'unixepoch') = date('now', '-1 day')";
        Cursor doneCursor = db.rawQuery(doneQuery, null);
        int doneNum = doneCursor.moveToFirst() ? doneCursor.getInt(0) : 0;
        doneCursor.close();

        // 삽입
        String insertSql = "INSERT INTO STATS (reference_date, delay_num, done_num) " +
                "VALUES (date('now', '-1 day'), ?, ?)";
        db.execSQL(insertSql, new Object[]{delayNum, doneNum});
    }
}