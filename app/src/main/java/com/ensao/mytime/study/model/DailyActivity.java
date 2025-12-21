package com.ensao.mytime.study.model;

public class DailyActivity {
    private long id;  // Database ID for editing/deleting
    private String date;
    private String time;
    private String description;

    public DailyActivity(String date, String time, String description) {
        this.id = -1;  // -1 means not yet saved to database
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public DailyActivity(long id, String date, String time, String description) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}