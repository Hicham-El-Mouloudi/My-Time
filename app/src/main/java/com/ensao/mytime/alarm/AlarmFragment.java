package com.ensao.mytime.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.alarm.database.AlarmRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlarmFragment extends Fragment {
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
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        // init view using root 'view'
        recyclerView = view.findViewById(R.id.alarms_recycler_view);
        fabAddAlarm = view.findViewById(R.id.fab_add_alarm);
        emptyState = view.findViewById(R.id.empty_state);

        // use requireContext()
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Todo

    }
}
