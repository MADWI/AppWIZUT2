package pl.edu.zut.mad.appwizut2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.activities.MyGroups;
import pl.edu.zut.mad.appwizut2.activities.WebPlanActivity;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleCommonLoader;
import pl.edu.zut.mad.appwizut2.utils.DateUtils;

/**
 * Created by Dawid on 19.11.2015.
 */
public class TimetableFragment extends Fragment implements BaseDataLoader.DataLoadedListener<Timetable> {

    private static final String STATE_SELECTED_DATE = "selected_date";

    private String[] mDayNames;

    private ScheduleCommonLoader mScheduleLoader;
    private View mTimetableUnavailableWrapper;
    private TextView mTimetableUnavailableMessage;
    private Button mOpenPdfButton;
    private ProgressBar mLoadingIndicator;
    private View mTimetableWrapper;
    private TimetableAdapter mTabsAdapter;
    private Date[] mTabs;
    private Date mDateToSelect;
    private ViewPager mViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mDateToSelect = new Date(savedInstanceState.getLong(STATE_SELECTED_DATE, 0));
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDayNames = getActivity().getResources().getStringArray(R.array.week_days_short);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_timetable);

        // Inflate layout
        View view = inflater.inflate(R.layout.timetable_layout, container, false);

        // Find views
        mTimetableWrapper = view.findViewById(R.id.timetable_main);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);

        mTimetableUnavailableWrapper = view.findViewById(R.id.timetable_unavailable);
        mTimetableUnavailableMessage = (TextView) view.findViewById(R.id.message);
        mOpenPdfButton = (Button) view.findViewById(R.id.view_pdf);
        Button chooseGroupButton = (Button) view.findViewById(R.id.choose_group);
        Button importFromEdziekanatButton = (Button) view.findViewById(R.id.import_from_edziekanat);


        mLoadingIndicator = (ProgressBar) view.findViewById(R.id.loading_indicator);

        // Setup tabs
        mTabsAdapter = new TimetableAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mTabsAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        // Setup button listeners
        mOpenPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdf();
            }
        });
        chooseGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MyGroups.class));
            }
        });
        importFromEdziekanatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), WebPlanActivity.class));
            }
        });

        // Initialize loader
        mScheduleLoader = DataLoadingManager.getInstance(getActivity()).getLoader(ScheduleCommonLoader.class);
        mScheduleLoader.registerAndLoad(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        mScheduleLoader.unregister(this);

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewPager != null && mTabs != null) {
            outState.putLong(STATE_SELECTED_DATE, mTabs[mViewPager.getCurrentItem()].getTime());
        }
    }

    /**
     * Hide loading indicator and show the timetable
     */
    private void showTimetable() {
        // Switch view to actual timetable
        mTimetableWrapper.setVisibility(View.VISIBLE);
        mTimetableUnavailableWrapper.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.GONE);
    }

    /**
     * Switch view to timetable not available state
     * where we have buttons for opening pdf and choosing group
     *
     * @param isConfigured Whether the user have selected any group in settings
     */
    private void showTimetableUnavailable(boolean isConfigured) {
        if (isConfigured) {
            mTimetableUnavailableMessage.setText(R.string.timetable_unavailable);
            mOpenPdfButton.setVisibility(View.VISIBLE);
        } else {
            mTimetableUnavailableMessage.setText(R.string.timetable_not_configured);
            mOpenPdfButton.setVisibility(View.GONE);
        }

        // Switch view to message
        mTimetableWrapper.setVisibility(View.GONE);
        mTimetableUnavailableWrapper.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onDataLoaded(final Timetable timetable) {
        if (timetable != null) {
            mTabs = getTabsForTimetable(timetable);
            if (mTabsAdapter != null) {
                mTabsAdapter.notifyDataSetChanged();

                // Choose tab to select
                // Based on last selected tab
                int tabToSelect = -1;
                if (mDateToSelect != null) {
                    tabToSelect = Arrays.binarySearch(mTabs, mDateToSelect);
                }
                // If we don't have that one (no saved state or week changed), select today
                if (tabToSelect < 0) {
                    GregorianCalendar now = new GregorianCalendar();
                    DateUtils.stripTime(now);
                    tabToSelect = Arrays.binarySearch(mTabs, new Date(now.getTimeInMillis()));
                }
                // If that also failed (e.g. it's now weekend and we have entries for Mon-Fri)
                // Select first tab
                if (tabToSelect < 0) {
                    tabToSelect = 0;
                }

                mViewPager.setCurrentItem(tabToSelect, false);
            }

            showTimetable();
        } else {
            showTimetableUnavailable(mScheduleLoader.isConfigured());
        }
    }

    public class TimetableAdapter extends FragmentPagerAdapter
    {

        public TimetableAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mTabs != null ? mTabs.length : 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(mTabs[position].getTime());
            return mDayNames[(cal.get(Calendar.DAY_OF_WEEK) + (7 - Calendar.MONDAY)) % 7];
        }

        @Override
        public Fragment getItem(int position) {
            return TimetableDayFragment.newInstance(mTabs[position]);
        }
    }

    private Date[] getTabsForTimetable(Timetable timetable) {
        //
        GregorianCalendar startOfWeek = new GregorianCalendar();
        DateUtils.stripTime(startOfWeek);

        int todayWeekDay = startOfWeek.get(Calendar.DAY_OF_WEEK);

        // Rewind calendar to last monday
        int daysFromMonday = todayWeekDay - Calendar.MONDAY;
        if (daysFromMonday < 0) {
            daysFromMonday += 7;
        }
        startOfWeek.add(Calendar.DATE, -daysFromMonday);

        // If we're in weekend skip to next week, but only if there are no entries there
        if (todayWeekDay == Calendar.SATURDAY || todayWeekDay == Calendar.SUNDAY) {
            boolean haveEntryForWeekend = hasEntryInTimetableForSpan(timetable, startOfWeek, 5, 6);

            if (!haveEntryForWeekend) {
                // Skip to next week
                startOfWeek.add(Calendar.DATE, 7);
            }
        }

        // Collect days
        ArrayList<Date> dates = new ArrayList<>(7);

        boolean haveEntryForNormalDays = hasEntryInTimetableForSpan(timetable, startOfWeek, 0, 4);
        boolean haveEntryForWeekend = hasEntryInTimetableForSpan(timetable, startOfWeek, 5, 6);

        // Add non-weekend days when we have them or we have no days at all
        if (haveEntryForNormalDays || !haveEntryForWeekend) {
            for (int i = 0; i < 5; i++) {
                dates.add(getDateForDayWithOffset(startOfWeek, i));
            }
        }

        // Add days from weekend
        if (haveEntryForWeekend) {
            for (int i = 5; i < 7; i++) {
                dates.add(getDateForDayWithOffset(startOfWeek, i));
            }
        }

        return dates.toArray(new Date[dates.size()]);
    }

    private Date getDateForDayWithOffset(GregorianCalendar calendar, int dayOffset) {
        if (dayOffset != 0) {
            calendar = (GregorianCalendar) calendar.clone();
            calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        }
        return new Date(calendar.getTimeInMillis());
    }

    private boolean hasEntryInTimetableFor(Timetable timetable, GregorianCalendar calendar, int dayOffset) {
        return timetable.getScheduleForDate(getDateForDayWithOffset(calendar, dayOffset)) != null;
    }

    private boolean hasEntryInTimetableForSpan(Timetable timetable, GregorianCalendar calendar, int firstDayOffset, int lastDayOffset) {
        for (int i = firstDayOffset; i <= lastDayOffset; i++) {
            if (hasEntryInTimetableFor(timetable, calendar, i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.view_pdf) {
            openPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPdf() {
        new OpenSchedulePdfDialogFragment().show(getFragmentManager(), "OpenSchedulePdf");
    }
}
