package com.ensao.mytime.Activityfeature.Busniss;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "CourseContent",foreignKeys = @ForeignKey(
        entity=Course.class,
        parentColumns = "id",
        childColumns = "CourseID"
),indices = {@Index("CourseID")})
public class CourseContent {

    @PrimaryKey(autoGenerate = true)
    private long id ;


    @NonNull
    private String ContentPath;
    private long CourseID;


    public long getId() {
        return id;
    }

    public String getContentPath() {
        return ContentPath;
    }

    public long getCourseID() {
        return CourseID;
    }




    public void setCourseID(long courseID) {
        CourseID = courseID;
    }
    public void setContentPath(String contentPath) {
        ContentPath = contentPath;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CourseContent() {
    }

    @Ignore
    public CourseContent(long courseID, String contentPath) {
        CourseID = courseID;
        ContentPath = contentPath;
    }
}
