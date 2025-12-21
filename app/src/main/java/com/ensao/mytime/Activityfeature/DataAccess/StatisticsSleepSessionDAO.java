package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsSleepSession;

import java.util.Date;
import java.util.List;

@Dao
public interface StatisticsSleepSessionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StatisticsSleepSession session);

    @Query("SELECT * FROM StatisticsSleepSession WHERE date = :date LIMIT 1")
    StatisticsSleepSession getSleepSessionByDate(Date date);

    @Query("SELECT * FROM StatisticsSleepSession WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    List<StatisticsSleepSession> getSleepSessionsInRange(Date startDate, Date endDate);

    @Query("SELECT * FROM StatisticsSleepSession ORDER BY date DESC LIMIT :limit")
    List<StatisticsSleepSession> getRecentSleepSessions(int limit);

    @Query("DELETE FROM StatisticsSleepSession WHERE id = :id")
    int delete(long id);
}
