package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsSleepSession;
import com.ensao.mytime.Activityfeature.DataAccess.StatisticsSleepSessionDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsSleepSessionRepo {

    private Executor _executor;
    private StatisticsSleepSessionDAO _sleepSessionDAO;

    public StatisticsSleepSessionRepo(Application app) {
        _sleepSessionDAO = ActivityRoomDB.getInstance(app).statisticsSleepSessionDAO();
        _executor = Executors.newSingleThreadExecutor();
    }

    public void insert(StatisticsSleepSession session, CallBackAfterDbOperation<Long> callback) {
        _executor.execute(() -> {
            long id = _sleepSessionDAO.insert(session);
            if (callback != null) {
                callback.onComplete(id);
            }
        });
    }

    public void getByDate(Date date, Activity currentActivity,
            CallBackAfterDbOperation<StatisticsSleepSession> callback) {
        _executor.execute(() -> {
            StatisticsSleepSession session = _sleepSessionDAO.getSleepSessionByDate(date);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(session));
            } else if (callback != null) {
                callback.onComplete(session);
            }
        });
    }

    public void getInRange(Date startDate, Date endDate, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsSleepSession>> callback) {
        _executor.execute(() -> {
            List<StatisticsSleepSession> sessions = _sleepSessionDAO.getSleepSessionsInRange(startDate, endDate);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    public void getRecent(int limit, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsSleepSession>> callback) {
        _executor.execute(() -> {
            List<StatisticsSleepSession> sessions = _sleepSessionDAO.getRecentSleepSessions(limit);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    // Synchronous methods for use when already on background thread
    public StatisticsSleepSession getByDateSync(Date date) {
        return _sleepSessionDAO.getSleepSessionByDate(date);
    }

    public List<StatisticsSleepSession> getInRangeSync(Date startDate, Date endDate) {
        return _sleepSessionDAO.getSleepSessionsInRange(startDate, endDate);
    }
}
