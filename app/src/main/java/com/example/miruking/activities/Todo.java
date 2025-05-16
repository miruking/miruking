package com.example.miruking.activities;

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
}
