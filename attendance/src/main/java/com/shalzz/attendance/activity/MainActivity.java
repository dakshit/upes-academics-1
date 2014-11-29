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

package com.shalzz.attendance.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.fragment.AttendanceListFragment;
import com.shalzz.attendance.fragment.SettingsFragment;
import com.shalzz.attendance.fragment.TimeTablePagerFragment;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.wrapper.MyPreferencesManager;
import com.shalzz.attendance.wrapper.MyVolley;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends Activity {

    /**
     * Used to lock drawer on tablets.
     */
    private boolean isDrawerLocked = false;

    /**
     * To prevent onDestroy functions when logging out.
     */
    public static boolean LOGGED_OUT = false;

    /**
     * Persistent fragment identifier + current app version.
     */
    public static String PREFERENCE_ACTIVATED_FRAGMENT = "ACTIVATED_FRAGMENT2.2";

    private static final String FRAGMENT_TAG = "MainActivity.FRAGMENT_TAG";
	private static final String mTag = "Main Activity";

    private static MainActivity mActivity;
	private String[] mNavTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private View Drawerheader;
	private FragmentManager mFragmentManager;
	private final Boolean DEBUG_FRAGMENTS = true;
    private Fragment fragment = null;
    private ActionBar actionbar;

	// Our custom poor-man's back stack which has only one entry at maximum.
	private Fragment mPreviousFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer);

		mNavTitles = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		mFragmentManager = getFragmentManager();

        // Check for tablet layout
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_container);
        if(((ViewGroup.MarginLayoutParams)frameLayout.getLayoutParams()).leftMargin == (int)getResources().getDimension(R.dimen.drawer_size)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, mDrawerList);
            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
            Log.i(mTag,"Tablet layout applied");
        }

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Drawerheader = inflater.inflate(R.layout.drawer_header, null);
		if(mDrawerList.getHeaderViewsCount()==0)
			mDrawerList.addHeaderView(Drawerheader);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item,R.id.drawer_list_textView, mNavTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mTitle  = getTitle();

		actionbar = getActionBar();

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                 R.drawable.ic_drawer,R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
                actionbar.setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				actionbar.setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

//        actionbar.setDisplayUseLogoEnabled(false);
//        actionbar.setDisplayShowHomeEnabled(false);
//        mDrawerToggle.setDrawerIndicatorEnabled(true);

		// Set the drawer toggle as the DrawerListener
        if(!isDrawerLocked) {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
        }

		reloadCurrentFragment();

		updateDrawerHeader();
		mActivity = this;

        showcaseView();
	}

    void showcaseView() {
        MyPreferencesManager prefs = new MyPreferencesManager(this);
        if(prefs.isFirstLaunch(mTag)) {
            final ShowcaseView sv = new ShowcaseView.Builder(this)
                    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
                    .setStyle(R.style.AppBaseTheme)
                    .setContentTitle("Navigation bar")
                    .setContentText("Press this button or swipe from the left edge to access the navigation bar")
                    .build();

            sv.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sv.hide();
                    if(fragment instanceof AttendanceListFragment)
                    {
                        ((AttendanceListFragment) fragment).showcaseView();
                    }
                }
            });
            prefs.setFirstLaunch(mTag);
        }
    }

	public static MainActivity getInstance(){
		return mActivity;
	}

	public void updateDrawerHeader() {
		DatabaseHandler db = new DatabaseHandler(this);
		if(db.getRowCount()>0) {
			ListHeader listheader = db.getListHeader();

			TextView tv_name = (TextView) Drawerheader.findViewById(R.id.drawer_header_name);
			TextView tv_course = (TextView) Drawerheader.findViewById(R.id.drawer_header_course);
			tv_name.setText(listheader.getName());
			tv_course.setText(listheader.getCourse());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
		{
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}
		return super.onOptionsItemSelected(item);
	}


	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayView(position);
		}
	}

	void displayView(int position) {
		// update the main content by replacing fragments
		switch (position) {
		case 0:
			return;
		case 1:
			fragment = new AttendanceListFragment();
			break;
		case 2:
			fragment = new TimeTablePagerFragment();
			break;
		case 3:
			fragment = new SettingsFragment();
			break;
		case 4:
			return;

		default:
			break;
		}

		if (fragment != null) {
			// show the fragment
			showFragment(fragment);

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			mDrawerTitle = mNavTitles[position-1];
			setTitle(mDrawerTitle);
            if(!isDrawerLocked)
			    mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			Log.e(mTag, "Error in creating fragment");
		}
	}
	
	private void persistCurrentFragment() {
        if(!LOGGED_OUT) {
            SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
            SharedPreferences.Editor editor = settings.edit();
            int fragmentPosition = 1;
            Fragment installed = getInstalledFragment();

            if (installed instanceof AttendanceListFragment) {
                fragmentPosition = 1;
            } else if (installed instanceof TimeTablePagerFragment) {
                fragmentPosition = 2;
            }

            if (DEBUG_FRAGMENTS) {
                Log.i(mTag, this + " persistCurrentFragment: Saving fragment " + installed + " at position " + fragmentPosition);
            }

            editor.putInt(PREFERENCE_ACTIVATED_FRAGMENT, fragmentPosition);
            editor.commit();
        }
	}

	private void reloadCurrentFragment() {
		SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
		int fragmentPosition = settings.getInt(PREFERENCE_ACTIVATED_FRAGMENT, 1);
		
		if (DEBUG_FRAGMENTS) {
			Log.i(mTag, this + " reloadCurrentFragment: Restoring fragment positon " + fragmentPosition);
		}
		
		displayView(fragmentPosition);
		}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		actionbar.setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	/**
	 * Push the installed fragment into our custom back stack (or optionally
	 * {@link FragmentTransaction#remove} it) and {@link FragmentTransaction#add} {@code fragment}.
	 *
	 * @param fragment {@link Fragment} to be added.
	 *
	 */
	private void showFragment(Fragment fragment) {
		final FragmentTransaction ft = mFragmentManager.beginTransaction();
		final Fragment installed = getInstalledFragment();

		// return if the fragment is already installed 
		if(isAttendanceListInstalled() && fragment instanceof AttendanceListFragment ||
		   isTimeTablePagerInstalled() && fragment instanceof TimeTablePagerFragment ||
		   isSettingsInstalled() && fragment instanceof SettingsFragment) {
			
			if (DEBUG_FRAGMENTS) {
				Log.i(mTag, this + " showFragment: " + fragment + " is already installed");
			}
			return;
		}

		if (DEBUG_FRAGMENTS) {
			Log.i(mTag, this + " backstack: [push] " + installed
					+ " -> " + fragment);
		}

		if (mPreviousFragment != null) {
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " showFragment: destroying previous fragment "
						+ mPreviousFragment);
			}
			ft.remove(mPreviousFragment);
			mPreviousFragment = null;
		}

		// Remove the current fragment or push it into the backstack.
		if (installed != null) {
			mPreviousFragment = installed;
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " showFragment: detaching "
						+ mPreviousFragment);
			}
			ft.detach(mPreviousFragment);
		}
		// Show the new one
		if (DEBUG_FRAGMENTS) {
			Log.d(mTag, this + " showFragment: replacing with " + fragment);
		}
		ft.replace(R.id.frame_container, fragment, FRAGMENT_TAG);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	@Override
	public void onBackPressed() {
        // close drawer if it is open
        if (mDrawerLayout.isDrawerOpen(mDrawerList) && !isDrawerLocked)
        {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
		// Custom back stack
		else if (shouldPopFromBackStack()) {
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " Back: Popping from back stack");
			}
			popFromBackStack();
		}
		else
			super.onBackPressed();
	}

	/**
	 * @return true if we should pop from our custom back stack.
	 */
	private boolean shouldPopFromBackStack() {

		if (mPreviousFragment == null) {
			return false; // Nothing in the back stack
		}
		final Fragment installed = getInstalledFragment();
		if (installed == null) {
			// If no fragment is installed right now, do nothing.
			return false;
		}
		// Okay now we have 2 fragments; the one in the back stack and the one that's currently
		// installed.
        return !(installed instanceof AttendanceListFragment ||
                installed instanceof TimeTablePagerFragment);

    }

	/**
	 * Pop from our custom back stack.
	 */
	private void popFromBackStack() {
		if (mPreviousFragment == null) {
			return;
		}
		final FragmentTransaction ft = mFragmentManager.beginTransaction();
		final Fragment installed = getInstalledFragment();     
		int position = 0 ;
		Log.i(mTag, this + " backstack: [pop] " + installed + " -> "
				+ mPreviousFragment);
		ft.remove(installed);
		// Restore listContext.
		if (mPreviousFragment instanceof AttendanceListFragment) {
			position = 1;
		} else if (mPreviousFragment instanceof TimeTablePagerFragment) {
			position = 2;
		} else if (mPreviousFragment instanceof SettingsFragment) {
			position = 3;
		}
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position-1);
		mDrawerTitle = mNavTitles[position-1];
		setTitle(mDrawerTitle);

		ft.attach(mPreviousFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		mPreviousFragment = null;
		ft.commit();
	}

	/**
	 * @return currently installed {@link Fragment} (1-pane has only one at most), or null if none
	 *         exists.
	 */
	private Fragment getInstalledFragment() {
		return mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
	}

	private boolean isAttendanceListInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
        return mFragment instanceof AttendanceListFragment;
    }
	
	private boolean isTimeTablePagerInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
        return mFragment instanceof TimeTablePagerFragment;
    }
	
	private boolean isSettingsInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
        return mFragment instanceof SettingsFragment;
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
        actionbar.setTitle(mTitle);
	}

    @Override
    public void onPause() {
        mPreviousFragment = null;
        persistCurrentFragment();
        super.onPause();
    }

	@Override
	public void onDestroy() {
		MyVolley.getInstance().cancelPendingRequests("com.shalzz.attendance.AttendanceListFragment");
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

}