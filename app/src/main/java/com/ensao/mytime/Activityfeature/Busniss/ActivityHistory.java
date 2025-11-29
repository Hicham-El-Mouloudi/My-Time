package com.ensao.mytime.Activityfeature.Busniss;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ActivityHistory",foreignKeys = @ForeignKey(
        entity = userActivity.class,
        parentColumns = "id",
        childColumns = "ActivityID"
))
public class ActivityHistory {

    @PrimaryKey(autoGenerate = true)
    private int id ;

    private int ActivityID;

    private Date StartDate;
    private Date EndDate;

    private Boolean IsCompleted;

    private String Notes;




    //=========================================================


    public int getId() {
        return id;
    }

    public int getActivityID() {
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

    public Boolean getCompleted() {
        return IsCompleted;
    }



    //========================================


    public void setActivityID(int activityID) {
        ActivityID = activityID;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }

    public void setCompleted(Boolean completed) {
        IsCompleted = completed;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }





    //============================================


    public ActivityHistory(int activityID, Date startDate, Date endDate, Boolean isCompleted, String notes) {
        ActivityID = activityID;
        StartDate = startDate;
        EndDate = endDate;
        IsCompleted = isCompleted;
        Notes = notes;
    }



}
