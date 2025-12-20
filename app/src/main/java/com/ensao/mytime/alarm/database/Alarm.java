package com.ensao.mytime.alarm.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long timeInMillis;
    private boolean isEnabled = true;
    private int daysOfWeek; // Bitmask: Sun=1, Mon=2, ..., Sat=64
    private int ringtoneCode;
    private int vibrationCode;
    private String ringtoneUri;

    // Default Constructor
    public Alarm() {
    }

    public Alarm(long timeInMillis, boolean isEnabled, int daysOfWeek) {
        this.timeInMillis = timeInMillis;
        this.isEnabled = isEnabled;
        this.daysOfWeek = daysOfWeek;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(int daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getRingtoneCode() {
        return ringtoneCode;
    }

    public void setRingtoneCode(int ringtoneCode) {
        this.ringtoneCode = ringtoneCode;
    }

    public int getVibrationCode() {
        return vibrationCode;
    }

    public void setVibrationCode(int vibrationCode) {
        this.vibrationCode = vibrationCode;
    }

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }
}
