package pl.edu.zut.mad.appwizut2.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.zut.mad.appwizut2.CaldroidCustomAdapter;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.EventsLoader;
import pl.edu.zut.mad.appwizut2.network.ScheduleEdzLoader;
import pl.edu.zut.mad.appwizut2.network.ScheduleLoader;
import pl.edu.zut.mad.appwizut2.network.WeekParityLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

public class CaldroidCustomFragment extends CaldroidFragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<Timetable>{

    private final static String CURRENT_MONTH = "current_month";
    private final static String CURRENT_YEAR = "current_year";
    private final static String CURRENT_CLICKED_DATE = "clicked_date";
    private List<DayParity> parityList;
    private List<ListItemContainer> eventsData = new ArrayList<>();
    private List<ListItemContainer> eventsInDay = new ArrayList<>();

    private TextView clickedDate;
    private RecyclerView itemListView;

    String strDate="";
    private int mMonth = 0;
    private int mYear = 0;

    private WeekParityLoader mParityLoader;
    private EventsLoader mEventsDataLoader;
    private final Map<String, Integer> mEventCountsOnDays = new HashMap<>();

    private TimetableDayFragment mTimetableDayFragment;
    private BaseDataLoader<Timetable, ?> mTimeTableLoader;
    private SharedPreferences mPreferences;
    private PagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

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
        if(mMonth != 0 && mYear != 0){
            month = mMonth;
            year = mYear;
        }

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
        clickedDate = (TextView) wrapper.findViewById(R.id.dateTextView);

        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        // Initialize data load
        DataLoadingManager loadingManager = DataLoadingManager.getInstance(getContext());
        mParityLoader = loadingManager.getLoader(WeekParityLoader.class);
        mParityLoader.registerAndLoad(mParityListener);
        mEventsDataLoader = loadingManager.getLoader(EventsLoader.class);
        mEventsDataLoader.registerAndLoad(mEventsDataListener);

        mViewPager = (ViewPager) wrapper.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) wrapper.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Zajęcia"));
        tabLayout.addTab(tabLayout.newTab().setText("Wydarzenia"));
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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mTimeTableLoader = chooseLoaderFromSettings(mPreferences);
        mTimeTableLoader.registerAndLoad(this);

        return wrapper;
    }

    private BaseDataLoader<Timetable, ?> chooseLoaderFromSettings(SharedPreferences preferences) {
        DataLoadingManager dataLoadingManager = DataLoadingManager.getInstance(getActivity());

        if ("edziekanat".equals(preferences.getString(Constants.PREF_TIMETABLE_DATA_SOURCE, ""))) {
            return dataLoadingManager.getLoader(ScheduleEdzLoader.class);
        } else {
            return dataLoadingManager.getLoader(ScheduleLoader.class);
        }
    }

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(mEventsDataListener);
        mParityLoader.unregister(mParityListener);
        mEventsDataLoader = null;
        mParityLoader = null;
        super.onDestroyView();
    }

    private void initializeAdapter(){
        ListItemAdapter listItemAdapter = new ListItemAdapter(eventsInDay);
        itemListView.setAdapter(listItemAdapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){
            String selectedDate = savedInstanceState.getString(CURRENT_CLICKED_DATE);
            if(selectedDate != null ){
                clickedDate.setText(selectedDate);
            }
            updateEventsInDay();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(listener != null) {
            outState.putString(CURRENT_CLICKED_DATE, clickedDate.getText().toString());
        }
        outState.putInt(CURRENT_MONTH, getMonth());
        outState.putInt(CURRENT_YEAR, getYear());
    }


    private void initUI() {

        // Set cells background according to parity
        if (parityList != null) {
            for (DayParity dayParities : parityList) {
                DayParity.Parity parity = dayParities.getParity();
                if (parity == DayParity.Parity.EVEN) {
                    setBackgroundResourceForDate(R.color.even, dayParities.getDate());
                } else {
                    setBackgroundResourceForDate(R.color.uneven, dayParities.getDate());
                }
            }
        }

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

            strDate = Constants.FOR_EVENTS_FORMATTER.format(date);
         //   clickedDate.setText("Wydarzenia " + Constants.REVERSED_FORMATTER.format(date));
            clickedDate.setText("Zajęcia " + Constants.REVERSED_FORMATTER.format(date));

            mPagerAdapter = new ScheduleAndEventsAdapter(getChildFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
        }

    };

    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str) {
        Date dateStr = null;
        try {
            dateStr = Constants.FORMATTER.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    @Override
    public void onRefresh() {
        mParityLoader.requestRefresh();
        mEventsDataLoader.requestRefresh();
    }

    private void updateEventsInDay() {
        eventsInDay = new ArrayList<>();
        if (eventsData != null) {
            for (ListItemContainer item : eventsData) {
                String date = item.getDate().substring(0, 10);
                if (date.equals(strDate)) {
                    eventsInDay.add(item);
                }
            }
        }
        initializeAdapter();
    }

    private final BaseDataLoader.DataLoadedListener<List<DayParity>> mParityListener = new BaseDataLoader.DataLoadedListener<List<DayParity>>() {
        @Override
        public void onDataLoaded(List<DayParity> data) {
            parityList = data;
            initUI();
        }
    };

    private final BaseDataLoader.DataLoadedListener<List<ListItemContainer>> mEventsDataListener = new BaseDataLoader.DataLoadedListener<List<ListItemContainer>>() {
        @Override
        public void onDataLoaded(List<ListItemContainer> data) {
            eventsData = data;

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
//            updateEventsInDay();
        }
    };

    @Override
    public void onDataLoaded(Timetable timetable) {
        if (mTimetableDayFragment != null) {
            mTimetableDayFragment.onScheduleAvailable(timetable);
        }
    }

    private class ScheduleAndEventsAdapter extends FragmentPagerAdapter {

        public ScheduleAndEventsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mTimetableDayFragment = TimetableDayFragment.newInstance(Calendar.DATE);
            } else {
                return TimetableDayFragment.newInstance(2);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
