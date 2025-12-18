package com.ensao.mytime.alarm.database;


import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {
    private AlarmDao dao;
    private LiveData<List<Alarm>> allAlarms;
    private ExecutorService executorService;
    private final Handler mainThreadHandler;

    //
    public AlarmRepository(Application application){
        AlarmDatabase database = AlarmDatabase.getInstance(application);
        dao = database.alarmDao();
        allAlarms = dao.getAllAlarms();
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }
    public interface OnAlarmInsertedListener {
        void onAlarmInserted(Alarm alarm);
    }
    public void insert(Alarm alarm, OnAlarmInsertedListener listener){
        executorService.execute(() -> {
            long id = dao.insert(alarm);
            alarm.setId((int) id);
            if (listener != null){
                mainThreadHandler.post(() -> listener.onAlarmInserted(alarm));
            }
        });
    }
    public void update(Alarm alarm){
        executorService.execute(() -> dao.update(alarm));
    }
    public void delete (Alarm alarm){
        executorService.execute(() -> dao.delete(alarm));
    }
    public LiveData<List<Alarm>> getAllAlarms(){
        return allAlarms;
    }
    public List<Alarm> getEnabledAlarmsSync(){
        return dao.getEnabledAlarms();
    }
    public Alarm getAlarmByIdSync(int id){
        return dao.getAlarmById(id);
    }

}
