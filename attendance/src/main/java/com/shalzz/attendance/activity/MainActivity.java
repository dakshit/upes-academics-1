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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
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

import com.bugsnag.android.Bugsnag;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.fragment.AttendanceListFragment;
import com.shalzz.attendance.fragment.SettingsFragment;
import com.shalzz.attendance.fragment.TimeTablePagerFragment;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.wrapper.MyPreferencesManager;
import com.shalzz.attendance.wrapper.MyVolley;

public class MainActivity extends ActionBarActivity {

    /**
     * Reference to fragment positions
     */
    public static enum Fragments {
        ATTENDANCE(1),
        TIMETABLE(2),
        SETTINGS(3);

        private final int value;

        private Fragments(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Drawer lock state. True for tablets, false otherwise .
     */
    private boolean isDrawerLocked = false;

    /**
     * To prevent saving the drawer position when logging out.
     */
    public static boolean LOGGED_OUT = false;

    /**
     * Remember the position of the selected item.
     */
    public static String PREFERENCE_ACTIVATED_FRAGMENT = "ACTIVATED_FRAGMENT3.0";

    public static final String FRAGMENT_TAG = "MainActivity.FRAGMENT";

    private static final String PREVIOUS_FRAGMENT_TAG = "MainActivity.PREVOIUS_FRAGMENT";

    private static final String mTag = "MainActivity";

    /**
     * Debug flag
     */
    private final Boolean DEBUG_FRAGMENTS = true;

    private int mCurrentSelectedPosition = 0;
    private static MainActivity mActivity;
    private String[] mNavTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private View Drawerheader;
    private FragmentManager mFragmentManager;
    private Fragment fragment = null;
    private ActionBar actionbar;
    public View dropShadow;
    // Our custom poor-man's back stack which has only one entry at maximum.
    private Fragment mPreviousFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);
        Bugsnag.setContext("MainActivity");

        // set toolbar as actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavTitles = getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        dropShadow = findViewById(R.id.drop_shadow);
        mFragmentManager = getFragmentManager();
        mTitle  = getTitle();
        actionbar = getSupportActionBar();
        mActivity = this;

        // Check for tablet layout
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_container);
        if(((ViewGroup.MarginLayoutParams)frameLayout.getLayoutParams()).leftMargin == (int)getResources().getDimension(R.dimen.drawer_size)) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, mDrawerList);
            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
            Log.i(mTag,"Tablet layout applied");
        }

        // set Drawer header
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Drawerheader = inflater.inflate(R.layout.drawer_header, null);
        if(mDrawerList.getHeaderViewsCount()==0)
            mDrawerList.addHeaderView(Drawerheader);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item,R.id.drawer_list_textView, mNavTitles));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

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

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        // Set the drawer toggle as the DrawerListener
        if(!isDrawerLocked) {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
        }

        // Select either the default item (0) or the last selected item.
        mCurrentSelectedPosition = reloadCurrentFragment();

        // Recycle fragment
        if(savedInstanceState != null) {
            fragment =  getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            mPreviousFragment = getFragmentManager().getFragment(savedInstanceState, PREVIOUS_FRAGMENT_TAG);
            Log.d(mTag, "current fag found: " + fragment );
            Log.d(mTag, "previous fag found: " + mPreviousFragment );
            showFragment(fragment);
            selectItem(mCurrentSelectedPosition);
        }
        else {
            displayView(mCurrentSelectedPosition);
        }

        updateDrawerHeader();
        showcaseView();
    }

    void showcaseView() {
        MyPreferencesManager prefs = new MyPreferencesManager(this);
        if(prefs.isFirstLaunch(mTag)) {

            Target homeTarget = new Target() {
                @Override
                public Point getPoint() {
                    // Get approximate position of home icon's center
                    int actionBarSize = getSupportActionBar().getHeight();
                    int x = actionBarSize / 2;
                    int y = actionBarSize / 2;
                    return new Point(x, y);
                }
            };

            final ShowcaseView sv = new ShowcaseView.Builder(this)
                    .setTarget(homeTarget)
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
            default:
                break;
        }

        if (fragment != null) {
            showFragment(fragment);
            selectItem(position);
        } else {
            Log.e(mTag, "Error in creating fragment");
        }
    }

    /**
     * Update selected item and title, then close the drawer
     * @param position the item to highlight
     */
    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerTitle = mNavTitles[position-1];
        setTitle(mDrawerTitle);
        if(!isDrawerLocked && mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawer(mDrawerList);
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
            return;
        }

        if (mPreviousFragment != null) {
            if (DEBUG_FRAGMENTS) {
                Log.d(mTag, this + " showFragment: destroying previous fragment "
                        + mPreviousFragment.getClass().getSimpleName());
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.remove(mPreviousFragment);
            mPreviousFragment = null;
        }

        // Remove the current fragment and push it into the backstack.
        if (installed != null) {
            mPreviousFragment = installed;
            ft.detach(mPreviousFragment);
        }

        // Show the new one
        ft.add(R.id.frame_container,fragment,FRAGMENT_TAG);
        if(fragment instanceof SettingsFragment)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        else
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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
            if(!getFragmentManager().popBackStackImmediate())
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
        Log.i(mTag, this + " backstack: [pop] " + installed.getClass().getSimpleName() + " -> "
                + mPreviousFragment.getClass().getSimpleName());

        ft.remove(installed);
        ft.attach(mPreviousFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();

        // redraw fragment
        if (mPreviousFragment instanceof AttendanceListFragment) {
            position = Fragments.ATTENDANCE.getValue();
        } else if (mPreviousFragment instanceof TimeTablePagerFragment) {
            position = Fragments.TIMETABLE.getValue();
            ((TimeTablePagerFragment) mPreviousFragment).notifyDataSetChanged();
        }
        selectItem(position);
        mPreviousFragment = null;
    }

    private void persistCurrentFragment() {
        if(!LOGGED_OUT) {
            SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", 0).edit();
            mCurrentSelectedPosition = mCurrentSelectedPosition == Fragments.SETTINGS.getValue() ?
                    Fragments.ATTENDANCE.getValue() : mCurrentSelectedPosition;
            editor.putInt(PREFERENCE_ACTIVATED_FRAGMENT, mCurrentSelectedPosition).apply();
        }
    }

    private int reloadCurrentFragment() {
        SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
        return settings.getInt(PREFERENCE_ACTIVATED_FRAGMENT, Fragments.ATTENDANCE.getValue());
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        actionbar.setTitle(mTitle);
        actionbar.setSubtitle("");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * @return currently installed {@link Fragment} (1-pane has only one at most), or null if none
     *         exists.
     */
    private Fragment getInstalledFragment() {
        return mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
    }

    private boolean isAttendanceListInstalled() {
        return getInstalledFragment() instanceof AttendanceListFragment;
    }

    private boolean isTimeTablePagerInstalled() {
        return getInstalledFragment() instanceof TimeTablePagerFragment;
    }

    private boolean isSettingsInstalled() {
        return getInstalledFragment() instanceof SettingsFragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // for orientation changes, etc.
        if (mPreviousFragment != null) {
            getFragmentManager()
                    .putFragment(outState, PREVIOUS_FRAGMENT_TAG, mPreviousFragment);
            Log.d(mTag, "previous fag saved: " + mPreviousFragment.getClass().getSimpleName());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        actionbar.setTitle(mTitle);
    }

    @Override
    public void onPause() {
        persistCurrentFragment();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MyVolley.getInstance().cancelPendingRequests("com.shalzz.attendance.fragment.AttendanceListFragment");
        MyVolley.getInstance().cancelPendingRequests("com.shalzz.attendance.fragment.TimeTablePagerFragment");
        super.onDestroy();
    }

}