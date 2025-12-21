package com.ensao.mytime.sleep.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.R;

import java.util.List;
import java.util.Set;

/**
 * Adapter pour afficher la liste des applications avec une case à cocher.
 */
public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder> {

    private final Context context;
    private final List<AppInfo> appList;
    private final Set<String> blockedPackages;
    private final OnAppSelectedListener listener;

    public interface OnAppSelectedListener {
        void onAppSelected(String packageName, boolean isSelected);
    }

    public AppSelectionAdapter(Context context, List<AppInfo> appList, Set<String> blockedPackages, OnAppSelectedListener listener) {
        this.context = context;
        this.appList = appList;
        this.blockedPackages = blockedPackages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_app_selection, parent, false);
        return new AppViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        if (app == null) return;
        holder.appName.setText(app.name);
        holder.appIcon.setImageDrawable(app.icon);

        // 1. Supprimer temporairement les écouteurs pour éviter les effets de bord du recyclage
        holder.appCheckbox.setOnClickListener(null);
        holder.itemView.setOnClickListener(null);

        // 2. Définir l'état (coché ou non)
        boolean isChecked = blockedPackages.contains(app.packageName);
        holder.appCheckbox.setChecked(isChecked);
        // Change l'apparence si l'app est sélectionnée pour être bloquée
        holder.itemView.setAlpha(isChecked ? 1.0f : 0.7f);

        // 3. Ré-attacher les écouteurs proprement
        View.OnClickListener toggleAction = v -> {
            boolean newState = (v instanceof CheckBox) ? ((CheckBox) v).isChecked() : !holder.appCheckbox.isChecked();

            if (!(v instanceof CheckBox)) {
                holder.appCheckbox.setChecked(newState);
            }

            listener.onAppSelected(app.packageName, newState);
        };

        holder.appCheckbox.setOnClickListener(toggleAction);
        holder.itemView.setOnClickListener(toggleAction);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox appCheckbox;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            appCheckbox = itemView.findViewById(R.id.app_checkbox);
        }
    }
}