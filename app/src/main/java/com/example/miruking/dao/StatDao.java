package com.example.miruking.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class StatDao {
    private final SQLiteDatabase db;

    public StatDao(SQLiteDatabase database) {
        this.db = database;
    }

    // ✅ 1. 일주일치 통계 (delay_num, done_num)
    public BarData getWeeklyBarData() {
        List<BarEntry> entries = new ArrayList<>();

        String sql = "SELECT delay_num, done_num FROM stat " +
                     "WHERE reference_date BETWEEN date('now', '-7 day') AND date('now') " +
                     "ORDER BY reference_date";

        Cursor cursor = db.rawQuery(sql, null);
        int index = 0;
        while (cursor.moveToNext()) {
            int delay = cursor.getInt(0);
            int done = cursor.getInt(1);

            // 예시: 완료만 보여줄 경우
            entries.add(new BarEntry(index++, done));
        }
        cursor.close();

        BarDataSet set = new BarDataSet(entries, "최근 7일 완료 수");
        return new BarData(set);
    }

    // ✅ 2. 전체 통계 (SUM)
    public int[] getTotalStats() {
        int[] result = new int[]{0, 0}; // [delay, done]

        Cursor cursor = db.rawQuery("SELECT SUM(delay_num), SUM(done_num) FROM stat", null);
        if (cursor.moveToFirst()) {
            result[0] = cursor.getInt(0); // delay
            result[1] = cursor.getInt(1); // done
        }
        cursor.close();

        return result;
    }

    // ✅ 3. 하루 통계 INSERT
    public void insertDailyStat(int delayNum, int doneNum) {
        String sql = "INSERT INTO stat (reference_date, delay_num, done_num) VALUES (date('now'), ?, ?)";
        db.execSQL(sql, new Object[]{delayNum, doneNum});
    }
}
