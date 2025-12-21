package com.ensao.mytime.study.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensao.mytime.study.model.Subject;
import com.ensao.mytime.R;
import java.util.List;
import android.graphics.Paint;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjects;
    private OnSubjectClickListener listener;

    public interface OnSubjectClickListener {
        void onSubjectChecked(Subject subject, boolean isChecked);
        void onSubjectDeleted(Subject subject);
    }

    public SubjectAdapter(List<Subject> subjects, OnSubjectClickListener listener) {
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
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

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSubject;
        private TextView tvSubjectName;
        private ImageButton btnDelete;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSubject = itemView.findViewById(R.id.cb_subject);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Subject subject, OnSubjectClickListener listener) {
            tvSubjectName.setText(subject.getName());
            cbSubject.setChecked(subject.isCompleted());

            // Styler le texte si complété
            if (subject.isCompleted()) {
                tvSubjectName.setTextColor(0xFF888888);
                tvSubjectName.setPaintFlags(tvSubjectName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvSubjectName.setTextColor(0xFF333333);
                tvSubjectName.setPaintFlags(tvSubjectName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            cbSubject.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    subject.setCompleted(isChecked);
                    listener.onSubjectChecked(subject, isChecked);

                    // Rafraîchir l'apparence
                    bind(subject, listener);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubjectDeleted(subject);
                }
            });
        }
    }
}