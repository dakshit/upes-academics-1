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

package com.shalzz.attendance.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.shalzz.attendance.fragment.DayFragment;
import com.shalzz.attendance.wrapper.DateHelper;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class TimeTablePagerAdapter extends FragmentStatePagerAdapter {

	@SuppressLint("UseSparseArrays")
	private final HashMap<Integer, DayFragment> activeFragments = new HashMap<Integer, DayFragment>();
    private Date date;
	
	public TimeTablePagerAdapter(FragmentManager fm, Date date) {
		super(fm);
        this.date = date;
	}

	@Override
	public Fragment getItem(int position) {
		DayFragment fragment = new DayFragment();
		Bundle args = new Bundle();
		args.putSerializable(DayFragment.ARG_DATE, DateHelper.addDays(date, -15+position));
		fragment.setArguments(args);
		
		activeFragments.put(position, fragment);
		
		return fragment;
	}

    public void setDate(Date date) {
        this.date = date;
        notifyDataSetChanged();
    }
	@Override
	public int getCount() {
		return 31;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		activeFragments.remove(position);
		super.destroyItem(container, position, object);
	}

	public Collection<DayFragment> getActiveFragments() {
		return activeFragments.values();
	}
	
	public DayFragment getFragment(int position) {
		return activeFragments.get(position);
	}
}
