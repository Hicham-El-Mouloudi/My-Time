package com.ensao.mytime.study.model;

import java.util.Date;

public class StudySession {
    private int id;
    private String subjectName;
    private Date startTime;
    private Date endTime;
    private int duration; // en minutes
    private boolean isCompleted;
    private String studyMethod; // "pomodoro" ou autre

    // Constructeurs
    public StudySession() {}

    public StudySession(String subjectName, int duration, String studyMethod) {
        this.subjectName = subjectName;
        this.duration = duration;
        this.studyMethod = studyMethod;
        this.startTime = new Date();
        this.isCompleted = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getStudyMethod() { return studyMethod; }
    public void setStudyMethod(String studyMethod) { this.studyMethod = studyMethod; }
}