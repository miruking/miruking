package com.example.miruking.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.Nag;
import java.util.ArrayList;
import java.util.List;

// DB 쿼리 및 데이터 입출력만 담당하는 순수 DAO 클래스
public class NagDAO {
    private MirukingDBHelper dbHelper;

    public NagDAO(Context context) {
        dbHelper = new MirukingDBHelper(context);
    }

    // 모든 잔소리 문구 조회
    public List<Nag> getAllNags() {
        List<Nag> nags = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nag_ID, nag_txt FROM NAGS ORDER BY nag_ID", null);
        while (cursor.moveToNext()) {
            nags.add(new Nag(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        db.close();
        return nags;
    }

    // 새 잔소리 문구 추가
    public long insertNag(String nagText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nag_txt", nagText);
        long result = db.insert("NAGS", null, values);
        db.close();
        return result;
    }

    // 잔소리 문구 수정
    public int updateNag(int nagId, String newText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nag_txt", newText);
        int count = db.update("NAGS", values, "nag_ID=?", new String[]{String.valueOf(nagId)});
        db.close();
        return count;
    }

    // 잔소리 문구 삭제
    public int deleteNag(int nagId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.delete("NAGS", "nag_ID=?", new String[]{String.valueOf(nagId)});
        db.close();
        return count;
    }
}
