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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shalzz.attendance.R;
import com.shalzz.attendance.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Subject> mSubjects;
    private final List<Long> mExpandedIds = new ArrayList<>();
    private float mExpandedTranslationZ;
    private int mLimit = -1;

    /** Constant used to indicate no row is expanded. */
    private static final long NONE_EXPANDED = -1;

    /**
     * Tracks the call log row which was previously expanded.  Used so that the closure of a
     * previously expanded call log entry can be animated on rebind.
     */
    private long mPreviouslyExpanded = NONE_EXPANDED;


    private SubjectItemExpandedListener mSubjectItemExpandedListener;

    /** Interface used to inform a parent UI element that a list item has been expanded. */
    public interface SubjectItemExpandedListener {
        /**
         * @param view The {@link View} that represents the item that was clicked
         *         on.
         */
        public void onItemExpanded(View view);

        /**
         * Retrieves the call log view for the specified call Id.  If the view is not currently
         * visible, returns null.
         *
         * @param callId The call Id.
         * @return The call log view.
         */
        public View getViewForCallId(long callId);
    }

    public ExpandableListAdapter(Context context,List<Subject> subjects,
                                 SubjectItemExpandedListener subjectItemExpandedListener) {
        if (subjects == null) {
            throw new IllegalArgumentException(
                    "Data set must not be null");
        }
        mContext = context;
        mSubjects = subjects;
        mSubjectItemExpandedListener = subjectItemExpandedListener;
        mExpandedTranslationZ = mContext.getResources().getDimension(R.dimen.atten_view_expanded_elevation);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int position = -1;

        public TextView subject;
        public TextView percentage;
        public TextView classes;
        public ProgressBar percent;

        //child views
        public RelativeLayout childView;
        public TextView tvAbsent;
        public TextView tvProjected;
        public TextView tvReach;
        public TextView tvClass;
        public ImageView ivAlert;

        public ViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.tvSubj);
            percentage = (TextView) itemView.findViewById(R.id.tvPercent);
            classes = (TextView) itemView.findViewById(R.id.tvClasses);
            percent = (ProgressBar) itemView.findViewById(R.id.pbPercent);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder vh;
        if(v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
            vh = new ViewHolder(v);
        }
        else
            vh = (ViewHolder) convertView.getTag();

        onBindViewHolder(vh,position);

        return v;
    }

    /**
     * The onClickListener used to expand or collapse the action buttons section for a call log
     * entry.
     */
    private final View.OnClickListener mExpandCollapseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handleRowExpanded(v, true /* animate */, false /* forceExpand */);
        }
    };

    // Replace the contents of a view (invoked by the layout manager)
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.position = position;
        holder.itemView.setTag(holder);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Float percent = mSubjects.get(position).getPercentage();
        holder.subject.setText(mSubjects.get(position).getName());
        holder.percentage.setText(mSubjects.get(position).getPercentage().toString()+"%");
        holder.classes.setText(mSubjects.get(position).getClassesAttended().intValue()+"/"+mSubjects.get(position).getClassesHeld().intValue());
        holder.percent.setProgress(percent.intValue());
        Drawable d = holder.percent.getProgressDrawable();
        d.setLevel(percent.intValue()*100);

        // In the call log, expand/collapse an actions section for the call log entry when
        // the primary view is tapped.
        holder.itemView.setOnClickListener(this.mExpandCollapseListener);

        // Restore expansion state of the row on rebind.  Inflate the actions ViewStub if required,
        // and set its visibility state accordingly.
        expandOrCollapseChildView(holder.itemView, isExpanded(position));

    }

    /**
     * Configures the action buttons in the expandable actions ViewStub.  The ViewStub is not
     * inflated during initial binding, so click handlers, tags and accessibility text must be set
     * here, if necessary.
     *
     * @param view The call log list item view.
     */
    private void inflateChildView(final View view) {
        final ViewHolder views = (ViewHolder) view.getTag();

        ViewStub stub = (ViewStub) view.findViewById(R.id.subject_details_stub);
        if (stub != null) {
            views.childView = (RelativeLayout) stub.inflate();
        }
        else
            views.childView = (RelativeLayout) views.itemView.findViewById(R.id.subTree);

        // child view
        View childView = views.childView;
        views.tvAbsent = (TextView) childView.findViewById(R.id.tvAbsent);
        views.tvProjected = (TextView) childView.findViewById(R.id.tvProjected);
        views.tvReach = (TextView) childView.findViewById(R.id.tvReach);
        views.tvClass = (TextView) childView.findViewById(R.id.tvClass);
        views.ivAlert = (ImageView) childView.findViewById(R.id.imageView1);

        bindChildView(views,views.position);
    }

    /**
     * Toggles the expansion state tracked for the call log row identified by rowId and returns
     * the new expansion state.  Assumes that only a single call log row will be expanded at any
     * one point and tracks the current and previous expanded item.
     *
     * @param rowId The row Id associated with the call log row to expand/collapse.
     * @return True where the row is now expanded, false otherwise.
     */
    private boolean toggleExpansion(long rowId) {
        if (isExpanded(rowId)) {
            // Collapsing currently expanded row.
            mExpandedIds.remove(rowId);

            return false;
        } else {
            // Expanding a row.
            mExpandedIds.add(rowId);

            // Collapse a view if limit reached
            boolean shouldCollapseOther =  mLimit > 0 && mExpandedIds.size() > mLimit;
            if(shouldCollapseOther) {
                mPreviouslyExpanded = mExpandedIds.get(0);
                mExpandedIds.remove(mPreviouslyExpanded);
            }
            else
                mPreviouslyExpanded = NONE_EXPANDED;
            return true;
        }
    }

    /**
     * Determines if a call log row with the given Id is expanded.
     * @param rowId The row Id of the call.
     * @return True if the row is expanded.
     */
    private boolean isExpanded(long rowId) {
        return mExpandedIds.contains(rowId);
    }

    /**
     * Expands or collapses the view containing the CALLBACK, VOICEMAIL and DETAILS action buttons.
     *
     * @param view The call log entry parent view.
     * @param isExpanded The new expansion state of the view.
     */
    private void expandOrCollapseChildView(View view, boolean isExpanded) {
        final ViewHolder views = (ViewHolder) view.getTag();

        if (isExpanded) {
            // Inflate the view stub if necessary.
            inflateChildView(view);

            views.childView.setVisibility(View.VISIBLE);
            views.childView.setAlpha(1.0f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setTranslationZ(mExpandedTranslationZ);
            }
        } else {

            // When recycling a view, it is possible the actionsView ViewStub was previously
            // inflated so we should hide it in this case.
            if (views.childView != null) {
                views.childView.setVisibility(View.GONE);
            }
            // TODO: implement elevation pre L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setTranslationZ(0);
            }
        }
    }

    /**
     * Manages the state changes for the UI interaction where a call log row is expanded.
     *
     * @param view The view that was tapped
     * @param animate Whether or not to animate the expansion/collapse
     * @param forceExpand Whether or not to force the call log row into an expanded state regardless
     *        of its previous state
     */
    private void handleRowExpanded(View view, boolean animate, boolean forceExpand) {
        final ViewHolder views = (ViewHolder) view.getTag();

        if (forceExpand && isExpanded(views.position)) {
            return;
        }

        // Hide or show the actions view.
        boolean expanded = toggleExpansion(views.position);

        // Trigger loading of the viewstub and visual expand or collapse.
        expandOrCollapseChildView(view, expanded);

        // Animate the expansion or collapse.
        if (mSubjectItemExpandedListener != null) {
            if (animate) {
                mSubjectItemExpandedListener.onItemExpanded(view);
            }

            // Animate the collapse of the previous item if it is still visible on screen.
            if (mPreviouslyExpanded != NONE_EXPANDED) {
                View previousItem = mSubjectItemExpandedListener.getViewForCallId(
                        mPreviouslyExpanded);

                if (previousItem != null) {
                    expandOrCollapseChildView(previousItem, false);
                    if (animate) {
                        mSubjectItemExpandedListener.onItemExpanded(previousItem);
                    }
                }
                mPreviouslyExpanded = NONE_EXPANDED;
            }
        }
    }

    public void bindChildView(ViewHolder holder, int position) {

        TextView tvAbsent = holder.tvAbsent;
        TextView tvProjected = holder.tvProjected;
        TextView tvReach = holder.tvReach;
        TextView tvClass = holder.tvClass;
        ImageView ivAlert = holder.ivAlert;

        int held = mSubjects.get(position).getClassesHeld().intValue();
        int attend = mSubjects.get(position).getClassesAttended().intValue();
        int percent = mSubjects.get(position).getPercentage().intValue();

        if(held==1)
            tvClass.setText("You have attended "+attend+ " out of "+held+ " class");
        else
            tvClass.setText("You have attended "+attend+ " out of "+held+ " classes");
        tvProjected.setText(mSubjects.get(position).getProjectedPercentage());
        tvAbsent.setText("Days Absent: "+mSubjects.get(position).getAbsentDates());

        if (percent<67 && held!=0) {
            int x = (2*held) - (3*attend);
            switch(x)
            {
                case 0:
                    tvReach.setVisibility(View.GONE);
                    ivAlert.setVisibility(View.GONE);
                    break;
                case 1:
                    tvReach.setText("Attend 1 more class to reach 67%");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.VISIBLE);
                    break;
                default:
                    tvReach.setText("Attend "+x+" more classes to reach 67%");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.VISIBLE);
                    break;
            }
        }
        else if(percent<75 && held!=0) {
            int x = (3*held) - (4*attend);
            switch(x)
            {
                case 0:
                    tvReach.setVisibility(View.GONE);
                    ivAlert.setVisibility(View.GONE);
                    break;
                case 1:
                    tvReach.setText("Attend 1 more class to reach 75%");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.VISIBLE);
                    break;
                default:
                    tvReach.setText("Attend "+x+" more classes to reach 75%");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            int x = ((4*attend)/3)-held;
            switch(x)
            {
                case 0:
                    tvReach.setVisibility(View.GONE);
                    ivAlert.setVisibility(View.GONE);
                    break;
                case 1:
                    tvReach.setText("You can safely miss 1 class");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.GONE);
                    break;
                default:
                    tvReach.setText("You can safely miss "+x+" classes");
                    tvReach.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
                    tvReach.setVisibility(View.VISIBLE);
                    ivAlert.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public int getCount() {
        return mSubjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mSubjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }


    /**
     * Set the maximum number of items allowed to be expanded. When the
     * (limit+1)th item is expanded, the first expanded item will collapse.
     *
     * @param limit the maximum number of items allowed to be expanded. Use <= 0
     * for no limit.
     */
    public void setLimit(final int limit) {
        mLimit = limit;
        mExpandedIds.clear();
        notifyDataSetChanged();
    }

    public void setDataSet(List<Subject> subjects) {
        mSubjects = subjects;
        notifyDataSetChanged();
    }
}