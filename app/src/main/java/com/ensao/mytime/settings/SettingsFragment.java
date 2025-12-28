package com.ensao.mytime.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.ensao.mytime.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(android.content.SharedPreferences sharedPreferences, String key) {
        if ("language".equals(key)) {
            String lang = sharedPreferences.getString(key, "en");
            LocaleHelper.setLocale(requireContext(), lang);
            requireActivity().recreate();
        }
    }
}