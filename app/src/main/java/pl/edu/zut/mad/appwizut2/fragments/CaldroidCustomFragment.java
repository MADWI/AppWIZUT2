package pl.edu.zut.mad.appwizut2.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class CaldroidCustomFragment extends CaldroidFragment {

    private final static String CURRENT_MONTH = "current_month";
    private final static String CURRENT_YEAR = "current_year";
    private final static String CURRENT_CLICKED_DATE = "clicked_date";

    private Date mDate;
    private String mDateString;
    private int mMonth = 0;
    private int mYear = 0;

    private EventsLoader mEventsDataLoader;
    private final Map<String, Integer> mEventCountsOnDays = new HashMap<>();

    private EventsFragment mEventsFragment;
    private TimetableDayFragment mTimetableDayFragment;
    private BaseDataLoader<Timetable, ?> mTimeTableLoader;
    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private Timetable mTimeTable;

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // michalbednarski: Hacky workaround for Caldroid's saved state mishandling
        if (savedInstanceState != null) {
            // Delete info for child fragment manager
            savedInstanceState.remove("android:support:fragments");
            mMonth = savedInstanceState.getInt(CURRENT_MONTH);
            mYear = savedInstanceState.getInt(CURRENT_YEAR);
        }
        // Call super
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: remove initUI method
        // (It shouldn't belong to onCreateView, this forces you to know day parity synchronously)
        initUI();
        if (mMonth != 0 && mYear != 0) {
            month = mMonth;
            year = mYear;
        }

        mDate = new Date(System.currentTimeMillis());

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

        mPagerAdapter = new ScheduleAndEventsAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });

        mTimeTableLoader = loadingManager.getLoader(ScheduleEdzLoader.class);
        mTimeTableLoader.registerAndLoad(mTimeTableListener);

        return wrapper;
    }

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(mEventsDataListener);
        mEventsDataLoader = null;
        mTimeTableLoader.unregister(mTimeTableListener);
        mTimeTableLoader = null;
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){
            String selectedDate = savedInstanceState.getString(CURRENT_CLICKED_DATE);
            if(selectedDate != null ) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                //clickedDate.setText(selectedDate);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (listener != null) {
            outState.putString(CURRENT_CLICKED_DATE, mDateString);
        }
        outState.putInt(CURRENT_MONTH, getMonth());
        outState.putInt(CURRENT_YEAR, getYear());
    }

    private void initUI() {

        // Set event counts in views
        HashMap<String, Object> extraData = getExtraData();
        extraData.put("EVENTS", mEventCountsOnDays);

        setCaldroidListener(listener);
        refreshView();
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
            setBackgroundResourceForDate(R.color.calendar_default, mDate);
            setBackgroundResourceForDate(R.color.backgroundGray, new Date(System.currentTimeMillis()));
            mDate = date;
            mDateString = Constants.FOR_EVENTS_FORMATTER.format(date);

            if (mTimeTable != null) {
                mTimetableDayFragment.onScheduleAvailable(mTimeTable, date);
            }
            mPagerAdapter = new ScheduleAndEventsAdapter(getChildFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
            mPagerAdapter.notifyDataSetChanged();

            mEventsFragment.updateEventsInDay(mDate);
            setBackgroundResourceForDate(R.color.even, mDate);
            refreshView();
        }
    };

    private final BaseDataLoader.DataLoadedListener<Timetable> mTimeTableListener = new BaseDataLoader.DataLoadedListener<Timetable>() {
        @Override
        public void onDataLoaded(Timetable timetable) {
            mTimeTable = timetable;
            if (mTimetableDayFragment != null && timetable != null) {
                mTimetableDayFragment.onScheduleAvailable(timetable, mDate);
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
            initUI();
        }
    };

    private class ScheduleAndEventsAdapter extends FragmentPagerAdapter {

        public ScheduleAndEventsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    mTimetableDayFragment = TimetableDayFragment.newInstance(mDate.getDay());
                    return mTimetableDayFragment;
                case 1:
                    mEventsFragment = new EventsFragment(mDate);
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
