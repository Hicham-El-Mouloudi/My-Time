package com.ensao.mytime.alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context; // Added
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // CHANGED: Use AndroidX Fragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.Alarm;
import com.ensao.mytime.alarm.database.AlarmRepository;

import java.util.Calendar; // Added missing import

public class AlarmFragment extends Fragment implements AlarmAdapter.OnAlarmActionListener {

    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private AlarmRepository repository;
    private FloatingActionButton fabAddAlarm;
    private View emptyState;

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

        // Init view using root 'view'
        recyclerView = view.findViewById(R.id.alarms_recycler_view);
        fabAddAlarm = view.findViewById(R.id.fab_add_alarm);
        emptyState = view.findViewById(R.id.empty_state);

        // FIX: Use requireContext() for LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Pass 'this' as the listener (implements interface), NOT as context
        adapter = new AlarmAdapter(this);
        recyclerView.setAdapter(adapter);

        // FIX: Use requireActivity().getApplication() for the Repository
        repository = new AlarmRepository(requireActivity().getApplication());

        // FIX: Use getViewLifecycleOwner() for Fragments
        repository.getAllAlarms().observe(getViewLifecycleOwner(), alarms -> {
            adapter.setAlarms(alarms);
            if (alarms.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });

        fabAddAlarm.setOnClickListener(v -> showAddAlarmDialog(null));
        checkExactAlarmPermission();
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // FIX: Use requireContext() for checkSelfPermission
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // requestPermissions is fine in Fragment
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // FIX: Use requireContext().getSystemService()
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // FIX: Use requireContext() for Builder
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to schedule exact alarms...")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            // FIX: Use requireContext().getPackageName()
                            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // FIX: Use requireContext() for Toast
                            Toast.makeText(requireContext(), "Alarms will not work without this permission", Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void showAddAlarmDialog(Alarm existingAlarm) {
        // FIX: Use requireActivity().getLayoutInflater() or just getLayoutInflater() is usually fine in modern fragments,
        // but LayoutInflater.from(context) is safest for dialogs.
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_alarm_dialog, null);

        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        TextInputEditText labelInput = dialogView.findViewById(R.id.alarm_label_input);

        timePicker.setIs24HourView(true);

        if (existingAlarm != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(existingAlarm.getTimeInMillis());
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));
            labelInput.setText(existingAlarm.getLabel());
        }

        // FIX: Use requireContext()
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            String label = labelInput.getText() != null ? labelInput.getText().toString() : "";

            if (existingAlarm != null) {
                existingAlarm.setTimeInMillis(calendar.getTimeInMillis());
                existingAlarm.setLabel(label);
                repository.update(existingAlarm);

                // FIX: Use requireContext() for scheduler
                AlarmScheduler.cancelAlarm(requireContext(), existingAlarm.getId());
                if (existingAlarm.isEnabled()) {
                    AlarmScheduler.scheduleAlarm(requireContext(), existingAlarm);
                }

                Toast.makeText(requireContext(), R.string.alarm_updated, Toast.LENGTH_SHORT).show();
            } else {
                Alarm alarm = new Alarm(calendar.getTimeInMillis(), label, true);

                repository.insert(alarm, insertedAlarm -> {
                    // FIX: Use requireActivity().runOnUiThread
                    requireActivity().runOnUiThread(() -> {
                        AlarmScheduler.scheduleAlarm(requireContext(), insertedAlarm);
                        Toast.makeText(requireContext(), R.string.alarm_created, Toast.LENGTH_SHORT).show();
                    });
                });
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- Interface Implementation ---

    @Override
    public void onAlarmToggle(Alarm alarm, boolean isEnabled) {
        alarm.setEnabled(isEnabled);
        repository.update(alarm);

        // FIX: Use requireContext() for context operations
        if (isEnabled) {
            AlarmScheduler.scheduleAlarm(requireContext(), alarm);
            Toast.makeText(requireContext(), R.string.alarm_enabled, Toast.LENGTH_SHORT).show();
        } else {
            AlarmScheduler.cancelAlarm(requireContext(), alarm.getId());
            Toast.makeText(requireContext(), R.string.alarm_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAlarmEdit(Alarm alarm) {
        showAddAlarmDialog(alarm);
    }

    @Override
    public void onAlarmDelete(Alarm alarm) {
        // FIX: Use requireContext() for Builder
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_alarm)
                .setMessage(R.string.delete_alarm_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    AlarmScheduler.cancelAlarm(requireContext(), alarm.getId());
                    repository.delete(alarm);
                    Toast.makeText(requireContext(), R.string.alarm_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}