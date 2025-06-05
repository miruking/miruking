package com.example.miruking.dao;

import static java.lang.Math.min;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.miruking.utils.ProfileManager;

public class LogDAO {

    private final Context context;
    private final SQLiteDatabase db;

    public LogDAO(SQLiteDatabase database, Context context) {
        this.context = context;
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
        if (state.equals("완료")) {
            int currentXp = ProfileManager.loadProfile(context); // 현재 XP 불러오기
            ProfileManager.saveProfile(context, currentXp + 5);   // 5 XP 추가
        } else if (state.equals("미룸")) {
            int currentXp = ProfileManager.loadProfile(context); // 현재 XP 불러오기
            ProfileManager.saveProfile(context, min(0, currentXp - 5));   // 5 XP 감소
        }

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
        if (state.equals("완료")) {
            int currentXp = ProfileManager.loadProfile(context); // 현재 XP 불러오기
            ProfileManager.saveProfile(context, currentXp + 5);   // 5 XP 추가
        } else if (state.equals("미룸")) {
            int currentXp = ProfileManager.loadProfile(context); // 현재 XP 불러오기
            ProfileManager.saveProfile(context, min(0, currentXp - 5));   // 5 XP 감소
        }

        String sql = "INSERT INTO todo_logs (t_id, b_num, state, timestamp) VALUES (?, ?, ?, ?)";
        db.execSQL(sql, new Object[]{tId, bNum, state, timestamp});
    }
}