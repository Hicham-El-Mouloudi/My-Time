package com.ensao.mytime.study.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.study.adapter.SubjectAdapter;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.study.viewmodel.StudyViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudySessionFragment extends Fragment {

    private StudyViewModel studyViewModel;

    // UI Elements (Timer)
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private Button btnStart, btnPause, btnStop;
    private Button btn25Min, btn45Min, btnCustom;

    // UI Elements (Sujets)
    private EditText etSubjectName;
    private Button btnAddSubject;
    private RecyclerView rvSubjects;
    private SubjectAdapter subjectAdapter;

    // --- NOUVEAUX UI ELEMENTS (PDFs) ---
    private RecyclerView rvPdfs;
    private TextView tvNoPdf;
    private Button btnImportPdf;
    private PdfAdapter pdfAdapter;
    private List<File> pdfList = new ArrayList<>();

    // Launcher pour ouvrir la galerie
    private ActivityResultLauncher<String> pickPdfLauncher;

    private int currentMaxDuration = 25 * 60; // Default 25 min in seconds

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- INITIALISATION DU LAUNCHER (Indispensable pour l'import) ---
        pickPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importPdfFile(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_session, container, false);

        studyViewModel = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);

        initViews(view);
        setupRecyclerView();    // Pour les matières
        setupPdfRecyclerView(); // Pour les PDF (NOUVEAU)
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

        // --- PDF Views ---
        btnImportPdf = view.findViewById(R.id.btn_import_pdf);
        rvPdfs = view.findViewById(R.id.rv_pdfs);
        tvNoPdf = view.findViewById(R.id.tv_no_pdf);

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

    // --- Configuration de la liste PDF ---
    private void setupPdfRecyclerView() {
        rvPdfs.setLayoutManager(new LinearLayoutManager(getContext()));
        pdfAdapter = new PdfAdapter(pdfList, this::openPdf);
        rvPdfs.setAdapter(pdfAdapter);
        loadSavedPdfs();
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
                    switch (state) {
                        case "stopped":
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

        // --- Clic sur le bouton Import PDF ---
        btnImportPdf.setOnClickListener(v -> {
            pickPdfLauncher.launch("application/pdf");
        });
    }

    // ==========================================
    // LOGIQUE PDF (IMPORT & LECTURE)
    // ==========================================

    private void importPdfFile(Uri sourceUri) {
        try {
            String fileName = getFileName(sourceUri);
            File destFile = new File(requireContext().getFilesDir(), fileName);

            InputStream is = requireContext().getContentResolver().openInputStream(sourceUri);
            FileOutputStream fos = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            is.close();
            fos.close();

            Toast.makeText(getContext(), "Document importé !", Toast.LENGTH_SHORT).show();
            loadSavedPdfs();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur d'importation", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedPdfs() {
        pdfList.clear();
        File dir = requireContext().getFilesDir();
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files != null) {
            for (File f : files) {
                pdfList.add(f);
            }
        }

        if (pdfAdapter != null) pdfAdapter.notifyDataSetChanged();
        if (tvNoPdf != null) {
            tvNoPdf.setVisibility(pdfList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openPdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Aucune application pour lire les PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    // ==========================================
    // MÉTHODES UI & UTILITAIRES
    // ==========================================

    private void updateTimerDisplay(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        if (tvTimer != null) {
            tvTimer.setText(timeString);
        }

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
        float alpha = enabled ? 1.0f : 0.5f;
        btn25Min.setAlpha(alpha);
        btn45Min.setAlpha(alpha);
        btnCustom.setAlpha(alpha);
    }

    private void updateDurationButtonSelection(Button selectedButton) {
        // Optionnel : Gestion de la sélection visuelle
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
        tvTimer = null; progressTimer = null;
        btnStart = null; btnPause = null; btnStop = null;
        btn25Min = null; btn45Min = null; btnCustom = null;
        etSubjectName = null; btnAddSubject = null; rvSubjects = null;
        subjectAdapter = null;
        // Clean PDF
        rvPdfs = null; tvNoPdf = null; btnImportPdf = null;
    }

    // --- ADAPTER PDF (STATIC) ---
    private static class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {
        private final List<File> files;
        private final OnPdfClickListener listener;

        public interface OnPdfClickListener {
            void onPdfClick(File file);
        }

        public PdfAdapter(List<File> files, OnPdfClickListener listener) {
            this.files = files;
            this.listener = listener;
        }

        @NonNull
        @Override
        public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new PdfViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
            File file = files.get(position);
            holder.tvName.setText("📄 " + file.getName());
            holder.itemView.setOnClickListener(v -> listener.onPdfClick(file));
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        static class PdfViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            public PdfViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
                // Couleur blanche pour le style Glass
                tvName.setTextColor(0xFFFFFFFF);
            }
        }
    }
}
