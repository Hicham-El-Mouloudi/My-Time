package com.ensao.mytime.Activityfeature.Busniss;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Course")
public class Course {

    @PrimaryKey(autoGenerate = true)
    private long id ;



    private String Title;
    private String description;


    //Getters =====================================
    public String getTitle() {
        return Title;
    }
    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }
    //Setters =====================================


    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }





    //constructors


    public void setId(long id) {
        this.id = id;
    }

    public Course() {
    }

    @Ignore
    public Course(String description, String title) {
        this.description = description;
        Title = title;
    }
}
