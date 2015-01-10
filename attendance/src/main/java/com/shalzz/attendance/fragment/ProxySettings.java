package com.shalzz.attendance.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;
import com.shalzz.attendance.R;

public class ProxySettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context mContext = getActivity();
        Bugsnag.setContext("ProxySettings");

        addPreferencesFromResource(R.xml.pref_proxy);

        String key = "pref_key_proxy_username";
        Preference connectionPref = findPreference(key);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        connectionPref.setSummary(sharedPref.getString(key, ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_key_proxy_username")) {
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
}
