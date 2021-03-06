package com.shalzz.attendance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.bugsnag.android.Bugsnag;
import com.shalzz.attendance.R;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense20;
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
        Preference pref = prefScreen.getPreference(0);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String name = "UPES Academics";
                final String url = "http://www.github.com/shalzz/upes-academics";
                final String copyright = "Copyright (C) 2013 - 2015 Shaleen Jain <shaleen.jain95@gmail.com>";
                final License license = new GnuGeneralPublicLicense20();
                final Notice notice = new Notice(name, url, copyright, license);
                new LicensesDialog.Builder(mContext).setNotices(notice).setShowFullLicenseText(true).build().show();
                return true;
            }
        });
        Preference noticePref = prefScreen.getPreference(1);
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
