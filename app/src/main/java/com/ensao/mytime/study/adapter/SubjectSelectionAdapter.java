package com.ensao.mytime.study.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.R;
import java.util.List;

public class SubjectSelectionAdapter extends RecyclerView.Adapter<SubjectSelectionAdapter.ViewHolder> {

    private List<Subject> subjects;
    private OnSubjectSelectedListener listener;

    public interface OnSubjectSelectedListener {
        void onSubjectSelected(Subject subject);
    }

    public SubjectSelectionAdapter(List<Subject> subjects, OnSubjectSelectedListener listener) {
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.bind(subject, listener);
    }

    @Override
    public int getItemCount() {
        return subjects == null ? 0 : subjects.size();
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubjectName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
        }

        public void bind(Subject subject, OnSubjectSelectedListener listener) {
            tvSubjectName.setText(subject.getName());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubjectSelected(subject);
                }
            });
        }
    }
}
