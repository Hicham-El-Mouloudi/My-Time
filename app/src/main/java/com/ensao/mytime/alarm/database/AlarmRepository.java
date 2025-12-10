package com.ensao.mytime.alarm.database;


import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {
    private AlarmDao dao;
    private LiveData<List<Alarm>> allAlarms;
    private ExecutorService executorService;

    //
    public AlarmRepository(Application application){
        AlarmDatabase database = AlarmDatabase.getInstance(application);
        dao = database.alarmDao();
        allAlarms = dao.getAllAlarms();
        executorService = Executors.newSingleThreadExecutor();
    }
    public interface OnAlarmInsertedListener {
        void onAlarmInserted(Alarm alarm);
    }
    public void insert(Alarm alarm, OnAlarmInsertedListener listener){
        long id = dao.insert(alarm);
        alarm.setId((int) id);
        executorService.execute(() -> {
            if (listener != null){
                listener.onAlarmInserted(alarm);
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
