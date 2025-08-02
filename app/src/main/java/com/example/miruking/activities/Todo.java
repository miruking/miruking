package com.example.miruking.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Todo {
    private int todoId;
    private String todoName;
    private String todoStartDate;
    private String todoEndDate;
    private String todoStartTime;
    private String todoEndTime;
    private String todoField;
    private int todoDelayStack;
    private String todoMemo;

    // 생성자
    public Todo(int todoId, String todoName, String todoStartDate, String todoEndDate,
                String todoStartTime, String todoEndTime, String todoField,
                int todoDelayStack, String todoMemo) {
        this.todoId = todoId;
        this.todoName = todoName;
        this.todoStartDate = todoStartDate;
        this.todoEndDate = todoEndDate;
        this.todoStartTime = todoStartTime;
        this.todoEndTime = todoEndTime;
        this.todoField = todoField;
        this.todoDelayStack = todoDelayStack;
        this.todoMemo = todoMemo;
    }

    // Getter 메소드들
    public int getTodoId() { return todoId; }
    public String getTodoName() { return todoName; }
    public String getTodoStartDate() { return todoStartDate; }
    public String getTodoEndDate() { return todoEndDate; }
    public String getTodoStartTime() { return todoStartTime; }
    public String getTodoEndTime() { return todoEndTime; }
    public String getTodoField() { return todoField; }
    public int getTodoDelayStack() { return todoDelayStack; }
    public String getTodoMemo() { return todoMemo; }

    //다른 일정 리스트 기능 추가후 작동하는지 확인해야함 25.06.03
    public Dday toDday() {
        return new Dday(
                this.todoId,
                this.todoName,
                this.todoEndDate,
                this.todoEndTime,
                this.todoMemo
        );
    }
    //루틴 수정에서 월 수 금만 불러오는 문제 수정(25.06.06)
    public Routine toRoutine(SQLiteDatabase db) {
        String cycle = "";
        boolean isActive = false;

        // ROUTINES 테이블에서 cycle과 is_active 가져오기
        Cursor cursor = db.rawQuery(
                "SELECT cycle, is_active FROM ROUTINES WHERE todo_ID = ?",
                new String[]{String.valueOf(this.todoId)}
        );
        if (cursor.moveToFirst()) {
            cycle = cursor.getString(0);
            isActive = cursor.getInt(1) == 1;
        }
        cursor.close();

        return new Routine(
                this.todoId,
                this.todoName,
                cycle,
                isActive,
                this.todoMemo
        );
    }
}
