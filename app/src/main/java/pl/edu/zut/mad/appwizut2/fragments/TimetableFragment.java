package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleLoader;

/**
 * Created by Dawid on 19.11.2015.
 */
public class TimetableFragment extends Fragment implements BaseDataLoader.DataLoadedListener<Timetable> {

    private ViewPager mPager;

    private TabLayout mTabLayout;

    private String[] mDayNames;

    private Timetable mTimetable;

    private List<TimetableDayFragment> mActiveDayFragments = new ArrayList<>();
    private ScheduleLoader mScheduleLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mScheduleLoader = DataLoadingManager.getInstance(getActivity()).getLoader(ScheduleLoader.class);
        mScheduleLoader.registerAndLoad(this);
    }

    @Override
    public void onDestroy() {
        mScheduleLoader.unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDayNames = getActivity().getResources().getStringArray(R.array.week_days_short);

        View view = inflater.inflate(R.layout.timetable_layout, container, false);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new TimetableAdapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mPager);

        return view;
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
            return TimetableDayFragment.newInstance(position);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            mScheduleLoader.requestRefresh();
            return true;
        }
        if (item.getItemId() == R.id.view_pdf) {
            new OpenSchedulePdfDialogFragment().show(getFragmentManager(), "OpenSchedulePdf");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
