package com.ensao.mytime.Activityfeature.Busniss;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "activities",
    foreignKeys = {@ForeignKey(
            entity = Course.class,
            parentColumns = "id",
            childColumns = "courseID"
    ),@ForeignKey(
            entity = Category.class,
            parentColumns = "id",
            childColumns = "CategoryID"
    )})
public class userActivity {

    @PrimaryKey(autoGenerate = true)
    private int id ;

    @NonNull
    private String Title ;
    private String Description;


    private int CategoryID ;

    @NonNull
    private boolean IsActive;
    private Date StartDate;
    private Date EndDate ;

    private Date CreatedAt;
    private int  courseID;


    //======================================================setters
    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setCategoryID(int categoryID) {
        CategoryID = categoryID;
    }

    public void setIsActive(Boolean isActive) {
        IsActive = isActive;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }

    public void setCreatedAt(Date createdAt) {
        CreatedAt = createdAt;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }


    //==============getters


    public int getId() {
        return id;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public int getCategoryID() {
        return CategoryID;
    }

    public Boolean getIsActive() {
        return IsActive;
    }

    public Date getStartDate() {
        return StartDate;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public Date getCreatedAt() {
        return CreatedAt;
    }

    public int getCourseID() {
        return courseID;
    }

    public userActivity(String title, String description, int categoryID, Boolean isActive, Date startDate, Date endDate, Date createdAt, int courseID) {
        Title = title;
        Description = description;
        CategoryID = categoryID;
        IsActive = isActive;
        StartDate = startDate;
        EndDate = endDate;
        CreatedAt = createdAt;
        this.courseID = courseID;
    }




}
