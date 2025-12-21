package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsWakeSession;

import java.util.Date;
import java.util.List;

@Dao
public interface StatisticsWakeSessionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StatisticsWakeSession session);

    @Query("SELECT * FROM StatisticsWakeSession WHERE date = :date LIMIT 1")
    StatisticsWakeSession getWakeSessionByDate(Date date);

    @Query("SELECT * FROM StatisticsWakeSession WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    List<StatisticsWakeSession> getWakeSessionsInRange(Date startDate, Date endDate);

    @Query("SELECT * FROM StatisticsWakeSession ORDER BY date DESC LIMIT :limit")
    List<StatisticsWakeSession> getRecentWakeSessions(int limit);

    @Query("DELETE FROM StatisticsWakeSession WHERE id = :id")
    int delete(long id);
}
