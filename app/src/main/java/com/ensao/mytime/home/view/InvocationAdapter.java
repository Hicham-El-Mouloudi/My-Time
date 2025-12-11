package com.ensao.mytime.home.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;
import com.ensao.mytime.home.model.SingleInvocation;

import java.util.List;

public class InvocationAdapter extends RecyclerView.Adapter<InvocationAdapter.InvocationViewHolder> {

    public interface InvocationActionListener {
        void onInvocationStateChanged(List<SingleInvocation> currentList);
    }
    private List<SingleInvocation> invocationList;
    private InvocationActionListener actionListener;

    public InvocationAdapter(List<SingleInvocation> invocationList, InvocationActionListener listener) {
        this.invocationList = invocationList;
        this.actionListener = listener;
    }

    public void setInvocationList(List<SingleInvocation> invocationList) {
        this.invocationList = invocationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_single_invocation, parent, false);
        return new InvocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvocationViewHolder holder, int position) {
        SingleInvocation invocation = invocationList.get(position);
        holder.bind(invocation);
    }

    @Override
    public int getItemCount() {
        return invocationList != null ? invocationList.size() : 0;
    }

    class InvocationViewHolder extends RecyclerView.ViewHolder {
        TextView invocationText, invocationCount;
        ImageButton decrementButton;
        ImageButton doneCheckButton;
        SingleInvocation currentInvocation;

        public InvocationViewHolder(@NonNull View itemView) {
            super(itemView);
            invocationText = itemView.findViewById(R.id.invocation_text_item);
            invocationCount = itemView.findViewById(R.id.invocation_count);
            decrementButton = itemView.findViewById(R.id.btn_decrement);
            doneCheckButton = itemView.findViewById(R.id.btn_done_check);

            decrementButton.setOnClickListener(v -> {
                if (currentInvocation != null && currentInvocation.getCurrentCount() > 0) {
                    currentInvocation.decrement();
                    updateCountDisplay();

                    if (actionListener != null) {
                        actionListener.onInvocationStateChanged(invocationList);
                    }
                }
            });
        }

        public void bind(SingleInvocation invocation) {
            currentInvocation = invocation;
            invocationText.setText(invocation.getText());
            updateCountDisplay();
        }

        private void updateCountDisplay() {
            if (currentInvocation.getCurrentCount() > 0) {
                invocationCount.setText(String.valueOf(currentInvocation.getCurrentCount()));
                invocationCount.setVisibility(View.VISIBLE);
                doneCheckButton.setVisibility(View.INVISIBLE);
                decrementButton.setEnabled(true);
            } else {
                invocationCount.setVisibility(View.INVISIBLE);
                doneCheckButton.setVisibility(View.VISIBLE);
                decrementButton.setEnabled(false);
            }
        }
    }
}