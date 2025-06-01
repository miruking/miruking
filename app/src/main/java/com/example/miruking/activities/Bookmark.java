package com.example.miruking.activities;

public class Bookmark {
    private int bookmarkNum;
    private int todoId;
    private String title;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private int delay;
    private String memo;

    public Bookmark(int bookmarkNum, int todoId, String title,
                    String startDate, String startTime,
                    String endDate, String endTime,
                    int delay, String memo) {
        this.bookmarkNum = bookmarkNum;
        this.todoId = todoId;
        this.title = title;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.delay = delay;
        this.memo = memo;
    }

    public int getBookmarkNum() { return bookmarkNum; }
    public int getTodoId() { return todoId; }
    public String getTitle() { return title; }
    public String getStartDate() { return startDate; }
    public String getStartTime() { return startTime; }
    public String getEndDate() { return endDate; }
    public String getEndTime() { return endTime; }
    public int getDelay() { return delay; }
    public String getMemo() { return memo; }
}
