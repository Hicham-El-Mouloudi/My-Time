package com.ensao.mytime.Activityfeature.Busniss;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "activities",
    foreignKeys = {@ForeignKey(
            entity = Course.class,
            parentColumns = "id",
            childColumns = "courseID",
            onDelete = ForeignKey.SET_NULL
    ),@ForeignKey(
            entity = Category.class,
            parentColumns = "id",
            childColumns = "CategoryID"
    )},
        indices = {@Index("courseID"),@Index("CategoryID")})
public class userActivity {

    @PrimaryKey(autoGenerate = true)
    private long id ;

    @NonNull
    private String Title ;
    private String Description;


    private long CategoryID ;

    @NonNull
    private boolean IsActive;
    private Date StartDate;
    private Date EndDate ;

    private Date CreatedAt;
    private Long courseID;  // Changed to Long (nullable) to allow null foreign key


    //======================================================setters
    public void setTitle(String title) {
        Title = title;
    }


    public boolean isActive() {
        return IsActive;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setCategoryID(long categoryID) {
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

    public void setCourseID(Long courseID) {
        this.courseID = courseID;
    }


    //==============getters


    public long getId() {
        return id;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public long getCategoryID() {
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

    public Long getCourseID() {
        return courseID;
    }


    public userActivity() {
    }

    @Ignore
    public userActivity(String title, String description, long categoryID, Boolean isActive, Date startDate, Date endDate, Date createdAt, Long courseID) {
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
