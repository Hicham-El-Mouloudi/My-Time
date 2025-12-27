package com.ensao.mytime.calendar;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ensao.mytime.R;
import com.ensao.mytime.Activityfeature.Busniss.Category;
import com.ensao.mytime.Activityfeature.Busniss.RepetitionKind;
import com.ensao.mytime.Activityfeature.Repos.CategoryRepo;
import com.ensao.mytime.Activityfeature.Repos.RepetitionKindRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateCategoryDialog extends DialogFragment {

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(Category category);
    }

    private OnCategoryCreatedListener listener;
    private CategoryRepo categoryRepo;
    private RepetitionKindRepo repetitionKindRepo;
    
    private EditText categoryNameEditText;
    private EditText categoryDescriptionEditText;
    private RadioGroup repetitionRadioGroup;
    
    private Map<String, Long> repetitionKindMap = new HashMap<>();

    public static CreateCategoryDialog newInstance(OnCategoryCreatedListener listener) {
        CreateCategoryDialog dialog = new CreateCategoryDialog();
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_create_category);

        // Initialize repos
        categoryRepo = new CategoryRepo(requireActivity().getApplication());
        repetitionKindRepo = new RepetitionKindRepo(requireActivity().getApplication());

        // Initialize views
        categoryNameEditText = dialog.findViewById(R.id.categoryNameEditText);
        categoryDescriptionEditText = dialog.findViewById(R.id.categoryDescriptionEditText);
        repetitionRadioGroup = dialog.findViewById(R.id.repetitionRadioGroup);
        Button btnCreate = dialog.findViewById(R.id.btnCreateCategory);
        Button btnCancel = dialog.findViewById(R.id.btnCancelCategory);

        // Load repetition kinds
        loadRepetitionKinds();

        btnCreate.setOnClickListener(v -> createCategory());
        btnCancel.setOnClickListener(v -> dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        return dialog;
    }

    private void loadRepetitionKinds() {
        repetitionKindRepo.GetKinds(requireActivity(), kinds -> {
            if (kinds != null) {
                for (RepetitionKind kind : kinds) {
                    repetitionKindMap.put(kind.getTitle(), kind.getId());
                }
            }
            
            // If no repetition kinds exist, we need to create them
            if (repetitionKindMap.isEmpty()) {
                Toast.makeText(getContext(), "Erreur: types de répétition non trouvés", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCategory() {
        String name = categoryNameEditText.getText().toString().trim();
        String description = categoryDescriptionEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez entrer un nom de catégorie", Toast.LENGTH_SHORT).show();
            categoryNameEditText.requestFocus();
            return;
        }

        // Get selected repetition kind
        String selectedRepetition = getSelectedRepetitionKind();
        Long repetitionId = repetitionKindMap.get(selectedRepetition);
        
        if (repetitionId == null) {
            // Create the repetition kind if it doesn't exist
            createRepetitionKindAndCategory(name, description, selectedRepetition);
            return;
        }

        // Create category
        Category newCategory = new Category(description, name, repetitionId);
        
        categoryRepo.Insert(newCategory, requireActivity(), insertedId -> {
            if (insertedId > 0) {
                newCategory.setId(insertedId);
                Toast.makeText(getContext(), "Catégorie créée", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onCategoryCreated(newCategory);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Erreur lors de la création", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRepetitionKindAndCategory(String categoryName, String categoryDescription, String repetitionTitle) {
        // We need to create the repetition kind first, then the category
        // This is a workaround since we can't directly access DAOs here
        // For now, show an error - the repetition kinds should be seeded in the database
        Toast.makeText(getContext(), 
                "Type de répétition '" + repetitionTitle + "' non trouvé. Veuillez contacter le support.", 
                Toast.LENGTH_LONG).show();
    }

    private String getSelectedRepetitionKind() {
        int selectedId = repetitionRadioGroup.getCheckedRadioButtonId();
        
        if (selectedId == R.id.radioEachday) {
            return "eachday";
        } else if (selectedId == R.id.radioEachweek) {
            return "eachweek";
        } else if (selectedId == R.id.radioEachmonth) {
            return "eachmonth";
        } else {
            return "onetime";
        }
    }
}
