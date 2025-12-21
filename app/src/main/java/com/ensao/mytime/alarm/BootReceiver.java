package com.ensao.mytime.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ensao.mytime.alarm.database.Alarm;
import com.ensao.mytime.alarm.database.AlarmDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all enabled alarms after device reboot
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                AlarmDatabase database = AlarmDatabase.getInstance(context);
                List<Alarm> enabledAlarms = database.alarmDao().getEnabledAlarms();

                for (Alarm alarm : enabledAlarms) {
                    // Only reschedule if alarm time is in the future
                    if (alarm.getTimeInMillis() > System.currentTimeMillis()) {
                        AlarmScheduler.scheduleAlarm(context, alarm);
                    }
                }
            });
        }
    }
}
