package com.ensao.mytime.Activityfeature.Busniss;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "RepetitionKind")
public class RepetitionKind {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String Title;


    public RepetitionKind() {
    }

    public void setId(long id) {
        this.id = id;
    }

    @Ignore
    public RepetitionKind(String title) {
        Title = title;
    }







    public String getTitle() {
        return Title;
    }
    public long getId() {
        return id;
    }


    public void setTitle(String title) {
        Title = title;
    }
}
