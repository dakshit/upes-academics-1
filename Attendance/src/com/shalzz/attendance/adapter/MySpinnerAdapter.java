package com.shalzz.attendance.adapter;

import java.util.Date;

import com.shalzz.attendance.R;
import com.shalzz.attendance.wrapper.DateHelper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MySpinnerAdapter extends BaseAdapter {

	private Context mContext;
	private String[] dropDownList;
	private final LayoutInflater mInflater;
	private Date mDate = DateHelper.getToDay();

	public MySpinnerAdapter(Context context){
		// initialise
		mContext = context;
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
