package com.example.miruking.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

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

    // 1. 최근 7일 완료 수 차트 데이터
    public Pair<BarData, List<String>> getWeeklyBarDataWithLabels() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        String sql = "SELECT reference_date, done_num FROM STATS " +
                "WHERE reference_date BETWEEN date('now', '-6 days') AND date('now') " +
                "ORDER BY reference_date";

        Cursor cursor = db.rawQuery(sql, null);
        int index = 0;
        while (cursor.moveToNext()) {
            String date = cursor.getString(0); // reference_date
            int done = cursor.getInt(1);       // done_num

            entries.add(new BarEntry(index, done));
            labels.add(date.substring(5)); // "MM-DD" 형식
            index++;
        }
        cursor.close();

        BarDataSet set = new BarDataSet(entries, "최근 7일 완료 수");
        BarData data = new BarData(set);
        return new Pair<>(data, labels);
    }

    // 2. 전체 통계 (SUM)
    public int[] getTotalStats() {
        int[] result = new int[]{0, 0}; // [delay, done]

        Cursor cursor = db.rawQuery("SELECT SUM(delay_num), SUM(done_num) FROM STATS", null);
        if (cursor.moveToFirst()) {
            result[0] = cursor.getInt(0); // delay
            result[1] = cursor.getInt(1); // done
        }
        cursor.close();

        return result;
    }
}
