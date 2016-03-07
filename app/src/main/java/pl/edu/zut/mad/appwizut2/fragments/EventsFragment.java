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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.EventsLoader;

/**
 * Created by macko on 07.03.2016.
 */
public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<ListItemContainer> eventsData = new ArrayList<>();
    private List<ListItemContainer> eventsInDay = new ArrayList<>();

    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private EventsLoader mEventsDataLoader;
    private Date mDate;

    public EventsFragment(Date date) {
        mDate = date;
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

        DataLoadingManager loadingManager = DataLoadingManager.getInstance(getContext());
        mEventsDataLoader = loadingManager.getLoader(EventsLoader.class);
        mEventsDataLoader.registerAndLoad(mEventsDataListener);

        initializeAdapter();

        return rootView;
    }

    public void updateEventsInDay(Date selectedDate) {
        eventsInDay = new ArrayList<>();
        if (eventsData != null) {
            for (ListItemContainer item : eventsData) {
                String itemDate = item.getDate().substring(0, 10);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = simpleDateFormat.parse(itemDate);
                    if (date.compareTo(selectedDate) == 0) {
                        eventsInDay.add(item);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        }
        initializeAdapter();
    }

    @Override
    public void onRefresh() {
        mEventsDataLoader.requestRefresh();
    }

    private void initializeAdapter() {
        ListItemAdapter listItemAdapter = new ListItemAdapter(eventsInDay);
        itemListView.setAdapter(listItemAdapter);
    }

    private final BaseDataLoader.DataLoadedListener<List<ListItemContainer>> mEventsDataListener = new BaseDataLoader.DataLoadedListener<List<ListItemContainer>>() {
        @Override
        public void onDataLoaded(List<ListItemContainer> data) {
            eventsData = data;
            updateEventsInDay(mDate);

            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onDestroyView() {
        mEventsDataLoader.unregister(mEventsDataListener);
        mEventsDataLoader = null;
        super.onDestroyView();
    }
}
