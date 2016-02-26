package pl.edu.zut.mad.appwizut2.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.activities.MyGroups;
import pl.edu.zut.mad.appwizut2.activities.WebPlanActivity;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleEdzLoader;
import pl.edu.zut.mad.appwizut2.network.ScheduleLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Created by Dawid on 19.11.2015.
 */
public class TimetableFragment extends Fragment implements BaseDataLoader.DataLoadedListener<Timetable>, SharedPreferences.OnSharedPreferenceChangeListener {

    private String[] mDayNames;

    private Timetable mTimetable;

    private List<TimetableDayFragment> mActiveDayFragments = new ArrayList<>();
    private BaseDataLoader<Timetable, ?> mScheduleLoader;
    private View mTimetableUnavailableWrapper;
    private TextView mTimetableUnavailableMessage;
    private Button mOpenPdfButton;
    private ProgressBar mLoadingIndicator;
    private View mTimetableWrapper;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);

        mTimetableUnavailableWrapper = view.findViewById(R.id.timetable_unavailable);
        mTimetableUnavailableMessage = (TextView) view.findViewById(R.id.message);
        mOpenPdfButton = (Button) view.findViewById(R.id.view_pdf);
        Button chooseGroupButton = (Button) view.findViewById(R.id.choose_group);
        Button importFromEdziekanatButton = (Button) view.findViewById(R.id.import_from_edziekanat);


        mLoadingIndicator = (ProgressBar) view.findViewById(R.id.loading_indicator);

        // Setup tabs
        pager.setAdapter(new TimetableAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(pager);

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

        // Select current page depending on current week day
        if (savedInstanceState == null) {
            int weekday = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);
            int tabToSelect =
                    weekday == Calendar.TUESDAY   ? 1 :
                    weekday == Calendar.WEDNESDAY ? 2 :
                    weekday == Calendar.THURSDAY  ? 3 :
                    weekday == Calendar.FRIDAY    ? 4 :
                    0;
            pager.setCurrentItem(tabToSelect);
        }

        // Choose loader
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mScheduleLoader = chooseLoaderFromSettings(mPreferences);

        // Start loader
        mScheduleLoader.registerAndLoad(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        return view;
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
        if (mPreferences != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        mScheduleLoader.unregister(this);

        super.onDestroyView();
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

    void registerDayFragment(TimetableDayFragment dayFragment) {
        mActiveDayFragments.add(dayFragment);
        if (mTimetable != null) {
            dayFragment.onScheduleAvailable(mTimetable);
        }
    }

    void unregisterDayFragment(TimetableDayFragment dayFragment) {
        mActiveDayFragments.remove(dayFragment);
    }

    @Override
    public void onDataLoaded(Timetable timetable) {
        if (timetable != null) {
            mTimetable = timetable;
            for (TimetableDayFragment dayFragment : mActiveDayFragments) {
                dayFragment.onScheduleAvailable(timetable);
            }
            showTimetable();
        } else {
            showTimetableUnavailable(mScheduleLoader instanceof ScheduleLoader && ((ScheduleLoader) mScheduleLoader).isConfigured());
        }
    }

    public class TimetableAdapter extends FragmentPagerAdapter
    {

        public TimetableAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDayNames[position];
        }

        @Override
        public Fragment getItem(int position) {
            // Map tab number to day in Calendar
            int day =
                    position == 0 ? Calendar.MONDAY :
                    position == 1 ? Calendar.TUESDAY :
                    position == 2 ? Calendar.WEDNESDAY :
                    position == 3 ? Calendar.THURSDAY :
                    position == 4 ? Calendar.FRIDAY : -1;

            return TimetableDayFragment.newInstance(day);
        }
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.PREF_TIMETABLE_DATA_SOURCE.equals(key)) {
            mScheduleLoader.unregister(TimetableFragment.this);
            mScheduleLoader = chooseLoaderFromSettings(sharedPreferences);
            mScheduleLoader.registerAndLoad(TimetableFragment.this);
        }
    }

    private void openPdf() {
        new OpenSchedulePdfDialogFragment().show(getFragmentManager(), "OpenSchedulePdf");
    }
}
