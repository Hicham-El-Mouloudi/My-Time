package com.ensao.mytime.Activityfeature.Busniss;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Category",foreignKeys = @ForeignKey(
        entity = RepetitionKind.class,
        parentColumns = "id",
        childColumns = "RepetitionKindID"

),indices = {@Index("RepetitionKindID")})
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String Title;
    private String description;

    private long RepetitionKindID;


    //Getters ===========================================================


    public long getRepetitionKindID() {
        return RepetitionKindID;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return Title;
    }

    public long getId() {
        return id;
    }



    //Setters =====================================


    public void setRepetitionKindID(long repetitionKindID) {
        RepetitionKindID = repetitionKindID;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setTitle(String title) {
        Title = title;
    }



    public void setId(long id) {
        this.id = id;
    }

    //Constructor
    public Category() {

    }


    @Ignore
    public Category(String description, String title, long repetitionKindID) {
        this.description = description;
        Title = title;
        RepetitionKindID = repetitionKindID;
    }




}
