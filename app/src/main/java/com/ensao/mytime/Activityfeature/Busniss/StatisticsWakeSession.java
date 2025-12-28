package com.ensao.mytime.Activityfeature.Busniss;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "StatisticsWakeSession", indices = { @Index(value = "date", unique = true) })
public class StatisticsWakeSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Date date;
    private int ringCount; // number of alarm rings
    private float timeVariability; // variation from expected wake time
    private String firstAlarm; // HH:mm format
    private String lastOff; // HH:mm format
    private float wakeDuration; // total wake process duration in minutes
    private boolean hasWake;

    // Default constructor for Room
    public StatisticsWakeSession() {
    }

    @Ignore
    public StatisticsWakeSession(Date date, int ringCount, float timeVariability,
            String firstAlarm, String lastOff, float wakeDuration, boolean hasWake) {
        this.date = date;
        this.ringCount = ringCount;
        this.timeVariability = timeVariability;
        this.firstAlarm = firstAlarm;
        this.lastOff = lastOff;
        this.wakeDuration = wakeDuration;
        this.hasWake = hasWake;
    }

    // Getters
    public long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public int getRingCount() {
        return ringCount;
    }

    public float getTimeVariability() {
        return timeVariability;
    }

    public String getFirstAlarm() {
        return firstAlarm;
    }

    public String getLastOff() {
        return lastOff;
    }

    public float getWakeDuration() {
        return wakeDuration;
    }

    public boolean isHasWake() {
        return hasWake;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
    }

    public void setTimeVariability(float timeVariability) {
        this.timeVariability = timeVariability;
    }

    public void setFirstAlarm(String firstAlarm) {
        this.firstAlarm = firstAlarm;
    }

    public void setLastOff(String lastOff) {
        this.lastOff = lastOff;
    }

    public void setWakeDuration(float wakeDuration) {
        this.wakeDuration = wakeDuration;
    }

    public void setHasWake(boolean hasWake) {
        this.hasWake = hasWake;
    }
}
