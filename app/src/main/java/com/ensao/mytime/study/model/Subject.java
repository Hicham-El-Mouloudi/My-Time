package com.ensao.mytime.study.model;

import java.util.Date;

public class Subject {
    private int id;
    private String name;
    private boolean isCompleted;
    private Date createdAt;

    public Subject(String name) {
        this.name = name;
        this.isCompleted = false;
        this.createdAt = new Date();
    }

    public Subject(int id, String name, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.isCompleted = isCompleted;
        this.createdAt = new Date();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}