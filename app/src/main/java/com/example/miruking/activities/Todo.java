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

    public Routine toRoutine() {
        return new Routine(
                this.todoId,
                this.todoName,
                "월,수,금", // 👉 필요시 DB에 실제 요일 정보 저장해서 불러오도록 변경 가능
                true,       // 👉 루틴 활성화 상태도 DB에서 관리 중이면 가져와야 함
                this.todoMemo
        );
    }
}
