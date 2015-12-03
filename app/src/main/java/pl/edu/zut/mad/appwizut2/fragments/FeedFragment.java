package pl.edu.zut.mad.appwizut2.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Context;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;
import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.utils.Interfaces;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;

/**
 * Created by macko on 07.11.2015.
 */
public abstract class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "created";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "text";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_ID = "id";
    private static final String INSTANCE_CURRENT_KEY = "current_data";
    private static final String INSTANCE_CURRENT_SIZE = "current_size";



    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;


    private String addressUrl;
    private Context context;
    private List<ListItemContainer> currentData;
    OfflineHandler offlineHandler;


    private static final Pattern patternQuot = Pattern.compile("(&quot;)");

    protected void setFeedUrl(String addressUrl) {this.addressUrl = addressUrl;}

    /**
     * ustawienie modelu danych offline dla fragmentu
     * @param context
     */
    protected void initModel(Context context){
        if (addressUrl.equals(HTTPLinks.ANNOUNCEMENTS)){
            offlineHandler = new OfflineHandler(context, OfflineHandler.OfflineDataHandlerKeys.ANNOUNCEMENTS);
        }else if (addressUrl.equals(HTTPLinks.PLAN_CHANGES)){
            offlineHandler = new OfflineHandler(context, OfflineHandler.OfflineDataHandlerKeys.PLAN_CHANGES);
        }

    }

    protected View initView(LayoutInflater inflater, ViewGroup container, Context context) {
        this.context = context;

        View rootView = inflater.inflate(R.layout.item_list, container, false);
        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.item_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar)rootView.findViewById(R.id.item_list_progress_bar);


        itemListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        itemListView.setLayoutManager(layoutManager);

        //inicjalizacja pustego adaptera w celu uniknięcia błędu nieokreślonego layoutu
        itemListView.setAdapter(new ListItemAdapter(new ArrayList<ListItemContainer>()));

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){
            Integer size = savedInstanceState.getInt(INSTANCE_CURRENT_SIZE);
            currentData = new ArrayList<>();
            for (int i = 0; i < size;i++){
                currentData.add((ListItemContainer)savedInstanceState.getSerializable(INSTANCE_CURRENT_KEY + i));
            }

        }
        if (currentData != null){
            ListItemAdapter listItemAdapter = new ListItemAdapter(currentData);
            itemListView.setAdapter(listItemAdapter);
            clearProgressBar();
        }else {
            refresh();
        }
    }



    public void refresh(){
        DownloadContentTask task = new DownloadContentTask();
        task.execute(addressUrl);

    }
    @Override
    public void onRefresh() {
        refresh();

    }

    private List<ListItemContainer> createItemList(String pageContent) {
        List<ListItemContainer> itemList = new ArrayList<>();
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(TAG_ENTRY);

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                listItemContainer.setTitle(item.getString(TAG_TITLE));
                listItemContainer.setDate(item.getString(TAG_DATE));
                listItemContainer.setAuthor(item.getString(TAG_AUTHOR));
                listItemContainer.setId(item.getString(TAG_ID));

                String body = item.getString(TAG_BODY);
                Matcher matcher = patternQuot.matcher(body);
                body = matcher.replaceAll("\"");

                listItemContainer.setBody(body);
                itemList.add(listItemContainer);
            }

        } catch (JSONException e) {
            Log.i("FeedFragment: ","JSONException:" + e.getMessage());
            return null;
        }
        return itemList;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (int i = 0; i < currentData.size();i++){
            outState.putSerializable(INSTANCE_CURRENT_KEY + i,currentData.get(i) );

        }
        outState.putInt(INSTANCE_CURRENT_SIZE,currentData.size());
    }

    private void clearProgressBar(){
        if (progressBar != null) {
            progressBar.clearAnimation();
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
    }
    private class DownloadContentTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (HttpConnect.isOnline(context)) {
                HttpConnect connection = new HttpConnect(addressUrl);
                String pageContent = connection.getPage();
                currentData = createItemList(pageContent);
                offlineHandler.setCurrentOfflineData(currentData);
                offlineHandler.saveCurrentData(new Interfaces.CompletitionCallback() {
                    @Override
                    public void finished(Boolean success) {
                        Log.i("offline data save","result: " + (success ? "success" : "error"));
                    }
                });

            }else {
                currentData = offlineHandler.getCurrentData(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            clearProgressBar();
            if(currentData != null){
                ListItemAdapter listItemAdapter = new ListItemAdapter(currentData);
                itemListView.setAdapter(listItemAdapter);
            }
            swipeRefreshLayout.setRefreshing(false);
        }




    }
}
