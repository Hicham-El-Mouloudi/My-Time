package com.ensao.mytime.Activityfeature.Busniss;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "StatisticsSleepSession", indices = { @Index(value = "date", unique = true) })
public class StatisticsSleepSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Date date;
    private float sleepDuration; // hours
    private int sleepEfficiency; // percentage
    private float timeInBed; // hours
    private int sleepLatency; // minutes to fall asleep
    private int wakeDuringSleep; // minutes awake during sleep
    private boolean hasSleep;

    // Default constructor for Room
    public StatisticsSleepSession() {
    }

    @Ignore
    public StatisticsSleepSession(Date date, float sleepDuration, int sleepEfficiency,
            float timeInBed, int sleepLatency, int wakeDuringSleep, boolean hasSleep) {
        this.date = date;
        this.sleepDuration = sleepDuration;
        this.sleepEfficiency = sleepEfficiency;
        this.timeInBed = timeInBed;
        this.sleepLatency = sleepLatency;
        this.wakeDuringSleep = wakeDuringSleep;
        this.hasSleep = hasSleep;
    }

    // Getters
    public long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public float getSleepDuration() {
        return sleepDuration;
    }

    public int getSleepEfficiency() {
        return sleepEfficiency;
    }

    public float getTimeInBed() {
        return timeInBed;
    }

    public int getSleepLatency() {
        return sleepLatency;
    }

    public int getWakeDuringSleep() {
        return wakeDuringSleep;
    }

    public boolean isHasSleep() {
        return hasSleep;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSleepDuration(float sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public void setSleepEfficiency(int sleepEfficiency) {
        this.sleepEfficiency = sleepEfficiency;
    }

    public void setTimeInBed(float timeInBed) {
        this.timeInBed = timeInBed;
    }

    public void setSleepLatency(int sleepLatency) {
        this.sleepLatency = sleepLatency;
    }

    public void setWakeDuringSleep(int wakeDuringSleep) {
        this.wakeDuringSleep = wakeDuringSleep;
    }

    public void setHasSleep(boolean hasSleep) {
        this.hasSleep = hasSleep;
    }
}
