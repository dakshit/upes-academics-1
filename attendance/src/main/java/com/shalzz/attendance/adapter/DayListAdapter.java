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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;
import com.shalzz.attendance.R;
import com.shalzz.attendance.model.Day;
import com.shalzz.attendance.model.Period;

import java.text.ParseException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.ViewHolder>{
    private List<Period> periods;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.tvSubjectName) TextView tvSubjectName;
        @InjectView(R.id.tvTime) TextView tvTime;
        @InjectView(R.id.tvTeacher) TextView tvTeacher;
        @InjectView(R.id.tvRoom) TextView tvRoom;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }

    public DayListAdapter(Day day){
        if (day == null) {
            throw new IllegalArgumentException(
                    "Data set must not be null");
        }
        periods = day.getAllPeriods();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_list_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Period period = periods.get(position);
        if(!period.getBatch().equals("NULL"))
            holder.tvSubjectName.setText(period.getSubjectName()+" - "+period.getBatch());
        else
            holder.tvSubjectName.setText(period.getSubjectName());
        holder.tvRoom.setText(period.getRoom());
        holder.tvTeacher.setText(period.getTeacher());
        try {
            holder.tvTime.setText(period.getTimein12hr());
        } catch (ParseException e) {
            Bugsnag.notify(e, Severity.WARNING);
            holder.tvTime.setText(period.getTime());
            e.printStackTrace();
        }

    }

    public void setDataSet(Day day) {
        periods = day.getAllPeriods();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return periods.size();
    }

}
