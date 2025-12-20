package com.ensao.mytime.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ensao.mytime.R;
import com.ensao.mytime.home.view.InvocationFragment;
import com.ensao.mytime.home.viewmodel.MotivationViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MotivationViewModel viewModel;
    private TextView quoteText, quoteAuthor, gregorianDateText;
    private View invocationBadge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MotivationViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadDailyQuote();
        viewModel.checkInvocationTime();
        displayCurrentDate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quoteText = view.findViewById(R.id.quote_text);
        quoteAuthor = view.findViewById(R.id.quote_author);
        gregorianDateText = view.findViewById(R.id.gregorian_date);
        invocationBadge = view.findViewById(R.id.invocation_badge);
        MaterialButton invocationButton = view.findViewById(R.id.btn_invocations);

        viewModel.quote.observe(getViewLifecycleOwner(), quote -> {
            if (quote != null) {
                quoteText.setText(quote.getText());
                quoteAuthor.setText("— " + quote.getAuthor());
            } else {
                quoteText.setText("Impossible de charger la citation.");
                quoteAuthor.setText("");
            }
        });

        viewModel.isInvocationTime.observe(getViewLifecycleOwner(), isTime -> {
            if (isTime != null) {
                invocationBadge.setVisibility(isTime ? View.VISIBLE : View.GONE);
            }
        });

        invocationButton.setOnClickListener(v -> {
            // Cacher le badge lorsqu'on clique sur le bouton
            invocationBadge.setVisibility(View.GONE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InvocationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        displayCurrentDate();
    }

    private void displayCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        gregorianDateText.setText(currentDate);
    }
}