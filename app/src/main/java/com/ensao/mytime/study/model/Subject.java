package com.ensao.mytime.study.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import com.ensao.mytime.study.database.DateConverter;
import java.util.Date;

@Entity(tableName = "subjects")
@TypeConverters({ DateConverter.class })
public class Subject {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted = false;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    // Constructeur pour Room
    public Subject() {
        this.createdAt = new Date();
    }

    // Constructeur pour votre code
    public Subject(String name) {
        this.name = name;
        this.createdAt = new Date();
    }

    // Constructeur existant pour compatibilité
    public Subject(int id, String name, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.isCompleted = isCompleted;
        this.createdAt = new Date();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}