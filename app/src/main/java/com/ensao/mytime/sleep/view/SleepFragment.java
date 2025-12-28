package com.ensao.mytime.sleep.view;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ensao.mytime.MainActivity;
import com.ensao.mytime.R;
import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.sleep.service.MyAccessibilityService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SleepFragment extends Fragment {

    private TextView tvWakeUpTime;
    private TextView tvSelectedSleepTime;
    private TextView tvSuggestedSleepTimes;
    private Button btnToggleSleepSession;
    private Button btnManageApps;

    private Calendar selectedWakeUpCalendar;
    private Calendar selectedSleepTime;
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Initialization
        btnToggleSleepSession = view.findViewById(R.id.btn_toggle_sleep_session);
        tvWakeUpTime = view.findViewById(R.id.tv_wake_up_time);
        tvSuggestedSleepTimes = view.findViewById(R.id.tv_suggested_sleep_times);
        btnManageApps = view.findViewById(R.id.btn_manage_blocked_apps);
        tvSelectedSleepTime = view.findViewById(R.id.tv_selected_sleep_time);

        selectedWakeUpCalendar = Calendar.getInstance();
        selectedWakeUpCalendar.set(Calendar.HOUR_OF_DAY, 7);
        selectedWakeUpCalendar.set(Calendar.MINUTE, 0);

        selectedSleepTime = Calendar.getInstance();
        selectedSleepTime.set(Calendar.HOUR_OF_DAY, 22);
        selectedSleepTime.set(Calendar.MINUTE, 30);
        selectedSleepTime.set(Calendar.SECOND, 0);

        loadSleepSettings();
        updateUI();

        // Click Listeners
        tvWakeUpTime.setOnClickListener(v -> showWakeUpTimePicker());
        tvSelectedSleepTime.setOnClickListener(v -> showSleepTimePicker()); // Now clickable directly

        btnToggleSleepSession.setOnClickListener(v -> toggleSleepSession());

        if (btnManageApps != null) {
            btnManageApps.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Animation fluide
                        .replace(R.id.mainContent, new AppSelectionFragment())
                        .addToBackStack(null)
                        .commit();
            });

        }
        checkCurrentSessionStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).checkNightModeDynamic();
        }
    }

    private void toggleSleepSession() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME,
                Context.MODE_PRIVATE);
        boolean isActive = prefs.getBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, false);

        if (!isActive) {
            checkAndScheduleAlarms();
        } else {
            cancelAllAlarmsAndServices();
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).checkNightModeDynamic();
        }
    }

    private void checkAndScheduleAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            showOverlayPermissionDialog();
            return;
        }

        if (!isAccessibilityServiceEnabled(requireContext())) {
            showAccessibilityInstructions();
            return;
        }

        // Check usage stats permission for wake detection during sleep
        if (!ensureUsagePermission()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                Toast.makeText(requireContext(), "Alarm permission required.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        scheduleAllAlarmsAndServices();
    }

    /**
     * Checks if the app has usage stats permission.
     * If not, prompts the user to grant it in settings.
     * 
     * @return true if permission is granted, false otherwise
     */
    private boolean ensureUsagePermission() {
        AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            showUsageAccessDialog();
            return false;
        }
        return true;
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Usage Access Permission")
                .setMessage(
                        "MyTime needs usage access to detect phone usage during sleep for accurate sleep statistics.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isAccessibilityServiceEnabled(Context context) {
        String expectedService = context.getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(expectedService);
    }

    private void showAccessibilityInstructions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Blocking Permission")
                .setMessage("To block apps, MyTime needs Accessibility permission.")
                .setPositiveButton("Settings",
                        (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showOverlayPermissionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Display Over Apps")
                .setMessage("MyTime needs this permission to show the blocking alert.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void scheduleAllAlarmsAndServices() {
        Calendar now = Calendar.getInstance();
        Calendar sleepTime = getRetainedSleepTime();

        // 1. Planification du blocage (2h avant)
        Calendar blockStartTime = (Calendar) sleepTime.clone();
        blockStartTime.add(Calendar.HOUR_OF_DAY, -2);
        AlarmScheduler.scheduleSleepPreparation(requireContext(), blockStartTime);

        // 2. Planification du Réveil
        Calendar wakeUpTime = (Calendar) selectedWakeUpCalendar.clone();
        if (wakeUpTime.before(now)) {
            wakeUpTime.add(Calendar.DAY_OF_YEAR, 1);
        }
        AlarmScheduler.scheduleWakeUpAlarm(requireContext(), wakeUpTime);

        saveSleepSettings(true);
        updateToggleButtonState(true);
        Toast.makeText(requireContext(), "Session Scheduled! 🌙", Toast.LENGTH_SHORT).show();
    }

    private void cancelAllAlarmsAndServices() {
        saveSleepSettings(false);
        AlarmScheduler.cancelSleepPreparation(requireContext());
        AlarmScheduler.cancelWakeUpAlarm(requireContext());

        updateToggleButtonState(false);
        Toast.makeText(requireContext(), "Session Disabled.", Toast.LENGTH_SHORT).show();
    }

    private void saveSleepSettings(boolean isActive) {
        refreshSleepTimeDate();
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(AlarmScheduler.KEY_WAKE_UP_TIME, selectedWakeUpCalendar.getTimeInMillis());
        editor.putLong(AlarmScheduler.KEY_SLEEP_TIME, selectedSleepTime.getTimeInMillis());
        editor.putBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, isActive);
        editor.apply();
    }

    private void loadSleepSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME,
                Context.MODE_PRIVATE);
        if (prefs.contains(AlarmScheduler.KEY_WAKE_UP_TIME)) {
            selectedWakeUpCalendar.setTimeInMillis(
                    prefs.getLong(AlarmScheduler.KEY_WAKE_UP_TIME, selectedWakeUpCalendar.getTimeInMillis()));
            selectedSleepTime
                    .setTimeInMillis(prefs.getLong(AlarmScheduler.KEY_SLEEP_TIME, selectedSleepTime.getTimeInMillis()));
        }
    }

    private void updateUI() {
        tvWakeUpTime.setText(timeFormatter.format(selectedWakeUpCalendar.getTime()));
        if (tvSelectedSleepTime != null) {
            // Just display the time, label is in layout
            tvSelectedSleepTime.setText(timeFormatter.format(selectedSleepTime.getTime()));
        }
        tvSuggestedSleepTimes
                .setText("Recommended Bedtimes:\n\n" + calculateSuggestedSleepTimes(selectedWakeUpCalendar));
    }

    private String calculateSuggestedSleepTimes(Calendar wakeUpCalendar) {
        StringBuilder sb = new StringBuilder();
        int fallingAsleepTime = 15;
        for (int i = 1; i <= 6; i++) {
            Calendar suggestedCalendar = (Calendar) wakeUpCalendar.clone();
            int totalMinutesBack = (i * 90) + fallingAsleepTime;
            suggestedCalendar.add(Calendar.MINUTE, -totalMinutesBack);
            String time = timeFormatter.format(suggestedCalendar.getTime());
            double hoursOfSleep = i * 1.5;
            sb.append("• ").append(time).append(" (").append(i).append(i > 1 ? " cycles, " : " cycle, ")
                    .append(hoursOfSleep).append("h sleep)");
            if (i >= 4)
                sb.append(" ⭐");
            sb.append("\n");
        }
        return sb.toString();
    }

    private void updateToggleButtonState(boolean isActive) {
        if (isActive) {
            btnToggleSleepSession.setText("Disable Night Session");
            btnToggleSleepSession
                    .setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_red));
        } else {
            btnToggleSleepSession.setText("Activate Night Session");
            btnToggleSleepSession
                    .setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.success_green));
        }
    }

    private void showWakeUpTimePicker() {
        new TimePickerDialog(requireContext(), (view, h, m) -> {
            selectedWakeUpCalendar.set(Calendar.HOUR_OF_DAY, h);
            selectedWakeUpCalendar.set(Calendar.MINUTE, m);
            updateUI();
        }, selectedWakeUpCalendar.get(Calendar.HOUR_OF_DAY), selectedWakeUpCalendar.get(Calendar.MINUTE), true).show();
    }

    private void showSleepTimePicker() {
        new TimePickerDialog(requireContext(), (view, h, m) -> {
            selectedSleepTime.set(Calendar.HOUR_OF_DAY, h);
            selectedSleepTime.set(Calendar.MINUTE, m);
            selectedSleepTime.set(Calendar.SECOND, 0);
            updateUI();
        }, selectedSleepTime.get(Calendar.HOUR_OF_DAY), selectedSleepTime.get(Calendar.MINUTE), true).show();
    }

    private void refreshSleepTimeDate() {
        Calendar now = Calendar.getInstance();
        selectedSleepTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
        selectedSleepTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
        selectedSleepTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
        if (selectedSleepTime.getTimeInMillis() < (now.getTimeInMillis() - 60000)) {
            selectedSleepTime.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void checkCurrentSessionStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME,
                Context.MODE_PRIVATE);
        updateToggleButtonState(prefs.getBoolean(AlarmScheduler.KEY_IS_SESSION_ACTIVE, false));
    }

    private Calendar getRetainedSleepTime() {
        Calendar calendar = (Calendar) selectedSleepTime.clone();
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DATE, 1);
        return calendar;
    }
}