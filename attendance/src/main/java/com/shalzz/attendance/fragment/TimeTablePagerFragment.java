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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.shalzz.attendance.DataAPI;
import com.shalzz.attendance.DataAssembler;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.Miscellaneous;
import com.shalzz.attendance.R;
import com.shalzz.attendance.UserAccount;
import com.shalzz.attendance.adapter.MySpinnerAdapter;
import com.shalzz.attendance.adapter.TimeTablePagerAdapter;
import com.shalzz.attendance.wrapper.MyPreferencesManager;
import com.shalzz.attendance.wrapper.MySyncManager;
import com.shalzz.attendance.wrapper.MyVolley;
import com.shalzz.attendance.wrapper.MyVolleyErrorHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TimeTablePagerFragment extends SherlockFragment {

	private TimeTablePagerAdapter mTimeTablePagerAdapter;
	private ViewPager mViewPager;
	private String myTag = "Pager Fragment";
	private Context mContext;
	private Miscellaneous misc;
	private ActionBar actionbar;
	private MySpinnerAdapter mSpinnerAdapter ;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		misc  = new Miscellaneous(mContext);
		actionbar= getSherlockActivity().getSupportActionBar();
		actionbar.setDisplayShowTitleEnabled(false);

		mSpinnerAdapter = new MySpinnerAdapter(mContext);

		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				Log.d(myTag,""+itemId);
				if(position == 0) {
					return true;
				}
				else if (position == 1) {
					scrollToToday();
					return true;
				}
				return false;
			}
		};

		actionbar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(container==null)
			return null;

		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.swipe_layout, container, false);

		mTimeTablePagerAdapter = new TimeTablePagerAdapter(getSherlockActivity().getSupportFragmentManager());
		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		mViewPager.setAdapter(mTimeTablePagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageScrollStateChanged(int state) {}

			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				updateSpinner();
			}

			public void onPageSelected(int position) {}
		});

		return view;
	}

	@Override
	public void onStart() {
		DatabaseHandler db = new DatabaseHandler(mContext);
		if(db.getRowCountofTimeTable()<=0) {
			DataAPI.getTimeTable(mContext, timeTableSuccessListener(), myErrorListener());
			misc.showProgressDialog("Loading your TimeTable...","Loading", true, pdCancelListener());
		} 
		else
			scrollToToday();
		super.onStart();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.time_table, menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view

		DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
		ListView mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);

		// set the Navigation mode to standard
		if(drawerOpen) {
			actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionbar.setDisplayShowTitleEnabled(true);
		}
		else {
			actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			actionbar.setDisplayShowTitleEnabled(false);
		}

		return;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_logout)
		{			
			new UserAccount(mContext).Logout();
		}
		else if(item.getItemId() == R.id.menu_refresh)
		{
			DataAPI.getTimeTable(mContext, timeTableSuccessListener(), myErrorListener());
			misc.showProgressDialog("Refreshing your TimeTable...","Refreshing", true, pdCancelListener());
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateFragments() {
		for (DayFragment fragment : mTimeTablePagerAdapter.getActiveFragments()) {
			Log.d("TimeTableActivity", "Update Fragment " + fragment.getDate() + " with new data.");
			//fragment.notifyDataSetChanged();
			fragment.setTimeTable();
		}
	}

	private void updateSpinner() {
		DayFragment fragment = mTimeTablePagerAdapter.getFragment(mViewPager.getCurrentItem());
		if(fragment!=null && actionbar.getNavigationMode()== ActionBar.NAVIGATION_MODE_LIST) {// Can happen because of asynchronous fragment transactions.
			mSpinnerAdapter.setDate(fragment.getDate());
		}
	}

	private void scrollToToday() {
		mViewPager.setCurrentItem(15, true);
		Log.d(myTag,"Scrolling to Today");
	}
	
	public void scrollToPosition(int position) {
		mViewPager.setCurrentItem(position);
	}

	DialogInterface.OnCancelListener pdCancelListener() {
		return new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// Cancel all pending requests when user presses back button.
				Crouton.makeText(getActivity(), "Refresh canceled", Style.INFO).show();
				MyVolley.getInstance().cancelPendingRequests(myTag);
			}
		};

	}

	private Response.Listener<String> timeTableSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
                misc.dismissProgressDialog();
                try {
                    if (DataAssembler.parseTimeTable(response, mContext) == 0) {
                        updateFragments();
                        scrollToToday();
                        Log.i(myTag, "Sync complete");
                        MyPreferencesManager prefs = new MyPreferencesManager(mContext);
                        prefs.setLastSyncTime();
                    }
                }
                catch(Exception e) {
                    Crouton.makeText((Activity) mContext, "An unexpected error occurred", Style.ALERT).show();
                }
			}
		};
	}

	private Response.ErrorListener myErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				misc.dismissProgressDialog();
				String msg = MyVolleyErrorHelper.getMessage(error, mContext);
				Crouton.makeText((Activity) mContext, msg, Style.ALERT).show();
				Log.e(myTag, msg);
			}
		};
	}

	@Override
	public void onResume() {
		mTimeTablePagerAdapter.notifyDataSetChanged();
		updateFragments();
		updateSpinner();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}
}
