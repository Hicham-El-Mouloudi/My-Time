package com.ensao.mytime.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.ensao.mytime.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // SYNC: Ensure the preference is initialized with the current system language
        // if not set
        android.content.SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (!prefs.contains("language")) {
            String systemLang = java.util.Locale.getDefault().getLanguage();
            // Fallback to English if system lang is not supported (optional, but good
            // practice)
            if (!"fr".equals(systemLang)) {
                systemLang = "en";
            }
            prefs.edit().putString("language", systemLang).apply();

            // Force update UI of the preference widget itself if screen is already visible
            androidx.preference.ListPreference langPref = findPreference("language");
            if (langPref != null) {
                langPref.setValue(systemLang);
            }
        }
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

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.lang_restart_title)
                    .setMessage(R.string.lang_restart_message)
                    .setPositiveButton(R.string.lang_restart_ok, null)
                    .setCancelable(false)
                    .show();
        }
    }
}