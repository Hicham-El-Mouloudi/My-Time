package com.ensao.mytime.alarm.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timeInMillis;
    private String label;
    private boolean isEnabled;

    public Alarm(long timeInMillis, String label, boolean isEnabled) {
        this.timeInMillis = timeInMillis;
        this.label = label;
        this.isEnabled = isEnabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
