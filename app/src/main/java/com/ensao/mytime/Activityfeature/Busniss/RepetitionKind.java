package com.ensao.mytime.Activityfeature.Busniss;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "RepetitionKind")
public class RepetitionKind {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String Title;

    public RepetitionKind(String title) {
        Title = title;
    }


    public String getTitle() {
        return Title;
    }
    public int getId() {
        return id;
    }


    public void setTitle(String title) {
        Title = title;
    }
}
