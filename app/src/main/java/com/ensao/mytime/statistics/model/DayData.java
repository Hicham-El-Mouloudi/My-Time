package com.ensao.mytime.statistics.model;

import java.time.LocalDate;
import java.util.List;

public class DayData {
    private LocalDate date;
    private boolean hasSleep;
    private boolean hasWake;

    // Sleep Data
    private float sleepDuration; // hours
    private int sleepEfficiency; // %
    private float timeInBed; // hours
    private int sleepLatency; // minutes
    private int wakeDuringSleep; // minutes

    // Wake Data
    private int wakeLatency; // minutes
    private int ringCount;
    private float timeVariability;
    private String firstAlarm; // HH:mm
    private String lastOff; // HH:mm
    private float wakeDuration; // minutes
    private List<Float> wakeVariance; // last 7 days
    private String averageWakeTime; // HH:mm

    public DayData(LocalDate date, boolean hasSleep, boolean hasWake) {
        this.date = date;
        this.hasSleep = hasSleep;
        this.hasWake = hasWake;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean hasSleep() {
        return hasSleep;
    }

    public void setHasSleep(boolean hasSleep) {
        this.hasSleep = hasSleep;
    }

    public boolean hasWake() {
        return hasWake;
    }

    public void setHasWake(boolean hasWake) {
        this.hasWake = hasWake;
    }

    public float getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(float sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public int getWakeEfficiency() {
        return 60;
    }

    public int getSleepEfficiency() {
        return sleepEfficiency;
    }

    public void setSleepEfficiency(int sleepEfficiency) {
        this.sleepEfficiency = sleepEfficiency;
    }

    public float getTimeInBed() {
        return timeInBed;
    }

    public void setTimeInBed(float timeInBed) {
        this.timeInBed = timeInBed;
    }

    public int getSleepLatency() {
        return sleepLatency;
    }

    public void setSleepLatency(int sleepLatency) {
        this.sleepLatency = sleepLatency;
    }

    public int getWakeDuringSleep() {
        return wakeDuringSleep;
    }

    public void setWakeDuringSleep(int wakeDuringSleep) {
        this.wakeDuringSleep = wakeDuringSleep;
    }

    public int getWakeLatency() {
        return wakeLatency;
    }

    public void setWakeLatency(int wakeLatency) {
        this.wakeLatency = wakeLatency;
    }

    public int getRingCount() {
        return ringCount;
    }

    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
    }

    public float getTimeVariability() {
        return timeVariability;
    }

    public void setTimeVariability(float timeVariability) {
        this.timeVariability = timeVariability;
    }

    public String getFirstAlarm() {
        return firstAlarm;
    }

    public void setFirstAlarm(String firstAlarm) {
        this.firstAlarm = firstAlarm;
    }

    public String getLastOff() {
        return lastOff;
    }

    public void setLastOff(String lastOff) {
        this.lastOff = lastOff;
    }

    public float getWakeDuration() {
        return wakeDuration;
    }

    public void setWakeDuration(float wakeDuration) {
        this.wakeDuration = wakeDuration;
    }

    public List<Float> getWakeVariance() {
        return wakeVariance;
    }

    public void setWakeVariance(List<Float> wakeVariance) {
        this.wakeVariance = wakeVariance;
    }

    public String getAverageWakeTime() {
        return averageWakeTime;
    }

    public void setAverageWakeTime(String averageWakeTime) {
        this.averageWakeTime = averageWakeTime;
    }
}
