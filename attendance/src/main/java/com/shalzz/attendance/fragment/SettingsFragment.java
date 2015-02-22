/*
 * Copyright (c) 2014 Shaleen Jain <shaleen.jain95@gmail.com>
 *
 * This file is part of UPES Academics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shalzz.attendance.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.bugsnag.android.Bugsnag;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.activity.MainActivity;
import com.shalzz.attendance.wrapper.MySyncManager;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
        Bugsnag.setContext("Settings");

		addPreferencesFromResource(R.xml.preferences);

        String key = "subjects_expanded_limit";
		ListPreference listPref = (ListPreference) findPreference(key);
		listPref.setSummary(listPref.getEntry());

        key = "pref_batch";
        ListPreference listPref_batch = (ListPreference) findPreference(key);
        listPref_batch.setSummary(listPref_batch.getEntry());

		key = "data_sync_interval";
		ListPreference synclistPref = (ListPreference) findPreference(key);
		synclistPref.setSummary(synclistPref.getEntry());
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "subjects_expanded_limit": {
                ListPreference connectionPref = (ListPreference) findPreference(key);
                connectionPref.setSummary(connectionPref.getEntry());
                break;
            }
            case "pref_batch": {
                ListPreference connectionPref = (ListPreference) findPreference(key);
                connectionPref.setSummary(connectionPref.getEntry());
                break;
            }
            case "data_sync_interval": {
                DatabaseHandler db = new DatabaseHandler(mContext);
                ListPreference connectionPref = (ListPreference) findPreference(key);
                connectionPref.setSummary(connectionPref.getEntry());
                MySyncManager.addPeriodicSync(mContext, "" + db.getListHeader().getSAPId());
                break;
            }
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

		PreferenceCategory prefCategory = (PreferenceCategory) getPreferenceScreen().getPreference(3);
		PreferenceScreen prefScreen =  (PreferenceScreen) prefCategory.getPreference(0);
        prefScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
                Fragment mFragment = new AboutSettingsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.frame_container, mFragment, MainActivity.FRAGMENT_TAG);
                transaction.addToBackStack(null);
                ((MainActivity)getActivity()).mPopSettingsBackStack = true;

                transaction.commit();
				return true;
			}
		});

        PreferenceCategory proxyPrefCategory = (PreferenceCategory) getPreferenceScreen().getPreference(2);
        PreferenceScreen proxyPrefScreen =  (PreferenceScreen) proxyPrefCategory.getPreference(2);
        proxyPrefScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Fragment mFragment = new ProxySettings();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.frame_container, mFragment, MainActivity.FRAGMENT_TAG);
                transaction.addToBackStack(null);
                ((MainActivity)getActivity()).mPopSettingsBackStack = true;

                transaction.commit();
                return true;
            }
        });
	}
}