package com.ensao.mytime.alarm.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    long insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarm ORDER BY timeInMillis ASC")
    LiveData<List<Alarm>> getAllAlarms();
    @Query("SELECT * FROM alarm WHERE id = :id")
    Alarm getAlarmById(int id);
    @Query("SELECT * FROM alarm WHERE isEnabled = 1 ORDER BY timeInMillis ASC")
    List<Alarm> getEnabledAlarms();

}
