package com.example.miruking.activities;

public class Dday {
    private int id;
    private String title;
    private String endDate;
    private String endTime;
    private String memo;

    public Dday(int id, String title, String endDate, String endTime, String memo) {
        this.id = id;
        this.title = title;
        this.endDate = endDate;
        this.endTime = endTime;
        this.memo = memo;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getEndDate() { return endDate; }
    public String getEndTime() { return endTime; }
    public String getMemo() { return memo; }
}
