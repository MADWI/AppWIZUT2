package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.BusHours;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Created by barto on 23/11/2015.
 */
public class BusTimetableFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<List<BusHours>> {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private View mDataNotAvailableView;

    private BusTimetableLoader mLoader;

    /** Keys for Map used by {@link SimpleAdapter} */
    private static final String TAG_FROM_TO = "type";
    private static final String TAG_LINE_NUMBER = "line";
    private static final String TAG_HOUR_INFO = "hour_info";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set activity title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_public_transport);

        // Inflate view and find views
        View view = inflater.inflate(R.layout.bus_list_layout, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.item_list_progress_bar);
        mDataNotAvailableView = view.findViewById(R.id.data_not_available);

        // Create and register loader
        mLoader = DataLoadingManager.getInstance(getContext()).getLoader(BusTimetableLoader.class);
        mLoader.registerAndLoad(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        // Unregister from loader
        mLoader.unregister(this);
        super.onDestroyView();
    }

    public void onRefresh(){
        mSwipeRefreshLayout.setRefreshing(true);
        mLoader.requestRefresh();
    }

    private void clearProgressBar(){
        mDataNotAvailableView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDataLoaded(List<BusHours> buses) {
        if (buses == null) {
            clearProgressBar();
            mDataNotAvailableView.setVisibility(View.VISIBLE);
            return;
        }

        GregorianCalendar today = new GregorianCalendar();
        int currentMinute = today.get(Calendar.HOUR_OF_DAY) * 60 + today.get(Calendar.MINUTE);


        List<Map<String, String>> departuresForDisplay = new ArrayList<>();
        for (BusHours busHours : buses) {
            int[] rawDepartures = busHours.getHoursForDay(today);
            if (rawDepartures == null) {
                continue;
            }

            int numberOfTimesToDisplay = Constants.DISPLAYED_DEPARTURES_COUNT;

            // Find first departure after now
            int i = Arrays.binarySearch(rawDepartures, currentMinute);
            if (i < 0) {
                i = ~i;
            }

            StringBuilder departureHours = new StringBuilder();
            boolean firstHour = true;

            // Convert data to human readable h:mm
            for (; i < rawDepartures.length && numberOfTimesToDisplay > 0; i++, numberOfTimesToDisplay--) {
                if (firstHour) {
                    firstHour = false;
                } else {
                    departureHours.append(' ');
                }
                departureHours.append(BusHours.minuteNrToHhMmString(rawDepartures[i]));
            }

            // TODO: Do we want to show next day upcoming departures here?

            // Wrap results for display
            ArrayMap<String, String> busForDisplay = new ArrayMap<>();
            busForDisplay.put(TAG_LINE_NUMBER, busHours.getStop().getLineName());
            busForDisplay.put(TAG_FROM_TO, busHours.getStop().getFromTo());
            busForDisplay.put(TAG_HOUR_INFO, departureHours.toString());
            departuresForDisplay.add(busForDisplay);
        }

        // Put results in ListView
        ListAdapter adapter = new SimpleAdapter(
                getActivity(),
                departuresForDisplay,
                R.layout.bus_timetable_layout,
                new String[]{TAG_LINE_NUMBER, TAG_FROM_TO, TAG_HOUR_INFO},
                new int[]{R.id.line, R.id.type, R.id.hour}
        );

        setListAdapter(adapter);
        clearProgressBar();
    }
}
