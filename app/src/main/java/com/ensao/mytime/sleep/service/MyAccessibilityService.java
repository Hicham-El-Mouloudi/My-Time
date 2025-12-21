package com.ensao.mytime.sleep.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.MainActivity;

import java.util.HashSet;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() == null) return;

            String packageName = event.getPackageName().toString();

            // ✅ SECURITÉ : Ne jamais bloquer l'application
            if (packageName.equals(getPackageName())) {
                return;
            }

            SharedPreferences prefs = getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

            // 1. Vérifier si le bouton global "Session Nuit" est activé
            boolean isSessionActive = prefs.getBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, false);
            if (!isSessionActive) return;

            // 2. Vérifier si nous sommes dans la fenêtre temporelle (Coucher - 2h -> Réveil)
            long now = System.currentTimeMillis();
            long sleepTime = prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, 0);
            long wakeUpTime = prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, 0);
            long startTime = sleepTime - (2 * 60 * 60 * 1000); // Heure coucher - 2 heures

            // Logique de vérification de la fenêtre (gère le passage à minuit)
            boolean isInWindow;
            if (wakeUpTime > startTime) {
                isInWindow = (now >= startTime && now <= wakeUpTime);
            } else {
                isInWindow = (now >= startTime || now <= wakeUpTime);
            }

            // 3. Si on est dans la fenêtre, vérifier si l'app ouverte est dans la liste noire
            if (isInWindow) {
                Set<String> blockedApps = prefs.getStringSet(AlarmScheduler.KEY_BLOCKED_APPS_SET, new HashSet<>());
                if (blockedApps.contains(packageName)) {
                    // Rediriger vers MyTime avec l'extra pour afficher le dialogue
                    Intent lockIntent = new Intent(this, MainActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    lockIntent.putExtra("is_blocked_mode", true);
                    startActivity(lockIntent);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Le service d'accessibilité a été interrompu.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Le service d'accessibilité est connecté et prêt.");
    }
}