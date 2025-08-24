package com.example.miruking.utils;

public class NotificationDTO {
    private int t_id;  // Todo ID
    private String title;
    private String description;
    private boolean isBookmarked;

    private String startDate;
    private String endDate;
    private int delayStack;

    private String bookmarkName;
    private Integer b_id;  // ✅ 북마크 ID (nullable)

    public NotificationDTO(int t_id, String title, String description, boolean isBookmarked,
                           String startDate, String endDate, int delayStack,
                           String bookmarkName, Integer b_id) {
        this.t_id = t_id;
        this.title = title;
        this.description = description;
        this.isBookmarked = isBookmarked;
        this.startDate = startDate;
        this.endDate = endDate;
        this.delayStack = delayStack;
        this.bookmarkName = bookmarkName;
        this.b_id = b_id;
    }

    public int getT_id() {
        return t_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getDelayStack() {
        return delayStack;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public Integer getB_id() {
        return b_id;
    }
}