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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.adapter.DayListAdapter;
import com.shalzz.attendance.model.Day;
import com.shalzz.attendance.wrapper.DateHelper;

import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class DayFragment extends SherlockListFragment {
	
	private Context mContext;
	private Date mDate;
	public static final String ARG_DATE = "date";
	private DayListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mDate = new Date();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(container==null)
			return null;
		
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.timetable_view, container, false);
	}

	@Override
	public void onStart() {
		DatabaseHandler db = new DatabaseHandler(mContext);
		if(db.getRowCountofTimeTable()>0) 
			setTimeTable();
		super.onStart();
	}
	
	public void setTimeTable() {
		DatabaseHandler db = new DatabaseHandler(getActivity());
		String weekday = getWeekDay();
		if(weekday.equals("sun"))
			return;
		Day day = db.getDay(weekday);
		mAdapter = new DayListAdapter(mContext, day);
		setListAdapter(mAdapter);
	}

	public String getWeekDay() {
		setDate();
		return DateHelper.getTechnicalWeekday(mDate);
	}
	
	public void setDate() {
		Date date = (Date) getArguments().getSerializable(ARG_DATE);
		if(date!=null) // Can happen because of asynchronous fragment transactions.
			mDate = date;
	}
	
	public Date getDate() {
		return mDate;
	}
	
	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		DatabaseHandler db = new DatabaseHandler(mContext);
		if(db.getRowCountofTimeTable()>0)
			setTimeTable();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}
}
