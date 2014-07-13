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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shalzz.attendance.R;
import com.shalzz.attendance.wrapper.DateHelper;

import java.util.Date;

public class MySpinnerAdapter extends BaseAdapter {

	private Context mContext;
	private String[] dropDownList;
	private final LayoutInflater mInflater;
	private Date mDate;

	public MySpinnerAdapter(Context context){
		// initialise
		mContext = context;
		mDate = new Date();
		dropDownList = mContext.getResources().getStringArray(R.array.action_list);
		mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return dropDownList.length;
	}

	@Override
	public Object getItem(int position) {
		if (position < dropDownList.length) {
			return dropDownList[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v;

		// Check if can recycle the view
		if (convertView == null || ((Integer) convertView.getTag()).intValue()!= R.layout.spinner_header) 
		{
			v = mInflater.inflate(R.layout.spinner_header, parent, false);
			// Set the tag to make sure you can recycle it when you get it as a convert view
			v.setTag(Integer.valueOf(R.layout.spinner_header));
		}
		else {
			v = convertView;
		}

        TextView weekDay = (TextView) v.findViewById(R.id.spinner_list_title);
        TextView date = (TextView) v.findViewById(R.id.spinner_list_subtitle);
        
        weekDay.setText(DateHelper.getProperWeekday(mDate));
        date.setText(DateHelper.formatToProperFormat(mDate));

		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater)
				mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		convertView = mInflater.inflate(R.layout.spinner_item, null);
		TextView textview = (TextView) convertView.findViewById(R.id.spinner_item);
		textview.setText(dropDownList[position]);
		return convertView;
	}
	

    public void setDate(Date date) {
    	if(date!=null)
    		mDate = date;
        notifyDataSetChanged();
    }
}
