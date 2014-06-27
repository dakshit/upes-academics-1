package com.shalzz.attendance.adapter;

import java.util.List;

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
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = (LayoutInflater)
				mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		convertView = mInflater.inflate(R.layout.day_list_item, null);
		TextView tvSubjectName = (TextView) convertView.findViewById(R.id.tvSubjectName);
		TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
		Period period = periods.get(position);
		String subject= period.getName();
		String time = period.getTime();
		tvSubjectName.setText(subject);
		if(!subject.equals(""))
			tvTime.setText(time.substring(1, time.length()-1));
		return convertView;
	}
}
