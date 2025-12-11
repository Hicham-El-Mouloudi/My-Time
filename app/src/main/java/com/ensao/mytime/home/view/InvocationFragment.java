package com.ensao.mytime.home.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ensao.mytime.R;
import com.ensao.mytime.home.model.SingleInvocation;
import com.ensao.mytime.home.viewmodel.InvocationViewModel;

import java.util.Collections;
import java.util.List;

public class InvocationFragment extends Fragment implements InvocationAdapter.InvocationActionListener {

    private InvocationViewModel viewModel;
    private TextView titleTextView, completionMessageTextView;
    private RecyclerView invocationRecyclerView;
    private InvocationAdapter invocationAdapter;
    private InvocationListener mListener;

    public interface InvocationListener {
        void onSettingsButtonClicked();
        void onAllInvocationsCompleted();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof InvocationListener) {
            mListener = (InvocationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement InvocationListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invocation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InvocationViewModel.class);

        titleTextView = view.findViewById(R.id.invocation_title);
        invocationRecyclerView = view.findViewById(R.id.invocation_list_recycler);
        completionMessageTextView = view.findViewById(R.id.completion_message);
        ImageButton settingsButton = view.findViewById(R.id.btn_settings);

        setupRecyclerView();
        observeViewModel();

        settingsButton.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSettingsButtonClicked();
            }
        });
    }

    private void setupRecyclerView() {
        invocationAdapter = new InvocationAdapter(Collections.emptyList(), this);
        invocationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        invocationRecyclerView.setAdapter(invocationAdapter);
    }

    private void observeViewModel() {
        viewModel.invocationContent.observe(getViewLifecycleOwner(), content -> {
            if (content != null && content.length == 2) {
                String title = (String) content[0];
                titleTextView.setText(title);

                if (content[1] instanceof List) {
                    try {
                        List<SingleInvocation> list = (List<SingleInvocation>) content[1];
                        invocationAdapter.setInvocationList(list);
                    } catch (ClassCastException e) {
                        Log.e("InvocationFragment", "Erreur de cast", e);
                    }
                }
            }
        });

        viewModel.allInvocationsCompleted.observe(getViewLifecycleOwner(), completed -> {
            if (completed != null) {
                if (completed) {
                    showCompletionMessage();
                    if (mListener != null) {
                        mListener.onAllInvocationsCompleted();
                    }
                } else {
                    showInvocationList();
                }
            }
        });
    }

    private void showCompletionMessage() {
        invocationRecyclerView.setVisibility(View.GONE);
        completionMessageTextView.setVisibility(View.VISIBLE);

        String title = titleTextView.getText().toString();
        if (title.contains("Matin")) {
            completionMessageTextView.setText("Excellent ! Tu as terminé les invocations du matin de ce jour.");
        } else if (title.contains("Soir")) {
            completionMessageTextView.setText("Excellent ! Tu as terminé les invocations du soir de ce jour.");
        } else {
             completionMessageTextView.setText("Excellent ! Tu as terminé tes invocations.");
        }
    }

    private void showInvocationList() {
        invocationRecyclerView.setVisibility(View.VISIBLE);
        completionMessageTextView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.determineInvocation();
        }
    }

    @Override
    public void onInvocationStateChanged(List<SingleInvocation> currentList) {
        viewModel.saveCurrentState(currentList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}