package com.ensao.mytime.Activityfeature.Busniss;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Category",foreignKeys = @ForeignKey(
        entity = RepetitionKind.class,
        parentColumns = "id",
        childColumns = "RepetitionKindID"

))
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String Title;
    private String description;

    private int RepetitionKindID;


    //Getters ===========================================================


    public int getRepetitionKindID() {
        return RepetitionKindID;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return Title;
    }

    public int getId() {
        return id;
    }



    //Setters =====================================


    public void setRepetitionKindID(int repetitionKindID) {
        RepetitionKindID = repetitionKindID;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setTitle(String title) {
        Title = title;
    }




    //Constructor


    public Category(String description, String title, int repetitionKindID) {
        this.description = description;
        Title = title;
        RepetitionKindID = repetitionKindID;
    }




}
