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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.shalzz.attendance.CircularIndeterminate;
import com.shalzz.attendance.DataAPI;
import com.shalzz.attendance.DataAssembler;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.DividerItemDecoration;
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
import com.shalzz.attendance.wrapper.MyVolleyErrorHelper;

import java.util.List;

public class AttendanceListFragment extends Fragment implements ExpandableListAdapter.SubjectItemExpandedListener{

    /**
     * The {@link android.support.v4.widget.SwipeRefreshLayout} that detects swipe gestures and
     * triggers callbacks in the app.
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mFooter;
    private View mHeader;
    private TextView mLastRefreshView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private String mTag;
    private ExpandableListAdapter mAdapter;
    private int expandLimit;
    private CircularIndeterminate mProgress;
    private MyPreferencesManager prefs;
    private View mDropShadow;

    private float mExpandedItemTranslationZ;
    private int mFadeInDuration = 100;
    private int mFadeInStartDelay = 150;
    private int mFadeOutDuration = 100;
    private int mExpandCollapseDuration = 300;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mTag = getActivity().getLocalClassName();
        prefs = new MyPreferencesManager(mContext.getApplicationContext());
        mExpandedItemTranslationZ =
                getResources().getDimension(R.dimen.atten_view_expanded_elevation);
    }

    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        if(container==null)
            return null;

        setHasOptionsMenu(true);
        View mView = inflater.inflate(R.layout.attenview, container, false);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.atten_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_atten);
        mProgress = (CircularIndeterminate) mView.findViewById(R.id.circular_indet_atten);
        mDropShadow = MainActivity.getInstance().dropShadow;

        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefreshLayout.setProgressViewOffset(true, 1, 42);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mHeader = inflater.inflate(R.layout.list_header, mRecyclerView, false);
        mFooter = inflater.inflate(R.layout.list_footer, mRecyclerView, false);
        mLastRefreshView = (TextView) mHeader.findViewById(R.id.last_refreshed);
//        mListView.addHeaderView(mHeader);
//        mListView.addFooterView(mFooter);
//        mListView.setHeaderDividersEnabled(false);

        DatabaseHandler db = new DatabaseHandler(mContext);
        if(db.getRowCount()<=0) {
            String SAPID = getActivity().getIntent().getExtras().getString("SAPID");
            MySyncManager.addPeriodicSync(mContext,SAPID);
            DataAPI.getAttendance(mContext, successListener(), errorListener());
            mProgress.setVisibility(View.VISIBLE);
        }
        else {
            setAttendance();
            updateLastRefresh();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataAPI.getAttendance(mContext, successListener(), errorListener());
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ?
                    0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
                mDropShadow.setVisibility(mHeader.isShown() ? View.GONE : View.VISIBLE);
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    protected void updateLastRefresh() {
        mLastRefreshView.setText("Last refreshed " + prefs.getLastSyncTime() + " hours ago");
    }

    public void showcaseView() {
//        int firstElementPosition = 0;
//        firstElementPosition += mListView.getHeaderViewsCount();
        View firstElementView = mRecyclerView.getChildAt(0);

        new ShowcaseView.Builder(getActivity())
                .setStyle(R.style.AppBaseTheme)
                .setTarget(new ViewTarget(firstElementView))
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
            expandLimit = Integer.parseInt(sharedPref.getString("subjects_expanded_limit", "0"));

            List<Subject> subjects = db.getAllOrderedSubjects();

            mAdapter = new ExpandableListAdapter(mContext,subjects,this);
            mAdapter.setHasStableIds(true);
//            mAdapter.setLimit(expandLimit);
            mRecyclerView.setAdapter(mAdapter);

        }
    }

    private void updateHeaderNFooter() {

        TextView tvPercent = (TextView) mFooter.findViewById(R.id.tvTotalPercent);
        TextView tvClasses = (TextView) mFooter.findViewById(R.id.tvClass);
        ProgressBar pbPercent = (ProgressBar) mFooter.findViewById(R.id.pbTotalPercent);
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

        TextView tvName = (TextView) mHeader.findViewById(R.id.tvName);
        TextView tvSap = (TextView) mHeader.findViewById(R.id.tvSAP);
        TextView tvcourse = (TextView) mHeader.findViewById(R.id.tvCourse);

        ListHeader listheader = db.getListHeader();
        tvName.setText(listheader.getName());
        tvSap.setText(String.valueOf(listheader.getSAPId()));
        tvcourse.setText(listheader.getCourse());

        MainActivity.getInstance().updateDrawerHeader();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search subjects");

        MenuItemCompat.setOnActionExpandListener(searchItem , new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                DatabaseHandler db = new DatabaseHandler(mContext);
                List<Subject> subjects = db.getAllOrderedSubjects();
                mAdapter.setDataSet(subjects);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                Miscellaneous.closeKeyboard(mContext, searchView);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                DatabaseHandler db = new DatabaseHandler(mContext);
                List<Subject> subjects = db.getAllSubjectsLike(arg0);
                mAdapter.setDataSet(subjects);
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

            // We make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Response.Listener<String> successListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Stop the refreshing indicator
                mProgress.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    DataAssembler.parseStudentDetails(response, mContext);
                    DataAssembler.parseAttendance(response, mContext);
                    setAttendance();
                    prefs.setLastSyncTime();
                    updateLastRefresh();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Miscellaneous.showSnackBar(mContext,"An unexpected error occurred");
                }
            }
        };
    }

    private Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Stop the refreshing indicator
                mProgress.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                String msg = MyVolleyErrorHelper.getMessage(error, mContext);
                Miscellaneous.showSnackBar(mContext,msg);
                Log.e(mTag, msg);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemExpanded(final View view) {
        final int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final ExpandableListAdapter.ViewHolder viewHolder = (ExpandableListAdapter.ViewHolder) view.getTag();
        final RelativeLayout childView = viewHolder.childView;
        childView.measure(spec, spec);
        final int startingHeight = view.getHeight();
        final ViewTreeObserver observer = mRecyclerView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // We don't want to continue getting called for every draw.
                if (observer.isAlive()) {
                    observer.removeOnPreDrawListener(this);
                }
                // Calculate some values to help with the animation.
                final int endingHeight = view.getHeight();
                final int distance = Math.abs(endingHeight - startingHeight);
                final int baseHeight = Math.min(endingHeight, startingHeight);
                final boolean isExpanded = endingHeight > startingHeight;

                // Set the views back to the start state of the animation
                view.getLayoutParams().height = startingHeight;
                if (!isExpanded) {
                    viewHolder.childView.setVisibility(View.VISIBLE);
                }

                // Set up the fade effect for the action buttons.
                if (isExpanded) {
                    // Start the fade in after the expansion has partly completed, otherwise it
                    // will be mostly over before the expansion completes.
                    viewHolder.childView.setAlpha(0f);
                    viewHolder.childView.animate()
                            .alpha(1f)
                            .setStartDelay(mFadeInStartDelay)
                            .setDuration(mFadeInDuration)
                            .start();
                } else {
                    viewHolder.childView.setAlpha(1f);
                    viewHolder.childView.animate()
                            .alpha(0f)
                            .setDuration(mFadeOutDuration)
                            .start();
                }
                view.requestLayout();

                // Set up the animator to animate the expansion and shadow depth.
                ValueAnimator animator = isExpanded ? ValueAnimator.ofFloat(0f, 1f)
                        : ValueAnimator.ofFloat(1f, 0f);

                // Figure out how much scrolling is needed to make the view fully visible.
                final Rect localVisibleRect = new Rect();
                view.getLocalVisibleRect(localVisibleRect);
                // TODO: fix scrolling
                view.measure(spec,spec);
                final int scrollingNeeded = localVisibleRect.top > 0 ? -localVisibleRect.top
                        : view.getMeasuredHeight() - localVisibleRect.height();
                final RecyclerView recyclerView = mRecyclerView;
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    private int mCurrentScroll = 0;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        Float value = (Float) animator.getAnimatedValue();

                        // For each value from 0 to 1, animate the various parts of the layout.
                        view.getLayoutParams().height = (int) (value * distance + baseHeight);
                        float z = mExpandedItemTranslationZ * value;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            view.setTranslationZ(z);
                        }
                        view.requestLayout();

                        if (isExpanded) {
                            if (recyclerView != null) {
                                int scrollBy = (int) (value * scrollingNeeded) - mCurrentScroll;
                                recyclerView.smoothScrollBy(0, scrollBy);
                                mCurrentScroll += scrollBy;
                            }
                        }
                    }
                });

                // Set everything to their final values when the animation's done.
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                        if (!isExpanded) {
                            viewHolder.childView.setVisibility(View.GONE);
                        } else {
                            // This seems like it should be unnecessary, but without this, after
                            // navigating out of the activity and then back, the action view alpha
                            // is defaulting to the value (0) at the start of the expand animation.
                            viewHolder.childView.setAlpha(1);
                        }
                    }
                });

                animator.setDuration(mExpandCollapseDuration);
                animator.start();

                // Return false so this draw does not occur to prevent the final frame from
                // being drawn for the single frame before the animations start.
                return false;
            }
        });
    }

    @Override
    public View getViewForCallId(long callId) {
        int firstPosition = mLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = mLayoutManager.findLastVisibleItemPosition();

        for (int position = firstPosition; position <= lastPosition - firstPosition; position++) {
            View view = mRecyclerView.getChildAt(position);

            if (view != null) {
                final ExpandableListAdapter.ViewHolder viewHolder = (ExpandableListAdapter.ViewHolder) view.getTag();
                if (viewHolder != null && viewHolder.position == callId) {
                    return view;
                }
            }
        }

        return null;
    }
}
