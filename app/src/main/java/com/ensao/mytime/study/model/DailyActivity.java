package com.ensao.mytime.study.model;

public class DailyActivity {
    private String date;
    private String time;
    private String description;

    public DailyActivity(String date, String time, String description) {
        this.date = date;
        this.time = time;
        this.description = description;
    }

    // Getters et Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}