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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.FeedLoader;
import pl.edu.zut.mad.appwizut2.utils.Constans;

/**
 * Created by macko on 07.11.2015.
 * modified for OfflineData by Damian Malarczyk
 */
public abstract class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<List<ListItemContainer>> {


    private static final String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "content";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_ID = "id";

    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;


    private FeedLoader mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_list, container, false);
        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.item_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar)rootView.findViewById(R.id.item_list_progress_bar);

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

    // TODO: move this method to FeedLoader once we're not using it anywhere else
    // and then let exceptions from it be handled by caller (add throws)
    public static ArrayList<ListItemContainer> createItemList(String pageContent) {
        ArrayList<ListItemContainer> itemList = new ArrayList<>();
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(Constans.TAG_ENTRY);

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                listItemContainer.setTitle(item.getString(TAG_TITLE));
                listItemContainer.setDate(item.getString(Constans.TAG_DATE));
                listItemContainer.setAuthor(item.getString(TAG_AUTHOR));
                listItemContainer.setId(item.getString(TAG_ID));
                listItemContainer.setBody(item.getString(TAG_BODY));
                itemList.add(listItemContainer);
            }

        } catch (JSONException e) {
            Log.e("FeedFragment: ", "JSONException:" + e.getMessage());
            return null;
        }
        return itemList;
    }

    @Override
    public void onDataLoaded(List<ListItemContainer> data) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        if(data != null){
            ListItemAdapter listItemAdapter = new ListItemAdapter(data);
            itemListView.setAdapter(listItemAdapter);
        }
    }

    /**
     * Obtain loader to be used for this fragment
     */
    protected abstract FeedLoader createLoader();
}
