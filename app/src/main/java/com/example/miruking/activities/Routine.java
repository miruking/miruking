package com.example.miruking.activities;

public class Routine {
    private int id;
    private String title;
    private String cycle; // ex: "월,수,금"
    private boolean isActive;
    private String memo;

    public Routine(int id, String title, String cycle, boolean isActive, String memo) {
        this.id = id;
        this.title = title;
        this.cycle = cycle;
        this.isActive = isActive;
        this.memo = memo;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCycle() { return cycle; }
    public boolean isActive() { return isActive; }
    public String getMemo() { return memo; }
}
