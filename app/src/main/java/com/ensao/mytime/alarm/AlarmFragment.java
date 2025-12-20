package com.ensao.mytime.alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.Alarm;
import com.ensao.mytime.alarm.database.AlarmRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmFragment extends Fragment implements AlarmAdapter.OnAlarmActionListener {

    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private AlarmRepository repository;
    private FloatingActionButton fabAddAlarm;
    private FloatingActionButton fabDeleteSelected;
    private View emptyState;

    // Clock views
    private AnalogClockView analogClock;
    private TextView digitalClock;
    private AppBarLayout appBarLayout;

    // Handler for digital clock updates
    private Handler clockHandler;
    private Runnable clockUpdateRunnable;
    private SimpleDateFormat timeFormat;

    // Ringtone Selection
    private androidx.activity.result.ActivityResultLauncher<Intent> ringtonePickerLauncher;
    private String currentRingtoneUri;
    private TextView ringtoneNameText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ringtonePickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData()
                                .getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            currentRingtoneUri = uri.toString();
                            android.media.Ringtone ringtone = android.media.RingtoneManager
                                    .getRingtone(requireContext(), uri);
                            if (ringtone != null) {
                                ringtoneNameText.setText(ringtone.getTitle(requireContext()));
                            }
                        } else {
                            // Silent
                            currentRingtoneUri = "";
                            ringtoneNameText.setText("Silent");
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.alarms_recycler_view);
        fabAddAlarm = view.findViewById(R.id.fab_add_alarm);
        fabDeleteSelected = view.findViewById(R.id.fab_delete_selected);
        emptyState = view.findViewById(R.id.empty_state);

        // Initialize clock views
        analogClock = view.findViewById(R.id.analog_clock);
        digitalClock = view.findViewById(R.id.digital_clock);
        appBarLayout = view.findViewById(R.id.app_bar_layout);

        // Setup time format
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        clockHandler = new Handler(Looper.getMainLooper());
        clockUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateDigitalClock();
                clockHandler.postDelayed(this, 1000);
            }
        };

        // Setup scroll-based clock animation
        setupClockAnimation();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AlarmAdapter(this);
        recyclerView.setAdapter(adapter);

        repository = new AlarmRepository(requireActivity().getApplication());

        repository.getAllAlarms().observe(getViewLifecycleOwner(), alarms -> {
            adapter.setAlarms(alarms);
            // If alarms are empty, show empty state (unless we are just updating)
            if (alarms.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });

        fabAddAlarm.setOnClickListener(v -> showAddAlarmDialog(null));
        fabDeleteSelected.setOnClickListener(v -> deleteSelectedAlarms());

        checkExactAlarmPermission();
        checkNotificationPermission();
        checkFullScreenIntentPermission();
    }

    private void setupClockAnimation() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (totalScrollRange == 0)
                    return;

                // Calculate scroll progress (0 = fully expanded, 1 = fully collapsed)
                float scrollProgress = Math.abs(verticalOffset) / (float) totalScrollRange;

                // Animate analog clock: scale down and fade out
                float analogScale = 1f - (scrollProgress * 0.5f); // Scale from 1.0 to 0.5
                float analogAlpha = 1f - scrollProgress; // Fade out
                analogClock.setScaleX(analogScale);
                analogClock.setScaleY(analogScale);
                analogClock.setAlpha(analogAlpha);

                // Animate digital clock: fade in as we scroll
                digitalClock.setAlpha(scrollProgress);
            }
        });
    }

    private void updateDigitalClock() {
        if (digitalClock != null) {
            digitalClock.setText(timeFormat.format(new Date()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start digital clock updates
        updateDigitalClock();
        clockHandler.postDelayed(clockUpdateRunnable, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop digital clock updates
        clockHandler.removeCallbacks(clockUpdateRunnable);
    }

    private void deleteSelectedAlarms() {
        List<Alarm> selected = adapter.getSelectedAlarms();
        if (selected.isEmpty())
            return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Alarms")
                .setMessage("Delete " + selected.size() + " selected alarms?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (Alarm alarm : selected) {
                        AlarmScheduler.cancelAlarm(requireContext(), alarm.getId());
                        repository.delete(alarm);
                    }
                    adapter.clearSelection();
                    Toast.makeText(requireContext(), "Alarms deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Show dialog explaining why we need the permission
                new AlertDialog.Builder(requireContext())
                        .setTitle("Notification Permission Required")
                        .setMessage(
                                "This app needs notification permission to show alarm alerts. Without this, you won't see alarm notifications.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> {
                            requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(requireContext(),
                                    "Alarm notifications will not work without this permission",
                                    Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        } else {
            // For older Android versions, check if notifications are enabled
            NotificationManager notificationManager = (NotificationManager) requireContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.areNotificationsEnabled()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Notifications Disabled")
                        .setMessage("Please enable notifications for this app to receive alarm alerts.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
            NotificationManager notificationManager = (NotificationManager) requireContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.canUseFullScreenIntent()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("To show alarms on the lock screen, please enable Full Screen Notifications.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to schedule exact alarms...")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(requireContext(), "Alarms will not work without this permission",
                                    Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void showAddAlarmDialog(Alarm existingAlarm) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_alarm_dialog, null);

        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        // Day Toggles
        ToggleButton[] dayToggles = new ToggleButton[7];
        dayToggles[0] = dialogView.findViewById(R.id.day_sun);
        dayToggles[1] = dialogView.findViewById(R.id.day_mon);
        dayToggles[2] = dialogView.findViewById(R.id.day_tue);
        dayToggles[3] = dialogView.findViewById(R.id.day_wed);
        dayToggles[4] = dialogView.findViewById(R.id.day_thu);
        dayToggles[5] = dialogView.findViewById(R.id.day_fri);
        dayToggles[6] = dialogView.findViewById(R.id.day_sat);

        Button deleteAlarmBtn = dialogView.findViewById(R.id.delete_alarm_button);

        // Ringtone UI
        View ringtoneContainer = dialogView.findViewById(R.id.ringtone_container);
        ringtoneNameText = dialogView.findViewById(R.id.ringtone_name);

        ringtoneContainer.setOnClickListener(v -> {
            Intent intent = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE,
                    android.media.RingtoneManager.TYPE_ALARM);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    (currentRingtoneUri != null && !currentRingtoneUri.isEmpty()) ? Uri.parse(currentRingtoneUri)
                            : null);
            ringtonePickerLauncher.launch(intent);
        });

        // Setup Puzzle Type Spinner
        View puzzleTypeContainer = dialogView.findViewById(R.id.puzzle_type_container);
        Spinner puzzleTypeSpinner = dialogView.findViewById(R.id.puzzle_type_spinner);
        String[] puzzleNames = { "Jpeg Chaos", "Minesweeper", "Sudoku" };
        String[] puzzleValues = { "jpegchaos", "minesweeper", "sudoku" };
        ArrayAdapter<String> puzzleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, puzzleNames);
        puzzleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        puzzleTypeSpinner.setAdapter(puzzleAdapter);

        // Setup Sleep Alarm Switch Listener
        com.google.android.material.switchmaterial.SwitchMaterial sleepAlarmSwitch = dialogView
                .findViewById(R.id.sleep_alarm_switch);
        sleepAlarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            puzzleTypeContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if (existingAlarm != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(existingAlarm.getTimeInMillis());
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));

            // Set Toggles
            int days = existingAlarm.getDaysOfWeek();
            for (int i = 0; i < 7; i++) {
                if ((days & (1 << i)) != 0) {
                    dayToggles[i].setChecked(true);
                }
            }

            // Set Ringtone
            currentRingtoneUri = existingAlarm.getRingtoneUri();
            if (currentRingtoneUri != null && !currentRingtoneUri.isEmpty()) {
                android.media.Ringtone ringtone = android.media.RingtoneManager.getRingtone(requireContext(),
                        Uri.parse(currentRingtoneUri));
                if (ringtone != null) {
                    ringtoneNameText.setText(ringtone.getTitle(requireContext()));
                }
            } else {
                ringtoneNameText.setText("Default");
            }

            sleepAlarmSwitch.setChecked(existingAlarm.isSleepAlarm());
            puzzleTypeContainer.setVisibility(existingAlarm.isSleepAlarm() ? View.VISIBLE : View.GONE);

            // Set puzzle type spinner selection
            String savedPuzzleType = existingAlarm.getPuzzleType();
            if (savedPuzzleType != null) {
                for (int i = 0; i < puzzleValues.length; i++) {
                    if (puzzleValues[i].equals(savedPuzzleType)) {
                        puzzleTypeSpinner.setSelection(i);
                        break;
                    }
                }
            }

            deleteAlarmBtn.setVisibility(View.VISIBLE);
        } else {
            deleteAlarmBtn.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Calculate days bitmask
            int daysOfWeek = 0;
            for (int i = 0; i < 7; i++) {
                if (dayToggles[i].isChecked()) {
                    daysOfWeek |= (1 << i);
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Simple logic: if specific days not set, and time < now, +1 day.
            // Better logic in AlarmScheduler, but we need a base time.
            if (daysOfWeek == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Get Sleep Alarm State and Puzzle Type
            boolean isSleepAlarm = sleepAlarmSwitch.isChecked();
            String selectedPuzzleType = puzzleValues[puzzleTypeSpinner.getSelectedItemPosition()];

            if (existingAlarm != null) {
                existingAlarm.setTimeInMillis(calendar.getTimeInMillis());
                existingAlarm.setDaysOfWeek(daysOfWeek);
                existingAlarm.setRingtoneUri(currentRingtoneUri); // Save Ringtone
                existingAlarm.setSleepAlarm(isSleepAlarm);
                existingAlarm.setPuzzleType(selectedPuzzleType);
                repository.update(existingAlarm);

                AlarmScheduler.cancelAlarm(requireContext(), existingAlarm.getId());
                if (existingAlarm.isEnabled()) {
                    AlarmScheduler.scheduleAlarm(requireContext(), existingAlarm);
                }
                Toast.makeText(requireContext(), R.string.alarm_updated, Toast.LENGTH_SHORT).show();
            } else {
                Alarm alarm = new Alarm(calendar.getTimeInMillis(), true, daysOfWeek);
                alarm.setRingtoneUri(currentRingtoneUri); // Save Ringtone
                alarm.setSleepAlarm(isSleepAlarm);
                alarm.setPuzzleType(selectedPuzzleType);
                repository.insert(alarm, insertedAlarm -> {
                    requireActivity().runOnUiThread(() -> {
                        AlarmScheduler.scheduleAlarm(requireContext(), insertedAlarm);
                        Toast.makeText(requireContext(), R.string.alarm_created, Toast.LENGTH_SHORT).show();
                    });
                });
            }
            dialog.dismiss();
        });

        deleteAlarmBtn.setOnClickListener(v -> {
            if (existingAlarm != null) {
                AlarmScheduler.cancelAlarm(requireContext(), existingAlarm.getId());
                repository.delete(existingAlarm);
                Toast.makeText(requireContext(), R.string.alarm_deleted, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onAlarmToggle(Alarm alarm, boolean isEnabled) {
        alarm.setEnabled(isEnabled);
        repository.update(alarm);

        if (isEnabled) {
            AlarmScheduler.scheduleAlarm(requireContext(), alarm);
            Toast.makeText(requireContext(), R.string.alarm_enabled, Toast.LENGTH_SHORT).show();
        } else {
            AlarmScheduler.cancelAlarm(requireContext(), alarm.getId());
            Toast.makeText(requireContext(), R.string.alarm_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAlarmClick(Alarm alarm) {
        showAddAlarmDialog(alarm);
    }

    @Override
    public void onSelectionChanged(int count) {
        if (count > 0) {
            fabDeleteSelected.setVisibility(View.VISIBLE);
            fabAddAlarm.setVisibility(View.GONE);
        } else {
            fabDeleteSelected.setVisibility(View.GONE);
            fabAddAlarm.setVisibility(View.VISIBLE);
        }
    }
}