package com.example.miruking.dao;

import android.database.sqlite.SQLiteDatabase;

public class LogDao {

    private final SQLiteDatabase db;

    public LogDao(SQLiteDatabase database) {
        this.db = database;
    }

    /**
     * 알림 로그 삽입 (북마크 없는 일반 할 일)
     *
     * @param tId       할 일 ID
     * @param state     상태 설명 (예: "알림보냄", "완료", "미룸")
     * @param timestamp 타임스탬프 (System.currentTimeMillis() 사용 가능)
     */
    public void insertLog(int tId, String state, long timestamp) {
        String sql = "INSERT INTO todo_logs (t_id, state, timestamp) VALUES (?, ?, ?)";
        db.execSQL(sql, new Object[]{tId, state, timestamp});
    }

    /**
     * 알림 로그 삽입 (북마크 포함 할 일)
     *
     * @param tId       할 일 ID
     * @param bNum      북마크 번호
     * @param state     상태 설명
     * @param timestamp 타임스탬프
     */
    public void insertLogWithBookmark(int tId, int bNum, String state, long timestamp) {
        String sql = "INSERT INTO todo_logs (t_id, b_num, state, timestamp) VALUES (?, ?, ?, ?)";
        db.execSQL(sql, new Object[]{tId, bNum, state, timestamp});
    }
}