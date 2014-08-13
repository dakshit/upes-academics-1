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
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;
import com.shalzz.attendance.DataAPI;
import com.shalzz.attendance.DataAssembler;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.Miscellaneous;
import com.shalzz.attendance.R;
import com.shalzz.attendance.UserAccount;
import com.shalzz.attendance.activity.MainActivity;
import com.shalzz.attendance.adapter.ExpandableListAdapter;
import com.shalzz.attendance.model.ListFooter;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.model.Subject;
import com.shalzz.attendance.wrapper.MyPreferencesManager;
import com.shalzz.attendance.wrapper.MySyncManager;
import com.shalzz.attendance.wrapper.MyVolley;
import com.shalzz.attendance.wrapper.MyVolleyErrorHelper;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class AttendanceListFragment extends SherlockListFragment{

	private View footer;
	private View header;
	private Context mContext;
	private Miscellaneous misc;
	private String myTag ;
	private ExpandableListAdapter mAdapter;
	private SwingRightInAnimationAdapter animationAdapter;
	private ListView mlistview;
    private TextView last_refreshed;
<<<<<<< HEAD
    private SmoothProgressBar smoothProgressBar;
=======
    private int expandLimit;
    private final int ADAPTER_DELAY_MILLIS = 750;
>>>>>>> upstream/master

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		misc = new Miscellaneous(mContext);
		myTag = getActivity().getLocalClassName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(container==null)
			return null;

		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.attenview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mlistview = getListView();
		LayoutInflater inflater = this.getLayoutInflater(savedInstanceState);

		header = inflater.inflate(R.layout.list_header, null);
		mlistview.addHeaderView(header);

		footer=inflater.inflate(R.layout.list_footer, null);
		mlistview.addFooterView(footer);	

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		DatabaseHandler db = new DatabaseHandler(mContext);

        //smoothProgressBar
        smoothProgressBar = (SmoothProgressBar) getActivity().findViewById(R.id.smoothProgressBar);
        last_refreshed = (TextView) getActivity().findViewById(R.id.last_refreshed);

		if(db.getRowCount()<=0) {
			String SAPID = getSherlockActivity().getIntent().getExtras().getString("SAPID");
			MySyncManager.addPeriodicSync(mContext,SAPID);
			DataAPI.getAttendance(mContext, successListener(), errorListener());
			//misc.showProgressDialog("Loading your attendance...","Loading" ,true, pdCancelListener());
            smoothProgressBar.setVisibility(View.VISIBLE);
            last_refreshed.setVisibility(View.GONE);
		}
		else
			setAttendance();

        updateLastRefresh();
		super.onStart();
	}

    protected void updateLastRefresh() {
        MyPreferencesManager prefs = new MyPreferencesManager(mContext);
        last_refreshed.setText("Last refreshed "+prefs.getLastSyncTime()+" hours ago");
    }

    public void showcaseView() {
        new ShowcaseView.Builder(getActivity())
                .setStyle(R.style.Theme_Sherlock_Light_DarkActionBar)
                .setTarget(new ViewTarget(mlistview))
                .setContentTitle("Expandable item")
                .setContentText("Touch a Subject for more details about it")
                .build();
    }

	private void setAttendance() {
		DatabaseHandler db = new DatabaseHandler(getActivity());
		if(db.getRowCount()>0)
		{
			updateHeaderNFooter();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
			boolean alpha = sharedPref.getBoolean("alpha_subject_order", true);
			expandLimit = Integer.parseInt(sharedPref.getString("subjects_expanded_limit", "0"));

			List<Subject> subjects;
			if(alpha) 
				subjects = db.getAllOrderedSubjects();
			else 
				subjects = db.getAllSubjects();
			

			mAdapter = new ExpandableListAdapter(mContext,subjects);
			mAdapter.setLimit(expandLimit);
			animationAdapter = new SwingRightInAnimationAdapter(mAdapter);
			animationAdapter.setAbsListView(mlistview);
			animationAdapter.setInitialDelayMillis(ADAPTER_DELAY_MILLIS);
			mlistview.setAdapter(animationAdapter);

		}
	}

	private void updateHeaderNFooter() {

		TextView tvPercent = (TextView) footer.findViewById(R.id.tvTotalPercent);
		TextView tvClasses = (TextView) footer.findViewById(R.id.tvClass);
		ProgressBar pbPercent = (ProgressBar) footer.findViewById(R.id.pbTotalPercent);
		DatabaseHandler db = new DatabaseHandler(mContext);
		ListFooter listfooter = db.getListFooter();
		Float percent = listfooter.getPercentage();

		Rect bounds = pbPercent.getProgressDrawable().getBounds();
		if(percent<67.0) {
			pbPercent.setProgressDrawable(getResources().getDrawable(R.drawable.progress_amber));
		}
		else if(percent<75.0) {
			pbPercent.setProgressDrawable(getResources().getDrawable(R.drawable.progress_yellow));
		}
		pbPercent.getProgressDrawable().setBounds(bounds);

		tvPercent.setText(listfooter.getPercentage()+"%");
		tvClasses.setText(listfooter.getAttended().intValue()+"/"+listfooter.getHeld().intValue());
		pbPercent.setProgress(percent.intValue());

		TextView tvName = (TextView) header.findViewById(R.id.tvName);
		TextView tvSap = (TextView) header.findViewById(R.id.tvSAP);
		TextView tvcourse = (TextView) header.findViewById(R.id.tvCourse);

		ListHeader listheader = db.getListHeader();
		tvName.setText(listheader.getName());
		tvSap.setText(String.valueOf(listheader.getSAPId()));
		tvcourse.setText(listheader.getCourse());
		
		MainActivity.getInstance().updateDrawerHeader();

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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint("Search subjects");

		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				DatabaseHandler db = new DatabaseHandler(mContext);
				List<Subject> subjects = db.getAllOrderedSubjects();
                mAdapter = new ExpandableListAdapter(mContext,subjects);
                mAdapter.setLimit(expandLimit);
                animationAdapter = new SwingRightInAnimationAdapter(mAdapter);
                animationAdapter.setAbsListView(mlistview);
                animationAdapter.setInitialDelayMillis(ADAPTER_DELAY_MILLIS);
				mlistview.setAdapter(animationAdapter);
				return true;  // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded
				return true;  // Return true to expand action view
			}
		});

		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				Miscellaneous.closeKeyboard(mContext, searchView);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
				DatabaseHandler db = new DatabaseHandler(mContext);
				List<Subject> subjects = db.getAllSubjectsLike(arg0);
                mAdapter = new ExpandableListAdapter(mContext,subjects);
                mAdapter.setLimit(expandLimit);
                animationAdapter = new SwingRightInAnimationAdapter(mAdapter);
                animationAdapter.setAbsListView(mlistview);
                animationAdapter.setInitialDelayMillis(ADAPTER_DELAY_MILLIS);
                mlistview.setAdapter(animationAdapter);
				return false;
			}
		});
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view

		DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
		ListView mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
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
			DataAPI.getAttendance(mContext, successListener(), errorListener());
			//misc.showProgressDialog("Refreshing your attendance...","Refreshing",true, pdCancelListener());
            smoothProgressBar.setVisibility(View.VISIBLE);
            last_refreshed.setVisibility(View.GONE);
		}
		return super.onOptionsItemSelected(item);
	}

	private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {				
				//misc.dismissProgressDialog();
                smoothProgressBar.setVisibility(View.GONE);
                last_refreshed.setVisibility(View.VISIBLE);
                try {
                    DataAssembler.parseStudentDetails(response, mContext);
                    DataAssembler.parseAttendance(response, mContext);
                    setAttendance();
                    MyPreferencesManager prefs = new MyPreferencesManager(mContext);
                    prefs.setLastSyncTime();
                    updateLastRefresh();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Crouton.makeText((Activity) mContext, "An unexpected error occurred", Style.ALERT).show();
                }
			}
		};
	}

	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//misc.dismissProgressDialog();
                smoothProgressBar.setVisibility(View.GONE);
                last_refreshed.setVisibility(View.VISIBLE);

				String msg = MyVolleyErrorHelper.getMessage(error, mContext);
				Crouton.makeText((Activity) mContext, msg, Style.ALERT).show();
				Log.e(myTag, msg);
			}
		};
	}

	@Override
	public void onPause() {
		super.onPause();
	};
	
	@Override
	public void onResume() {
		setAttendance();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}
}
