package com.example.miruking.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MirukingDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "miruking.db";
    public static final int DATABASE_VERSION = 2;

    public MirukingDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODOS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS TODOS (" +
                "todo_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "todo_name TEXT NOT NULL," +
                "todo_start_date TEXT NOT NULL," +
                "todo_end_date TEXT NOT NULL," +
                "todo_start_time TEXT," +
                "todo_end_time TEXT," +
                "todo_field TEXT," +
                "todo_delay_stack INTEGER DEFAULT 0," +
                "todo_memo TEXT," +
                "cycle TEXT," +       // 루틴만 사용, 나머지는 NULL
                "is_active INTEGER" + //루틴만 사용, 나머지는 NULL 또는 1
                ");");


        // BOOKMARKS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS BOOKMARKS (" +
                "bookmark_num INTEGER," +
                "todo_ID INTEGER," +
                "bookmark_name TEXT NOT NULL," +
                "bookmark_start_date TEXT NOT NULL," +
                "bookmark_end_date TEXT NOT NULL," +
                "bookmark_start_time TEXT," +
                "bookmark_end_time TEXT," +
                "bookmark_delay_stack INTEGER DEFAULT 0," +
                "bookmark_memo TEXT," +
                "PRIMARY KEY (bookmark_num, todo_ID)," +
                "FOREIGN KEY (todo_ID) REFERENCES TODOS(todo_ID) ON DELETE CASCADE" +
                ");");

        // ROUTINES 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS ROUTINES (" +
                "todo_ID INTEGER PRIMARY KEY," +
                "cycle TEXT NOT NULL," +
                "is_active INTEGER NOT NULL," +
                "FOREIGN KEY (todo_ID) REFERENCES TODOS(todo_ID) ON DELETE CASCADE" +
                ");");

        // TODO_LOGS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS TODO_LOGS (" +
                "log_num INTEGER PRIMARY KEY AUTOINCREMENT," +
                "todo_ID INTEGER," +
                "bookmark_num INTEGER," +
                "todo_state TEXT NOT NULL," +
                "timestamp TEXT DEFAULT (datetime('now'))," +
                "FOREIGN KEY (todo_ID) REFERENCES TODOS(todo_ID) ON DELETE SET NULL" +
                ");");

        // NAGS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS NAGS (" +
                "nag_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nag_txt TEXT NOT NULL" +
                ");");

        // TODOS_NAGS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS TODOS_NAGS (" +
                "todo_ID INTEGER," +
                "nag_ID INTEGER," +
                "PRIMARY KEY (todo_ID, nag_ID)," +
                "FOREIGN KEY (todo_ID) REFERENCES TODOS(todo_ID) ON DELETE CASCADE," +
                "FOREIGN KEY (nag_ID) REFERENCES NAGS(nag_ID) ON DELETE CASCADE" +
                ");");

        // BOOKMARKS_NAGS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS BOOKMARKS_NAGS (" +
                "bookmark_num INTEGER," +
                "todo_ID INTEGER," +
                "nag_ID INTEGER," +
                "PRIMARY KEY (bookmark_num, todo_ID, nag_ID)," +
                "FOREIGN KEY (bookmark_num, todo_ID) REFERENCES BOOKMARKS(bookmark_num, todo_ID) ON DELETE CASCADE," +
                "FOREIGN KEY (nag_ID) REFERENCES NAGS(nag_ID) ON DELETE CASCADE" +
                ");");

        // STATS 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS STATS (" +
                "stat_num INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reference_date TEXT NOT NULL," +
                "delay_num INTEGER DEFAULT 0," +
                "done_num INTEGER DEFAULT 0" +
                ");");

        //NAGS 테이블에 기본 데이터
        db.execSQL("INSERT INTO NAGS (nag_txt) VALUES " +
                "('오늘 할 일을 내일로 미루면 결국 하지 않게 됩니다.'), " +
                "('작은 것부터 시작해보세요. 완벽할 필요 없어요!'), " +
                "('지금 시작하지 않으면 평생 미룰 거에요.'), " +
                "('5분만 해보세요. 그 후엔 멈춰도 돼요.'), " +
                "('미루는 습관은 성공의 가장 큰 적입니다.');");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 필요시 테이블 삭제/재생성 로직 작성
    }
}
