package com.ensao.mytime.sleep.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.statistics.StatisticsHelper;

public class WakeUpReceiver extends BroadcastReceiver {

    public static final String ACTION_WAKE_UP = "com.ensao.mytime.ACTION_WAKE_UP";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_WAKE_UP.equals(intent.getAction()))
            return;

        Log.d("WakeUpReceiver", "Réveil détecté : Fin de la session de nuit.");

        // Save sleep statistics before resetting the session
        StatisticsHelper.saveSleepStatistics(context);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Mise à jour des préférences pour indiquer que la session est terminée
        SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, false);
        editor.remove(AlarmScheduler.KEY_PREP_START_TIME);
        editor.apply();

        AlarmScheduler.cancelSleepPreparation(context);

        Log.d("WakeUpReceiver", "Session désactivée avec succès.");
    }
}
