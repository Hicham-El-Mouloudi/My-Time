package com.ensao.mytime.Activityfeature.DataAccess;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ensao.mytime.Activityfeature.Busniss.StatisticsStudySession;

import java.util.Date;
import java.util.List;

@Dao
public interface StatisticsStudySessionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(StatisticsStudySession session);

    @Query("SELECT * FROM StatisticsStudySession WHERE date = :date LIMIT 1")
    StatisticsStudySession getStudySessionByDate(Date date);

    @Query("SELECT * FROM StatisticsStudySession WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    List<StatisticsStudySession> getStudySessionsInRange(Date startDate, Date endDate);

    @Query("SELECT * FROM StatisticsStudySession ORDER BY date DESC LIMIT :limit")
    List<StatisticsStudySession> getRecentStudySessions(int limit);

    @Query("DELETE FROM StatisticsStudySession WHERE id = :id")
    int delete(long id);
}
