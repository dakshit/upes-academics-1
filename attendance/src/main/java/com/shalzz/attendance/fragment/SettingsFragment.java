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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.wrapper.MySyncManager;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense30;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();

		addPreferencesFromResource(R.xml.preferences);

		String key = "pref_key_proxy_username";
		Preference connectionPref = findPreference(key);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		connectionPref.setSummary(sharedPref.getString(key, ""));

		key = "subjects_expanded_limit";
		ListPreference listPref = (ListPreference) findPreference(key);
		listPref.setSummary(listPref.getEntry());

		key = "data_sync_interval";
		ListPreference synclistPref = (ListPreference) findPreference(key);
		synclistPref.setSummary(synclistPref.getEntry());
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("pref_key_proxy_username")) {
			Preference connectionPref = findPreference(key);
			connectionPref.setSummary(sharedPreferences.getString(key, ""));
		}
		else if(key.equals("subjects_expanded_limit")) {
			ListPreference connectionPref = (ListPreference) findPreference(key);
			connectionPref.setSummary(connectionPref.getEntry());
		}
		else if(key.equals("data_sync_interval")) {
			DatabaseHandler db = new DatabaseHandler(mContext);
			ListPreference connectionPref = (ListPreference) findPreference(key);
			connectionPref.setSummary(connectionPref.getEntry());
			MySyncManager.addPeriodicSync(mContext,""+db.getListHeader().getSAPId());
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
		Preference pref = prefScreen.getPreference(0);
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
		        final String name = "UPES Academics";
		        final String url = "http://www.github.com/shalzz/upes-academics";
		        final String copyright = "Copyright (C) 2013 - 2014 Shaleen Jain <shaleen.jain95@gmail.com>";
		        final License license = new GnuGeneralPublicLicense30();
		        final Notice notice = new Notice(name, url, copyright, license);
                new LicensesDialog.Builder(mContext).setNotices(notice).build().show();
				return true;
			}
		});
		Preference noticePref = prefScreen.getPreference(1);
		noticePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(mContext).setNotices(R.raw.notices).build().show();
				return true;
			}
		});
	}
}