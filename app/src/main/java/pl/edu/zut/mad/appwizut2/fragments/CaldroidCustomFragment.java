package pl.edu.zut.mad.appwizut2.fragments;


import android.os.Bundle;
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
import pl.edu.zut.mad.appwizut2.network.ScheduleCommonLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

public class CaldroidCustomFragment extends CaldroidFragment implements TabLayout.OnTabSelectedListener {

    private Date mSelectedDate;
    private String mDateString;

    private EventsLoader mEventsDataLoader;
    private ScheduleCommonLoader mScheduleLoader;
    private final Map<String, Integer> mEventCountsOnDays = new HashMap<>();

    private EventsFragment mEventsFragment;
    private TimetableDayFragment mTimetableDayFragment;
    private ViewPager mViewPager;
    private Timetable mTimetable;

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
            mDateString = savedInstanceState.getString(Constants.CURRENT_CLICKED_DATE);
            try {
                mSelectedDate = Constants.FOR_EVENTS_FORMATTER.parse(mDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            setBackgroundResourceForDate(R.color.calendar_selected, mSelectedDate);
        } else {
            try {
                mSelectedDate = Constants.FOR_EVENTS_FORMATTER.parse(
                        Constants.FOR_EVENTS_FORMATTER.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mDateString = Constants.FOR_EVENTS_FORMATTER.format(mSelectedDate);
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

        mScheduleLoader = DataLoadingManager.getInstance(getActivity()).getLoader(ScheduleCommonLoader.class);
        mScheduleLoader.registerAndLoad(mScheduleListener);

        mViewPager = (ViewPager) wrapper.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) wrapper.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lessons));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.events));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setOnTabSelectedListener(this);

        PagerAdapter pagerAdapter = new ScheduleAndEventsAdapter(getChildFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        setCaldroidListener(caldroidListener);

        return wrapper;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.CURRENT_CLICKED_DATE, mDateString);
    }

    @Override
    protected void retrieveInitialArgs() {
        startDayOfWeek = CaldroidFragment.MONDAY;
        setThemeResource(R.style.CaldroidCustomized);
        super.retrieveInitialArgs();
    }

    public CaldroidListener caldroidListener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            changeSelectedDate(date);

            if (mTimetableDayFragment != null) {
                mTimetableDayFragment.setDate(date);
            }
            if (mEventsFragment != null) {
                mEventsFragment.setDate(date);
            }
        }
    };

    private void changeSelectedDate(Date date) {
        clearBackgroundResourceForDate(mSelectedDate);
        /**
         * Use previous selected date to color background
         * depending on day with classes or no
         */
        setBackgroundForClassesDay(mSelectedDate);
        setBackgroundResourceForDate(R.color.calendar_selected, date);
        refreshView();

        mDateString = Constants.FOR_EVENTS_FORMATTER.format(date);
        mSelectedDate = date;
    }

    private void setBackgroundForClassesDay(Date date) {
        if (date == null || mTimetable == null) {
            return;
        }

        Timetable.Day[] days = mTimetable.getDays();
        for (Timetable.Day day : days) {
            if (date.equals(day.getDate().getTime())) {
                setBackgroundResourceForDate(R.color.colorPrimary, date);
                return;
            }
        }
    }

    private final BaseDataLoader.DataLoadedListener<List<ListItemContainer>> mEventsDataListener = new BaseDataLoader.DataLoadedListener<List<ListItemContainer>>() {
        @Override
        public void onDataLoaded(List<ListItemContainer> data) {

            // Count events on days
            mEventCountsOnDays.clear();
            if (data != null) {
                for (ListItemContainer entry : data) {
                    String dateString = entry.getDate();
                    Integer countSoFar = mEventCountsOnDays.get(dateString);
                    if (countSoFar == null) {
                        mEventCountsOnDays.put(dateString, 1);
                    } else {
                        mEventCountsOnDays.put(dateString, countSoFar + 1);
                    }
                }
            }
            HashMap<String, Object> extraData = getExtraData();
            extraData.put("EVENTS", mEventCountsOnDays);

            refreshView();
        }
    };

    private final BaseDataLoader.DataLoadedListener<Timetable> mScheduleListener = new BaseDataLoader.DataLoadedListener<Timetable>() {
        @Override
        public void onDataLoaded(Timetable timetable) {
            mTimetable = timetable;
            if (timetable == null) {
                return;
            }

            Timetable.Day[] days = timetable.getDays();
            for (Timetable.Day day : days) {
                if (!mSelectedDate.equals(day.getDate().getTime())) {
                    setBackgroundResourceForDate(R.color.colorPrimary, day.getDate().getTime());
                }
            }
            refreshView();
        }
    };

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

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(mEventsDataListener);
        mEventsDataLoader = null;
        mScheduleLoader.unregister(mScheduleListener);
        mScheduleLoader = null;
        super.onDestroyView();
    }

    private class ScheduleAndEventsAdapter extends FragmentPagerAdapter {

        public ScheduleAndEventsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    mTimetableDayFragment = TimetableDayFragment.newInstance(mSelectedDate);
                    return mTimetableDayFragment;
                case 1:
                    mEventsFragment = EventsFragment.newInstance(mSelectedDate);
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
