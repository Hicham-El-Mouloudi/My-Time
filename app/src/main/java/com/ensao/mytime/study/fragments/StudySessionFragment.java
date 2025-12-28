package com.ensao.mytime.study.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ensao.mytime.R;
import com.ensao.mytime.study.adapter.SubjectAdapter;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.viewmodel.StudyViewModel;
import java.util.ArrayList;

public class StudySessionFragment extends Fragment {

    private StudyViewModel studyViewModel;

    // UI Elements
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private Button btnStart, btnPause, btnStop;
    private Button btn25Min, btn45Min, btnCustom;
    private EditText etSubjectName;
    private Button btnAddSubject;
    private RecyclerView rvSubjects;
    private SubjectAdapter subjectAdapter;

    private int currentMaxDuration = 25 * 60; // Default 25 min in seconds

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_session, container, false);

        studyViewModel = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);

        initViews(view);
        setupRecyclerView();
        observeViewModel();
        setupButtonListeners();

        return view;
    }

    private void initViews(View view) {
        // Timer
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);

        // Timer Controls
        btnStart = view.findViewById(R.id.btn_start);
        btnPause = view.findViewById(R.id.btn_pause);
        btnStop = view.findViewById(R.id.btn_stop);

        // Duration Buttons
        btn25Min = view.findViewById(R.id.btn_25_min);
        btn45Min = view.findViewById(R.id.btn_45_min);
        btnCustom = view.findViewById(R.id.btn_custom);

        // Subjects
        etSubjectName = view.findViewById(R.id.et_subject_name);
        btnAddSubject = view.findViewById(R.id.btn_add_subject);
        rvSubjects = view.findViewById(R.id.rv_subjects);

        updateButtonVisibility("stopped");
    }

    private void setupRecyclerView() {
        subjectAdapter = new SubjectAdapter(new ArrayList<>(), new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onSubjectChecked(Subject subject, boolean isChecked) {
                studyViewModel.changeSubjectCompletion(subject, isChecked);
            }

            @Override
            public void onSubjectDeleted(Subject subject) {
                studyViewModel.deleteSubject(subject);
            }

            @Override
            public void onSubjectClicked(Subject subject) {
                // Do nothing for main list clicks
            }
        });

        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSubjects.setAdapter(subjectAdapter);
    }

    private void observeViewModel() {
        studyViewModel.getCurrentTime().observe(getViewLifecycleOwner(), timeInSeconds -> {
            if (timeInSeconds != null) {
                updateTimerDisplay(timeInSeconds);
            }
        });

        studyViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) {
                updateButtonVisibility(state);
                boolean isRunning = "running".equals(state) || "paused".equals(state);
                updateDurationButtonsState(isRunning);

                if (tvTimer != null) {
                    // Update text color based on state if needed, or keep white/primary
                    // Since we are using Glass style with explicit colors in XML, consistent white
                    // is usually best.
                    // But if you want status indication via text color:
                    switch (state) {
                        case "running":
                            // tvTimer.setTextColor(getResources().getColor(R.color.glass_text_primary));
                            break;
                        case "paused":
                            // tvTimer.setTextColor(getResources().getColor(R.color.glass_text_secondary));
                            break;
                        case "stopped":
                            // tvTimer.setTextColor(getResources().getColor(R.color.glass_text_primary));
                            // Reset progress on stop
                            if (progressTimer != null)
                                progressTimer.setProgress(100);
                            break;
                    }
                }
            }
        });

        studyViewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                subjectAdapter.setSubjects(subjects);
            }
        });
    }

    private void showCustomDurationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());

        builder.setTitle(getString(R.string.dialog_custom_duration_title));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_duration, null);
        android.widget.NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(120);
        numberPicker.setValue(30);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setFormatter(value -> String.format(getString(R.string.dialog_custom_duration_min_format), value));

        builder.setView(dialogView)
                .setPositiveButton(getString(R.string.dialog_custom_duration_confirm), (dialog, which) -> {
                    int selectedMinutes = numberPicker.getValue();
                    currentMaxDuration = selectedMinutes * 60;
                    studyViewModel.setTimerDuration(selectedMinutes);

                    tvTimer.setText(String.format("%02d:00", selectedMinutes));
                    if (progressTimer != null)
                        progressTimer.setProgress(100);

                    updateDurationButtonSelection(btnCustom);
                    btnCustom.setText(
                            String.format(getString(R.string.dialog_custom_duration_min_format), selectedMinutes));
                })
                .setNegativeButton(getString(R.string.dialog_custom_duration_cancel),
                        (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void showSubjectSelectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_subject, null);

        RecyclerView rvDialogSubjects = dialogView.findViewById(R.id.rv_dialog_subjects);
        EditText etNewSubject = dialogView.findViewById(R.id.et_dialog_new_subject);
        Button btnAdd = dialogView.findViewById(R.id.btn_dialog_add_subject);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Setup Adapter for Dialog
        com.ensao.mytime.study.adapter.SubjectSelectionAdapter dialogAdapter = new com.ensao.mytime.study.adapter.SubjectSelectionAdapter(
                new ArrayList<>(),
                subject -> {
                    // Select subject and start
                    studyViewModel.setCurrentSubject(subject.getName());
                    studyViewModel.startTimer();
                    dialog.dismiss();
                });

        rvDialogSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDialogSubjects.setAdapter(dialogAdapter);

        // Load subjects
        studyViewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                dialogAdapter.setSubjects(subjects);
            }
        });

        // Add Button Logic
        btnAdd.setOnClickListener(v -> {
            String name = etNewSubject.getText().toString().trim();
            if (!name.isEmpty()) {
                studyViewModel.insertSubject(name);
                etNewSubject.setText("");
                android.widget.Toast
                        .makeText(getContext(), R.string.study_msg_subject_added, android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }
        dialog.show();
    }

    private void setupButtonListeners() {
        btnStart.setOnClickListener(v -> {
            String currentState = studyViewModel.getTimerState().getValue();
            if ("paused".equals(currentState)) {
                studyViewModel.resumeTimer();
            } else {
                showSubjectSelectionDialog();
            }
        });

        btnPause.setOnClickListener(v -> studyViewModel.pauseTimer());
        btnStop.setOnClickListener(v -> studyViewModel.stopTimer());

        btn25Min.setOnClickListener(v -> {
            currentMaxDuration = 25 * 60;
            studyViewModel.setTimerDuration(25);
            tvTimer.setText("25:00");
            if (progressTimer != null)
                progressTimer.setProgress(100);
            updateDurationButtonSelection(btn25Min);
        });

        btn45Min.setOnClickListener(v -> {
            currentMaxDuration = 45 * 60;
            studyViewModel.setTimerDuration(45);
            tvTimer.setText("45:00");
            if (progressTimer != null)
                progressTimer.setProgress(100);
            updateDurationButtonSelection(btn45Min);
        });

        btnCustom.setOnClickListener(v -> showCustomDurationDialog());

        btnAddSubject.setOnClickListener(v -> {
            String subjectName = etSubjectName.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                studyViewModel.insertSubject(subjectName);
                etSubjectName.setText("");
                hideKeyboard();
            }
        });
    }

    private void updateTimerDisplay(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        if (tvTimer != null) {
            tvTimer.setText(timeString);
        }

        // Update Circular Progress
        if (progressTimer != null && currentMaxDuration > 0) {
            int progress = (int) ((timeInSeconds / (float) currentMaxDuration) * 100);
            progressTimer.setProgress(progress);
        }
    }

    private void updateButtonVisibility(String state) {
        if (btnStart == null || btnPause == null || btnStop == null)
            return;

        switch (state) {
            case "running":
                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                break;
            case "paused":
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setText(getString(R.string.study_btn_resume));
                btnPause.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                break;
            case "stopped":
            default:
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setText(getString(R.string.study_btn_start));
                btnPause.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                break;
        }
    }

    private void updateDurationButtonsState(boolean isTimerRunning) {
        boolean enabled = !isTimerRunning;
        btn25Min.setEnabled(enabled);
        btn45Min.setEnabled(enabled);
        btnCustom.setEnabled(enabled);

        // Tint logic handled by selector or simple alpha change if desired
        // For glass morphism, disabling might just mean lowering opacity
        float alpha = enabled ? 1.0f : 0.5f;
        btn25Min.setAlpha(alpha);
        btn45Min.setAlpha(alpha);
        btnCustom.setAlpha(alpha);
    }

    private void updateDurationButtonSelection(Button selectedButton) {
        // Reset styles (Glass button secondary is default)
        // Here we can use simple alpha or slightly different tint if we had specific
        // selected state color
        // For simplicity with glass buttons, we'll keep them consistent or maybe add a
        // border
        // For now, let's just ensure they look "reset".

        // If we want to highlight selection, we could change background tint.
        // Let's assume default is glass_button_secondary.
        // Selected could be glass_button_primary or just higher opacity.
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSubjectName != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvTimer = null;
        progressTimer = null;
        btnStart = null;
        btnPause = null;
        btnStop = null;
        btn25Min = null;
        btn45Min = null;
        btnCustom = null;
        etSubjectName = null;
        btnAddSubject = null;
        rvSubjects = null;
        subjectAdapter = null;
    }
}