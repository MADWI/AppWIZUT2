package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
    private List<ListItemContainer> eventsInDay = new ArrayList<>();

    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private EventsLoader mEventsDataLoader;
    private String mDate;

    public static EventsFragment newInstance(String date) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.CURRENT_CLICKED_DATE, date);

        EventsFragment eventsFragment = new EventsFragment();
        eventsFragment.setArguments(bundle);

        return eventsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.item_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        progressBar = (ProgressBar) rootView.findViewById(R.id.item_list_progress_bar);
        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);

        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);
        itemListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        itemListView.setLayoutManager(linearLayoutManager);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mDate = bundle.getString(Constants.CURRENT_CLICKED_DATE);
        }

        DataLoadingManager loadingManager = DataLoadingManager.getInstance(getContext());
        mEventsDataLoader = loadingManager.getLoader(EventsLoader.class);
        mEventsDataLoader.registerAndLoad(this);

        return rootView;
    }

    @Override
    public void onDataLoaded(List<ListItemContainer> data) {
        eventsData = data;
        updateEventsInDay(mDate);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void updateEventsInDay(String selectDate) {
        eventsInDay = new ArrayList<>();
        if (eventsData != null ) {
            for (ListItemContainer item : eventsData) {
                String itemDate = item.getDate().substring(0, 10);
                if (selectDate.compareTo(itemDate) == 0) {
                    eventsInDay.add(item);
                }
            }
        }
        ListItemAdapter listItemAdapter = new ListItemAdapter(eventsInDay);
        itemListView.setAdapter(listItemAdapter);
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
