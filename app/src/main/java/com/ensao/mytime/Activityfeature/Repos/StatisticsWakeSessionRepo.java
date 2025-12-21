package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsWakeSession;
import com.ensao.mytime.Activityfeature.DataAccess.StatisticsWakeSessionDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsWakeSessionRepo {

    private Executor _executor;
    private StatisticsWakeSessionDAO _wakeSessionDAO;

    public StatisticsWakeSessionRepo(Application app) {
        _wakeSessionDAO = ActivityRoomDB.getInstance(app).statisticsWakeSessionDAO();
        _executor = Executors.newSingleThreadExecutor();
    }

    public void insert(StatisticsWakeSession session, CallBackAfterDbOperation<Long> callback) {
        _executor.execute(() -> {
            long id = _wakeSessionDAO.insert(session);
            if (callback != null) {
                callback.onComplete(id);
            }
        });
    }

    public void getByDate(Date date, Activity currentActivity,
            CallBackAfterDbOperation<StatisticsWakeSession> callback) {
        _executor.execute(() -> {
            StatisticsWakeSession session = _wakeSessionDAO.getWakeSessionByDate(date);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(session));
            } else if (callback != null) {
                callback.onComplete(session);
            }
        });
    }

    public void getInRange(Date startDate, Date endDate, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsWakeSession>> callback) {
        _executor.execute(() -> {
            List<StatisticsWakeSession> sessions = _wakeSessionDAO.getWakeSessionsInRange(startDate, endDate);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    public void getRecent(int limit, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsWakeSession>> callback) {
        _executor.execute(() -> {
            List<StatisticsWakeSession> sessions = _wakeSessionDAO.getRecentWakeSessions(limit);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    // Synchronous methods for use when already on background thread
    public StatisticsWakeSession getByDateSync(Date date) {
        return _wakeSessionDAO.getWakeSessionByDate(date);
    }

    public List<StatisticsWakeSession> getInRangeSync(Date startDate, Date endDate) {
        return _wakeSessionDAO.getWakeSessionsInRange(startDate, endDate);
    }
}
