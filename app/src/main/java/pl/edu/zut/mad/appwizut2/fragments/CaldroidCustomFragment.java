package pl.edu.zut.mad.appwizut2.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.zut.mad.appwizut2.CaldroidCustomAdapter;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.EventsLoader;
import pl.edu.zut.mad.appwizut2.network.ScheduleEdzLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

public class CaldroidCustomFragment extends CaldroidFragment implements TabLayout.OnTabSelectedListener{

    private Date mSelectDate;
    private String mDateString;

    private EventsLoader mEventsDataLoader;
    private final Map<String, Integer> mEventCountsOnDays = new HashMap<>();

    private EventsFragment mEventsFragment;
    private TimetableDayFragment mTimetableDayFragment;
    private BaseDataLoader<Timetable, ?> mTimeTableLoader;
    private ViewPager mViewPager;
    private Timetable mTimeTable;


    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        // michalbednarski: Hacky workaround for Caldroid's saved state mishandling
        if (savedInstanceState != null) {
            // Delete info for child fragment manager
            savedInstanceState.remove("android:support:fragments");
            mDateString = savedInstanceState.getString(Constants.CURRENT_CLICKED_DATE);
            try {
                mSelectDate = Constants.FOR_EVENTS_FORMATTER.parse(mDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mSelectDate = Constants.FOR_EVENTS_FORMATTER.parse(
                        Constants.FOR_EVENTS_FORMATTER.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mDateString = Constants.FOR_EVENTS_FORMATTER.format(mSelectDate);
        }
        // Call super
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get calendar view from superclass
        ViewGroup calendarView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        // Disable state saving on superclass'es view
        // This is workaround for Caldroid's improper handling of state saving
        for (int i = 0; i < calendarView.getChildCount(); i++) {
            calendarView.getChildAt(i).setSaveFromParentEnabled(false);
        }

        //setting toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_calendar);

        // Wrap calendarView into out fragment
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.calendar_layout, container, false);

        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        // Initialize data load
        DataLoadingManager loadingManager = DataLoadingManager.getInstance(getContext());
        mEventsDataLoader = loadingManager.getLoader(EventsLoader.class);
        mEventsDataLoader.registerAndLoad(mEventsDataListener);

        mViewPager = (ViewPager) wrapper.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) wrapper.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lessons));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.events));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setOnTabSelectedListener(this);

        PagerAdapter pagerAdapter = new ScheduleAndEventsAdapter(getChildFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mTimeTableLoader = loadingManager.getLoader(ScheduleEdzLoader.class);
        mTimeTableLoader.registerAndLoad(mTimeTableListener);

        setCaldroidListener(listener);

        changeSelectDate(mSelectDate);
        mTimeTableLoader.requestRefresh();
        return wrapper;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString(Constants.CURRENT_CLICKED_DATE, mDateString);
    }

    @Override
    protected void retrieveInitialArgs() {
        setThemeResource(R.style.CaldroidCustomized);
        super.retrieveInitialArgs();
    }

    // Setup listener
    public CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            changeSelectDate(date);

            if (mTimeTable != null && mTimetableDayFragment != null) {
                mTimetableDayFragment.onScheduleAvailable(mTimeTable, date);
            }

            if (mEventsFragment != null) {
                mEventsFragment.updateEventsInDay(mDateString);
            }
        }
    };

    private void changeSelectDate(Date date) {
        setBackgroundResourceForDate(R.color.calendar_default, mSelectDate);
        setBackgroundResourceForDate(R.color.backgroundGray, new Date(System.currentTimeMillis()));
        mSelectDate = date;
        mDateString = Constants.FOR_EVENTS_FORMATTER.format(date);

        setBackgroundResourceForDate(R.color.even, mSelectDate);
        refreshView();
    }

    private final BaseDataLoader.DataLoadedListener<Timetable> mTimeTableListener = new BaseDataLoader.DataLoadedListener<Timetable>() {
        @Override
        public void onDataLoaded(Timetable timetable) {
            mTimeTable = timetable;
            if (mTimetableDayFragment != null && timetable != null) {
                mTimetableDayFragment.onScheduleAvailable(timetable, mSelectDate);
            }
        }
    };

    private final BaseDataLoader.DataLoadedListener<List<ListItemContainer>> mEventsDataListener = new BaseDataLoader.DataLoadedListener<List<ListItemContainer>>() {
        @Override
        public void onDataLoaded(List<ListItemContainer> data) {

            // Count events on days
            mEventCountsOnDays.clear();
            if (data != null) {
                for (ListItemContainer entry : data) {
                    String date = entry.getDate();
                    Integer countSoFar = mEventCountsOnDays.get(date);
                    if (countSoFar == null) {
                        mEventCountsOnDays.put(date, 1);
                    } else {
                        mEventCountsOnDays.put(date, countSoFar + 1);
                    }
                }
            }
            HashMap<String, Object> extraData = getExtraData();
            extraData.put("EVENTS", mEventCountsOnDays);

            refreshView();
        }
    };

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(mEventsDataListener);
        mEventsDataLoader = null;
        mTimeTableLoader.unregister(mTimeTableListener);
        mTimeTableLoader = null;
        super.onDestroyView();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private class ScheduleAndEventsAdapter extends FragmentPagerAdapter {

        public ScheduleAndEventsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    mTimetableDayFragment = TimetableDayFragment.newInstance(mSelectDate.getDay());
                    return mTimetableDayFragment;
                case 1:
                    mEventsFragment = EventsFragment.newInstance(mDateString);
                    return mEventsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
