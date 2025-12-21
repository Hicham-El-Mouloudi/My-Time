package com.ensao.mytime.sleep.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensao.mytime.home.AlarmScheduler;
import com.ensao.mytime.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionFragment extends Fragment implements AppSelectionAdapter.OnAppSelectedListener {

    private RecyclerView recyclerView;
    private AppSelectionAdapter adapter;
    private Set<String> blockedPackages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.app_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadBlockedAppsState();
        List<AppInfo> installedApps = loadInstalledApps();

        adapter = new AppSelectionAdapter(getContext(), installedApps, blockedPackages, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadBlockedAppsState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);

        Set<String> savedSet = prefs.getStringSet(AlarmScheduler.KEY_BLOCKED_APPS_SET, new HashSet<>());
        blockedPackages = new HashSet<>(savedSet);
    }

    private void saveBlockedAppsState() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet(AlarmScheduler.KEY_BLOCKED_APPS_SET, new HashSet<>(blockedPackages));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String blockedAppsString = String.join(",", blockedPackages);
            editor.putString(AlarmScheduler.KEY_BLOCKED_APPS_LIST, blockedAppsString);
        }

        editor.apply();


        Log.d("AppSelection", "Applications enregistrées dans " + AlarmScheduler.PREFS_NAME);
    }

    private List<AppInfo> loadInstalledApps() {
        if (getContext() == null) return new ArrayList<>();

        final PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<AppInfo> appList = new ArrayList<>();
        String myPackageName = getContext().getPackageName();

        for (ApplicationInfo packageInfo : packages) {
            Intent launchIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);

            if (launchIntent != null && !packageInfo.packageName.equals(myPackageName)) {
                try {
                    String appName = (String) pm.getApplicationLabel(packageInfo);
                    AppInfo app = new AppInfo(appName, packageInfo.packageName, pm.getApplicationIcon(packageInfo));
                    appList.add(app);
                } catch (Exception e) {
                    Log.e("AppSelection", "Erreur chargement app: " + packageInfo.packageName);
                }
            }
        }

        Collections.sort(appList, (a, b) -> a.name.compareToIgnoreCase(b.name));
        return appList;
    }

    @Override
    public void onAppSelected(String packageName, boolean isSelected) {
        if (isSelected) {
            blockedPackages.add(packageName);
        } else {
            blockedPackages.remove(packageName);
        }
        saveBlockedAppsState();

        String status = isSelected ? " bloquée" : " débloquée";
        Toast.makeText(getContext(), "App" + status, Toast.LENGTH_SHORT).show();
    }
}