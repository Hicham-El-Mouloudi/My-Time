package com.ensao.mytime.Activityfeature.Busniss;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ActivityHistory",foreignKeys = @ForeignKey(
        entity = userActivity.class,
        parentColumns = "id",
        childColumns = "ActivityID"
),indices = {@Index("ActivityID")})
public class ActivityHistory {

    @PrimaryKey(autoGenerate = true)
    private long id ;

    private long ActivityID;

    private Date StartDate;
    private Date EndDate;

    private Boolean IsCompleted;

    private String Notes;




    //=========================================================


    public long getId() {
        return id;
    }

    public long getActivityID() {
        return ActivityID;
    }

    public Date getStartDate() {
        return StartDate;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public String getNotes() {
        return Notes;
    }

    public Boolean getIsCompleted() {
        return IsCompleted;
    }



    //========================================


    public void setActivityID(long activityID) {
        ActivityID = activityID;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }



    public void setIsCompleted(Boolean completed) {
        IsCompleted = completed;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }





    //============================================


    public ActivityHistory() {
    }


    @Ignore
    public ActivityHistory(long activityID, Date startDate, Date endDate, Boolean isCompleted, String notes) {
        ActivityID = activityID;
        StartDate = startDate;
        EndDate = endDate;
        IsCompleted = isCompleted;
        Notes = notes;
    }



}
