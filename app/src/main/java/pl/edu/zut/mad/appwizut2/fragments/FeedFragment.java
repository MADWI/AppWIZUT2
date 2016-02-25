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

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.FeedLoader;

/**
 * Created by macko on 07.11.2015.
 * modified for OfflineData by Damian Malarczyk
 */
public abstract class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<List<ListItemContainer>> {


    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View mNoDataMessageView;


    private FeedLoader mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_list, container, false);
        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.item_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar)rootView.findViewById(R.id.item_list_progress_bar);
        mNoDataMessageView = rootView.findViewById(R.id.text_no_data);

        // TODO: These should be moved to layout xml
        itemListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        itemListView.setLayoutManager(layoutManager);

        //set empty adapter, not to get the "layout not set" warning
        itemListView.setAdapter(new ListItemAdapter(new ArrayList<ListItemContainer>()));

        // Load data
        mLoader = createLoader();
        mLoader.registerAndLoad(this);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        mLoader.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        if (mLoader != null) {
            mLoader.requestRefresh();
        }
    }

    @Override
    public void onDataLoaded(List<ListItemContainer> data) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if(data != null && data.size() > 0){
            mNoDataMessageView.setVisibility(View.INVISIBLE);
            ListItemAdapter listItemAdapter = new ListItemAdapter(data);
            itemListView.setAdapter(listItemAdapter);
        }else {
            itemListView.setAdapter(null);
            mNoDataMessageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Obtain loader to be used for this fragment
     */
    protected abstract FeedLoader createLoader();
}
