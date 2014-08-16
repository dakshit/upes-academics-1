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

package com.shalzz.attendance.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import com.shalzz.attendance.R;
import com.shalzz.attendance.activity.MainActivity;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MyPreferencesManager {

	/**
	 * The activity context.
	 */
	private Context mContext;

	/**
	 * Constructor to set the Activity context.
	 * @param context
	 */
	public MyPreferencesManager(Context context) {
		mContext = context;
	}
	
	public void setLastSyncTime() {
		Time now = new Time();
		now.setToNow();
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("REFRESH_TIME", now.toMillis(false));
		editor.commit();
	}
	
	public long getLastSyncTime() {
        Time now = new Time();
        now.setToNow();
        Long now_L = now.toMillis(false);
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		Long last_sync = settings.getLong("REFRESH_TIME", now_L );
		return (now_L-last_sync)/(1000*60*60); // convert milliseconds to hours
	}

	/**
	 * Gets the cookies from the shared preferences and adds them to the default CookieManager.
	 */
	public void getPersistentCookies()
	{
		CookieManager cookieMan = (CookieManager) CookieHandler.getDefault();
		SharedPreferences pcookies = mContext.getSharedPreferences("PERSISTCOOKIES", 0);	
		Iterator<String> keyset = pcookies.getAll().keySet().iterator();
		if(keyset.hasNext())
		{
			Log.i(mContext.getClass().getName(), "Persistent cookies found.");
			while(keyset.hasNext())
			{
				String cookiename = keyset.next();
				String cookievalue = pcookies.getString(cookiename, "");
				if(!cookievalue.isEmpty()) 
				{
					try {
						HttpCookie cookie = new HttpCookie(cookiename,cookievalue);
						cookie.setDomain("academics.ddn.upes.ac.in");
						cookie.setPath("/");
						cookie.setVersion(0);
						cookieMan.getCookieStore().add(new URI(mContext.getResources().getString(R.string.URL_home)), cookie);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
				else
				{
					Log.i(mContext.getClass().getName(), "Persistent cookies not found.");
				}
			}
		}
	}

	/**
	 * Saves the cookies in shared preferences.
	 */
	public void savePersistentCookies() {
		CookieManager cookieMan = (CookieManager) CookieHandler.getDefault();
		SharedPreferences persistentcookies = mContext.getSharedPreferences("PERSISTCOOKIES", 0);
		SharedPreferences.Editor editor = persistentcookies.edit();
		for(HttpCookie cookie : cookieMan.getCookieStore().getCookies() ){
			editor.putString(cookie.getName(), cookie.getValue());
		}
		editor.commit();
	}

	/**
	 * Removes the cookies from the shared preferences and Cookie Manager
	 */
	public void removePersistenCookies() {
		SharedPreferences pcookies = mContext.getSharedPreferences("PERSISTCOOKIES", 0);
		SharedPreferences.Editor editor = pcookies.edit();
		Iterator<String> keyset = pcookies.getAll().keySet().iterator();
		while(keyset.hasNext())
		{
			String cookiename = keyset.next();
			editor.remove(cookiename);
		}
		editor.commit();
		
		CookieManager cookieMan = (CookieManager) CookieHandler.getDefault();
		cookieMan.getCookieStore().removeAll();
	}

	/**
	 * Gets the login status from the preferences
	 * @return true if logged in else false
	 */
	public boolean getLoginStatus() {

		Log.i(mContext.getClass().getName(), "Getting Logged in state.");
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		boolean loggedin = settings.getBoolean("LOGGEDIN", false);
		Log.d(mContext.getClass().getName(), "Logged in state: "+loggedin+ "");
		return loggedin;
	}

	/**
	 * Saves the user details in shared preferences and sets login status to true.
	 * @param username
	 * @param password
	 */
	public void saveUser(String username, String password) {
		Log.i(mContext.getClass().getName(), "Setting LOGGEDIN pref to true");
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("LOGGEDIN", true);
		//editor.putString("USERNAME", username);
		//editor.putString("PASSWORD", password);
		editor.commit();
	}

	/**
	 * Removes the user details from the shared preferences and sets login status to false.
	 */
	public void removeUser() {	
		Log.i(mContext.getClass().getName(), "Setting LOGGEDIN pref to false");
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
        editor.putInt(MainActivity.PREFERENCE_ACTIVATED_FRAGMENT, 1);
		editor.putBoolean("LOGGEDIN", false);
		editor.remove("USERNAME");
		editor.remove("PASSWORD");
		editor.commit();
	}

    /**
     * Checks weather this is the first time the app is launched or not.
     * @return True or False
     * @param name
     */
    public boolean isFirstLaunch() {
        SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
        boolean boot = settings.getBoolean("FIRSTLAUNCH"+mContext.getClass().getName(), true);
        return boot;
    }

    /**
     * Sets the first launch to false.
     */
    public void setFirstLaunch() {
        SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("FIRSTLAUNCH"+mContext.getClass().getName(), false);
        editor.commit();
    }
}