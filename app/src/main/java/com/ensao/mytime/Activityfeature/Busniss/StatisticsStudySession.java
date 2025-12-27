package com.ensao.mytime.Activityfeature.Busniss;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "StatisticsStudySession", indices = { @Index(value = "date", unique = true) })
public class StatisticsStudySession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Date date;
    private int totalFocusTime; // minutes - aggregate sum of completed Pomodoro intervals
    private int streakCount; // consecutive days with at least one session
    private float pauseCount; // average pauses per session
    private String subjectDistribution; // JSON: {"Math": 45, "Physics": 30} (subject -> minutes)
    private int completedTasksCount; // tasks completed on this day
    private int totalTasksCount; // total tasks for this day
    private int subjectsStudiedCount; // distinct subjects studied
    private boolean hasStudy;

    // Default constructor for Room
    public StatisticsStudySession() {
    }

    @Ignore
    public StatisticsStudySession(Date date, int totalFocusTime, int streakCount, float pauseCount,
            String subjectDistribution, int completedTasksCount, int totalTasksCount,
            int subjectsStudiedCount, boolean hasStudy) {
        this.date = date;
        this.totalFocusTime = totalFocusTime;
        this.streakCount = streakCount;
        this.pauseCount = pauseCount;
        this.subjectDistribution = subjectDistribution;
        this.completedTasksCount = completedTasksCount;
        this.totalTasksCount = totalTasksCount;
        this.subjectsStudiedCount = subjectsStudiedCount;
        this.hasStudy = hasStudy;
    }

    // Getters
    public long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public int getTotalFocusTime() {
        return totalFocusTime;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public float getPauseCount() {
        return pauseCount;
    }

    public String getSubjectDistribution() {
        return subjectDistribution;
    }

    public int getCompletedTasksCount() {
        return completedTasksCount;
    }

    public int getTotalTasksCount() {
        return totalTasksCount;
    }

    public int getSubjectsStudiedCount() {
        return subjectsStudiedCount;
    }

    public boolean isHasStudy() {
        return hasStudy;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTotalFocusTime(int totalFocusTime) {
        this.totalFocusTime = totalFocusTime;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public void setPauseCount(float pauseCount) {
        this.pauseCount = pauseCount;
    }

    public void setSubjectDistribution(String subjectDistribution) {
        this.subjectDistribution = subjectDistribution;
    }

    public void setCompletedTasksCount(int completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }

    public void setTotalTasksCount(int totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    public void setSubjectsStudiedCount(int subjectsStudiedCount) {
        this.subjectsStudiedCount = subjectsStudiedCount;
    }

    public void setHasStudy(boolean hasStudy) {
        this.hasStudy = hasStudy;
    }
}
