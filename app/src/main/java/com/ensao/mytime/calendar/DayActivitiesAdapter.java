package com.ensao.mytime.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.study.model.DailyActivity;

import java.util.List;

public class DayActivitiesAdapter extends RecyclerView.Adapter<DayActivitiesAdapter.ActivityViewHolder> {

    private List<DailyActivity> activities;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityEdit(int position, DailyActivity activity);
        void onActivityDelete(int position, DailyActivity activity);
    }

    public DayActivitiesAdapter(List<DailyActivity> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        DailyActivity activity = activities.get(position);
        holder.bind(activity, listener, position);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTime;
        private TextView tvEndTime;
        private TextView tvTitle;
        private TextView tvDescription;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(DailyActivity activity, OnActivityClickListener listener, int position) {
            // Set start time
            tvTime.setText(activity.getTime() != null ? activity.getTime() : "");

            // Set end time
            if (tvEndTime != null) {
                String endTime = activity.getEndTime();
                if (endTime != null && !endTime.equals(activity.getTime())) {
                    tvEndTime.setText(endTime);
                    tvEndTime.setVisibility(View.VISIBLE);
                } else {
                    tvEndTime.setVisibility(View.GONE);
                }
            }

            // Set title
            if (tvTitle != null) {
                String title = activity.getTitle();
                if (title != null && !title.isEmpty()) {
                    tvTitle.setText(title);
                } else {
                    tvTitle.setText(activity.getDescription());
                }
            }

            // Set description
            String description = activity.getDescription();
            if (description != null && !description.isEmpty() && 
                !description.equals(activity.getTitle())) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityEdit(position, activity);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityDelete(position, activity);
                }
            });

            // Click on item to edit
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityEdit(position, activity);
                }
            });
        }
    }
}
