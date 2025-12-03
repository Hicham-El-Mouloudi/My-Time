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
        private TextView tvDescription;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(DailyActivity activity, OnActivityClickListener listener, int position) {
            tvTime.setText(activity.getTime());

            if (activity.getDescription().isEmpty()) {
                tvDescription.setText("Créneau disponible");
                tvDescription.setTextColor(itemView.getContext().getColor(R.color.medium_gray));
                tvDescription.setAlpha(0.6f);
            } else {
                tvDescription.setText(activity.getDescription());
                tvDescription.setTextColor(itemView.getContext().getColor(R.color.text_dark));
                tvDescription.setAlpha(1.0f);
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

            // Clic sur l'item pour éditer
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityEdit(position, activity);
                }
            });
        }
    }

}
