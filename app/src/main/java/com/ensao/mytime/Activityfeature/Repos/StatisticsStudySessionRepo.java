package com.ensao.mytime.Activityfeature.Repos;

import android.app.Activity;
import android.app.Application;

import com.ensao.mytime.Activityfeature.ActivityRoomDB;
import com.ensao.mytime.Activityfeature.Busniss.StatisticsStudySession;
import com.ensao.mytime.Activityfeature.DataAccess.StatisticsStudySessionDAO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsStudySessionRepo {

    private Executor _executor;
    private StatisticsStudySessionDAO _studySessionDAO;

    public StatisticsStudySessionRepo(Application app) {
        _studySessionDAO = ActivityRoomDB.getInstance(app).statisticsStudySessionDAO();
        _executor = Executors.newSingleThreadExecutor();
    }

    public void insert(StatisticsStudySession session, CallBackAfterDbOperation<Long> callback) {
        _executor.execute(() -> {
            long id = _studySessionDAO.insert(session);
            if (callback != null) {
                callback.onComplete(id);
            }
        });
    }

    public void getByDate(Date date, Activity currentActivity,
            CallBackAfterDbOperation<StatisticsStudySession> callback) {
        _executor.execute(() -> {
            StatisticsStudySession session = _studySessionDAO.getStudySessionByDate(date);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(session));
            } else if (callback != null) {
                callback.onComplete(session);
            }
        });
    }

    public void getInRange(Date startDate, Date endDate, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsStudySession>> callback) {
        _executor.execute(() -> {
            List<StatisticsStudySession> sessions = _studySessionDAO.getStudySessionsInRange(startDate, endDate);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    public void getRecent(int limit, Activity currentActivity,
            CallBackAfterDbOperation<List<StatisticsStudySession>> callback) {
        _executor.execute(() -> {
            List<StatisticsStudySession> sessions = _studySessionDAO.getRecentStudySessions(limit);
            if (callback != null && currentActivity != null) {
                currentActivity.runOnUiThread(() -> callback.onComplete(sessions));
            } else if (callback != null) {
                callback.onComplete(sessions);
            }
        });
    }

    // Synchronous methods for use when already on background thread
    public StatisticsStudySession getByDateSync(Date date) {
        return _studySessionDAO.getStudySessionByDate(date);
    }

    public List<StatisticsStudySession> getInRangeSync(Date startDate, Date endDate) {
        return _studySessionDAO.getStudySessionsInRange(startDate, endDate);
    }
}
