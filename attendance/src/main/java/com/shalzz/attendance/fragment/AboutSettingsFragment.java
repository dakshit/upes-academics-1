package com.shalzz.attendance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.bugsnag.android.Bugsnag;
import com.shalzz.attendance.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;

public class AboutSettingsFragment extends PreferenceFragment{

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Bugsnag.setContext("About");

        addPreferencesFromResource(R.xml.pref_about);
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceScreen prefScreen =  getPreferenceScreen();
        Preference noticePref = prefScreen.getPreference(0);
        noticePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(mContext).setNotices(R.raw.notices).setIncludeOwnLicense(true)
                        .build().show();
                return true;
            }
        });
    }

}
