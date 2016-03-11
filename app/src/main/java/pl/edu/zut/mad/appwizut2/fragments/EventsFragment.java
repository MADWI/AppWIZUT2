package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.EventsLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Created by macko on 07.03.2016.
 */
public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<List<ListItemContainer>> {

    private List<ListItemContainer> eventsData = new ArrayList<>();

    private TextView mNoDataView;
    private RecyclerView mItemListView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private EventsLoader mEventsDataLoader;
    private Date mDate;

    public static EventsFragment newInstance(Date date) {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.ARG_DATE, date.getTime());

        EventsFragment eventsFragment = new EventsFragment();
        eventsFragment.setArguments(bundle);

        return eventsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle retainableArguments = savedInstanceState != null ? savedInstanceState : getArguments();
        mDate = new Date(retainableArguments.getLong(Constants.ARG_DATE));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.item_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.item_list_progress_bar);
        mProgressBar.clearAnimation();
        mProgressBar.setVisibility(View.GONE);

        mNoDataView = (TextView) rootView.findViewById(R.id.text_no_data);
        mNoDataView.setText(R.string.no_events);

        mItemListView = (RecyclerView) rootView.findViewById(R.id.itemList);
        mItemListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mItemListView.setLayoutManager(linearLayoutManager);

        DataLoadingManager loadingManager = DataLoadingManager.getInstance(getContext());
        mEventsDataLoader = loadingManager.getLoader(EventsLoader.class);
        mEventsDataLoader.registerAndLoad(this);

        return rootView;
    }

    public void setDate(Date date) {
        mDate = date;
        putDataInView();
    }

    @Override
    public void onDataLoaded(List<ListItemContainer> data) {
        eventsData = data;
        putDataInView();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void putDataInView() {
        List<ListItemContainer> eventsInDay = new ArrayList<>();
        if (eventsData != null ) {
            for (ListItemContainer item : eventsData) {
                String itemDateString = item.getDate().substring(0, 10);
                Date itemDate = null;
                try {
                    itemDate = Constants.FOR_EVENTS_FORMATTER.parse(itemDateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (mDate.compareTo(itemDate) == 0) {
                    eventsInDay.add(item);
                }
            }
        }
        ListItemAdapter listItemAdapter = new ListItemAdapter(eventsInDay);
        mItemListView.setAdapter(listItemAdapter);


        if (eventsInDay.size() == 0) {
            mNoDataView.setVisibility(View.VISIBLE);
        } else {
            mNoDataView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        mEventsDataLoader.requestRefresh();
    }

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(this);
        mEventsDataLoader = null;
        super.onDestroyView();
    }
}
