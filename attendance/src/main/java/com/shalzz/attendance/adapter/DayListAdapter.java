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
import com.shalzz.attendance.model.Day;
import com.shalzz.attendance.model.Period;

import java.text.ParseException;
import java.util.List;

public class DayListAdapter extends BaseAdapter{
	private Context mContext;
	private List<Period> periods;

	public DayListAdapter(Context context,Day day){
		mContext = context;
		periods = day.getAllPeriods();
	}

	@Override
	public int getCount() {
		return periods.size();
	}

	@Override
	public Period getItem(int position) {
		return periods.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater)
				mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // Check if we can recycle the view
        if (convertView == null || (Integer) convertView.getTag() != R.layout.day_list_item+position)
        {
            convertView = mInflater.inflate(R.layout.day_list_item, parent, false);
            // view = parent.findViewById(R.id.activity_expandablelistitem_content);
            // Set the tag to make sure you can recycle it when you get it as a convert view
            convertView.setTag(R.layout.day_list_item + position);
        }

		TextView tvSubjectName = (TextView) convertView.findViewById(R.id.tvSubjectName);
		TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        TextView tvTeacher = (TextView) convertView.findViewById(R.id.tvTeacher);
        TextView tvRoom = (TextView) convertView.findViewById(R.id.tvRoom);
		Period period = periods.get(position);

		tvSubjectName.setText(period.getSubjectName());
        tvRoom.setText(period.getRoom());
        tvTeacher.setText(period.getTeacher());
        try {
            tvTime.setText(period.getTimein12hr());
        } catch (ParseException e) {
            tvTime.setText(period.getTime());
            e.printStackTrace();
        }
		return convertView;
	}
}
