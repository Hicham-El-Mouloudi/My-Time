package com.ensao.mytime.study.model;

public class DailyActivity {
    private long id;  // Database ID for editing/deleting
    private String date;
    private String time;      // Start time
    private String endTime;   // End time
    private String title;     // Activity title
    private String description;
    private long categoryId;  // Category ID for repetition

    // Constructor for backward compatibility
    public DailyActivity(String date, String time, String description) {
        this.id = -1;
        this.date = date;
        this.time = time;
        this.endTime = time;
        this.title = description;
        this.description = description;
        this.categoryId = -1;
    }

    // Constructor with ID for loading from database
    public DailyActivity(long id, String date, String time, String description) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.endTime = time;
        this.title = description;
        this.description = description;
        this.categoryId = -1;
    }

    // Constructor with all fields except categoryId
    public DailyActivity(long id, String date, String time, String endTime, String title, String description) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.endTime = endTime != null ? endTime : time;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.categoryId = -1;
    }

    // Full constructor with all fields including categoryId
    public DailyActivity(long id, String date, String time, String endTime, String title, String description, long categoryId) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.endTime = endTime != null ? endTime : time;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.categoryId = categoryId;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    // Format time range for display
    public String getTimeRange() {
        if (time != null && endTime != null && !time.equals(endTime)) {
            return time + " - " + endTime;
        }
        return time != null ? time : "";
    }
}