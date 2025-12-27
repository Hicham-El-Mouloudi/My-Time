package com.ensao.mytime.statistics.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    // Study Data
    private boolean hasStudy;
    private int totalFocusTime; // minutes
    private int streakCount;
    private float pauseCount;
    private Map<String, Integer> subjectDistribution; // subject -> minutes
    private int completedTasksCount;
    private int totalTasksCount;
    private List<Integer> weeklySubjectsStudied; // 7 days

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

    // Study Getters and Setters
    public boolean hasStudy() {
        return hasStudy;
    }

    public void setHasStudy(boolean hasStudy) {
        this.hasStudy = hasStudy;
    }

    public int getTotalFocusTime() {
        return totalFocusTime;
    }

    public void setTotalFocusTime(int totalFocusTime) {
        this.totalFocusTime = totalFocusTime;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public float getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(float pauseCount) {
        this.pauseCount = pauseCount;
    }

    public Map<String, Integer> getSubjectDistribution() {
        return subjectDistribution;
    }

    public void setSubjectDistribution(Map<String, Integer> subjectDistribution) {
        this.subjectDistribution = subjectDistribution;
    }

    public int getCompletedTasksCount() {
        return completedTasksCount;
    }

    public void setCompletedTasksCount(int completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }

    public int getTotalTasksCount() {
        return totalTasksCount;
    }

    public void setTotalTasksCount(int totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public List<Integer> getWeeklySubjectsStudied() {
        return weeklySubjectsStudied;
    }

    public void setWeeklySubjectsStudied(List<Integer> weeklySubjectsStudied) {
        this.weeklySubjectsStudied = weeklySubjectsStudied;
    }
}
