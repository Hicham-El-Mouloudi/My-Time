package com.ensao.mytime.calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ensao.mytime.R;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.DTOs.CategoryDetailedDTO;
import com.ensao.mytime.Activityfeature.Repos.CategoryRepo;
import com.ensao.mytime.study.model.DailyActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditActivityDialog extends DialogFragment implements CreateCategoryDialog.OnCategoryCreatedListener {

    public interface OnActivityEditedListener {
        void onActivityEdited(int position, DailyActivity updatedActivity);
    }

    private OnActivityEditedListener listener;
    private DailyActivity activityToEdit;
    private int position;
    private long currentCategoryId = -1;
    private CategoryRepo categoryRepo;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private LinearLayout startTimeContainer;
    private LinearLayout endTimeContainer;
    private Spinner categorySpinner;
    private ImageButton btnAddCategory;

    private int startHour = 9;
    private int startMinute = 0;
    private int endHour = 10;
    private int endMinute = 0;

    private List<CategoryDetailedDTO> categories = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    public static EditActivityDialog newInstance(int position, DailyActivity activity, long categoryId, OnActivityEditedListener listener) {
        EditActivityDialog dialog = new EditActivityDialog();
        dialog.position = position;
        dialog.activityToEdit = activity;
        dialog.currentCategoryId = categoryId;
        dialog.listener = listener;
        return dialog;
    }

    // Backward compatible constructor
    public static EditActivityDialog newInstance(int position, DailyActivity activity, OnActivityEditedListener listener) {
        return newInstance(position, activity, -1, (OnActivityEditedListener) listener);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_activity);

        // Initialize repo
        categoryRepo = new CategoryRepo(requireActivity().getApplication());

        // Initialize views
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        titleEditText = dialog.findViewById(R.id.titleEditText);
        descriptionEditText = dialog.findViewById(R.id.descriptionEditText);
        tvStartTime = dialog.findViewById(R.id.tvStartTime);
        tvEndTime = dialog.findViewById(R.id.tvEndTime);
        startTimeContainer = dialog.findViewById(R.id.startTimeContainer);
        endTimeContainer = dialog.findViewById(R.id.endTimeContainer);
        categorySpinner = dialog.findViewById(R.id.categorySpinner);
        btnAddCategory = dialog.findViewById(R.id.btnAddCategory);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Update dialog title for editing
        dialogTitle.setText("Modifier l'activité");
        btnSave.setText("Enregistrer");

        // Pre-fill values
        if (activityToEdit != null) {
            titleEditText.setText(activityToEdit.getTitle());
            descriptionEditText.setText(activityToEdit.getDescription());

            // Parse start time
            if (activityToEdit.getTime() != null && !activityToEdit.getTime().isEmpty()) {
                String[] parts = activityToEdit.getTime().split(":");
                if (parts.length == 2) {
                    try {
                        startHour = Integer.parseInt(parts[0]);
                        startMinute = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Parse end time
            if (activityToEdit.getEndTime() != null && !activityToEdit.getEndTime().isEmpty()) {
                String[] parts = activityToEdit.getEndTime().split(":");
                if (parts.length == 2) {
                    try {
                        endHour = Integer.parseInt(parts[0]);
                        endMinute = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        updateTimeDisplays();
        setupCategorySpinner();
        loadCategories();

        // Time picker listeners
        startTimeContainer.setOnClickListener(v -> showStartTimePicker());
        endTimeContainer.setOnClickListener(v -> showEndTimePicker());

        // Add category button
        btnAddCategory.setOnClickListener(v -> {
            CreateCategoryDialog categoryDialog = CreateCategoryDialog.newInstance(this);
            categoryDialog.show(getChildFragmentManager(), "CreateCategoryDialog");
        });

        btnSave.setOnClickListener(v -> saveActivity());
        btnCancel.setOnClickListener(v -> dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        return dialog;
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        categoryRepo.GetCategories(requireActivity(), loadedCategories -> {
            categories.clear();
            categoryAdapter.clear();

            int selectedIndex = 0;
            if (loadedCategories != null && !loadedCategories.isEmpty()) {
                categories.addAll(loadedCategories);
                for (int i = 0; i < loadedCategories.size(); i++) {
                    CategoryDetailedDTO dto = loadedCategories.get(i);
                    // Show user-friendly name for default category
                    String categoryName = dto.category.getTitle();
                    if ("default".equalsIgnoreCase(categoryName)) {
                        categoryName = "Par défaut";
                    }
                    String displayText = categoryName + " (" + getRepetitionLabel(dto.RepetitionTitle) + ")";
                    categoryAdapter.add(displayText);
                    
                    // Find and select current category
                    if (dto.category.getId() == currentCategoryId) {
                        selectedIndex = i;
                    }
                }
            }
            categoryAdapter.notifyDataSetChanged();
            categorySpinner.setSelection(selectedIndex);
        });
    }

    private String getRepetitionLabel(String repetitionTitle) {
        if (repetitionTitle == null) return "une fois";
        switch (repetitionTitle.toLowerCase()) {
            case "eachday": return "chaque jour";
            case "eachweek": return "chaque semaine";
            case "eachmonth": return "chaque mois";
            case "onetime":
            default: return "une fois";
        }
    }

    @Override
    public void onCategoryCreated(Category category) {
        // Store the new category ID to select it after reload
        final long newCategoryId = category.getId();
        
        categoryRepo.GetCategories(requireActivity(), loadedCategories -> {
            categories.clear();
            categoryAdapter.clear();

            int selectIndex = 0;
            if (loadedCategories != null && !loadedCategories.isEmpty()) {
                categories.addAll(loadedCategories);
                for (int i = 0; i < loadedCategories.size(); i++) {
                    CategoryDetailedDTO dto = loadedCategories.get(i);
                    String displayText = dto.category.getTitle() + " (" + getRepetitionLabel(dto.RepetitionTitle) + ")";
                    categoryAdapter.add(displayText);
                    // Select the newly created category
                    if (dto.category.getId() == newCategoryId) {
                        selectIndex = i;
                    }
                }
            }
            categoryAdapter.notifyDataSetChanged();
            categorySpinner.setSelection(selectIndex);
        });
    }

    private void showStartTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    startHour = hourOfDay;
                    startMinute = minute;
                    if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                        endHour = startHour + 1;
                        if (endHour > 23) endHour = 23;
                        endMinute = startMinute;
                    }
                    updateTimeDisplays();
                },
                startHour,
                startMinute,
                true
        );
        picker.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    endHour = hourOfDay;
                    endMinute = minute;
                    updateTimeDisplays();
                },
                endHour,
                endMinute,
                true
        );
        picker.show();
    }

    private void updateTimeDisplays() {
        tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
        tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
    }

    private void saveActivity() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer un titre", Toast.LENGTH_SHORT).show();
            titleEditText.requestFocus();
            return;
        }

        String startTime = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute);
        String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);

        long categoryId = getSelectedCategoryId();

        DailyActivity updatedActivity = new DailyActivity(
                activityToEdit.getId(),
                activityToEdit.getDate(),
                startTime,
                endTime,
                title,
                description,
                categoryId
        );

        if (listener != null) {
            listener.onActivityEdited(position, updatedActivity);
        }

        dismiss();
    }

    private long getSelectedCategoryId() {
        int position = categorySpinner.getSelectedItemPosition();
        if (position >= 0 && position < categories.size()) {
            return categories.get(position).category.getId();
        }
        return currentCategoryId; // Keep current if nothing selected
    }
}
