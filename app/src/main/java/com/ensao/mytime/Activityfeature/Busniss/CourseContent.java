package com.ensao.mytime.Activityfeature.Busniss;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "CourseContent",foreignKeys = @ForeignKey(
        entity=Course.class,
        parentColumns = "id",
        childColumns = "CourseID"
))
public class CourseContent {

    @PrimaryKey(autoGenerate = true)
    private int id ;


    @NonNull
    private String ContentPath;
    private int CourseID;


    public int getId() {
        return id;
    }

    public String getContentPath() {
        return ContentPath;
    }

    public int getCourseID() {
        return CourseID;
    }




    public void setCourseID(int courseID) {
        CourseID = courseID;
    }
    public void setContentPath(String contentPath) {
        ContentPath = contentPath;
    }


    public CourseContent(int courseID, String contentPath) {
        CourseID = courseID;
        ContentPath = contentPath;
    }
}
