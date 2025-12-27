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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddActivityDialog extends DialogFragment implements CreateCategoryDialog.OnCategoryCreatedListener {

    public interface OnActivityAddedListener {
        void onActivityAdded(DailyActivity activity);
    }

    private OnActivityAddedListener listener;
    private String selectedDate;
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

    public static AddActivityDialog newInstance(String date, OnActivityAddedListener listener) {
        AddActivityDialog dialog = new AddActivityDialog();
        dialog.selectedDate = date;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_activity);

        // Initialize repo
        categoryRepo = new CategoryRepo(requireActivity().getApplication());

        // Initialize views
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

        // Set initial time
        Calendar now = Calendar.getInstance();
        startHour = now.get(Calendar.HOUR_OF_DAY);
        startMinute = 0;
        endHour = startHour + 1;
        if (endHour > 23) endHour = 23;
        endMinute = 0;

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
            dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
          
        }

        return dialog;
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Utiliser cette méthode si vous préférez un DialogFragment standard
        View view = inflater.inflate(R.layout.dialog_add_activity, container, false);
      
        timePicker = view.findViewById(R.id.timePicker);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        timePicker.setIs24HourView(true);

        btnSave.setOnClickListener(v -> saveActivity());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
      
      
      
      

    private void loadCategories() {
        categoryRepo.GetCategories(requireActivity(), loadedCategories -> {
            categories.clear();
            categoryAdapter.clear();

            if (loadedCategories != null && !loadedCategories.isEmpty()) {
                categories.addAll(loadedCategories);
                for (CategoryDetailedDTO dto : loadedCategories) {
                    // Show user-friendly name for default category
                    String categoryName = dto.category.getTitle();
                    if ("default".equalsIgnoreCase(categoryName)) {
                        categoryName = "Par défaut";
                    }
                    String displayText = categoryName + " (" + getRepetitionLabel(dto.RepetitionTitle) + ")";
                    categoryAdapter.add(displayText);
                }
            }
            // If no categories exist, the spinner will be empty and -1 will be returned
            // which will cause CalendarFragment to use the default category
            categoryAdapter.notifyDataSetChanged();
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

        // Get selected category ID
        long categoryId = getSelectedCategoryId();

        DailyActivity newActivity = new DailyActivity(
                -1,
                selectedDate,
                startTime,
                endTime,
                title,
                description,
                categoryId
        );

        if (listener != null) {
            listener.onActivityAdded(newActivity);
        }

        dismiss();
    }

    private long getSelectedCategoryId() {
        int position = categorySpinner.getSelectedItemPosition();
        if (position >= 0 && position < categories.size()) {
            return categories.get(position).category.getId();
        }
        return -1; // Will use default category
    }
}
