package pl.edu.zut.mad.appwizut2.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pl.edu.zut.mad.appwizut2.CaldroidCustomAdapter;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;
import pl.edu.zut.mad.appwizut2.utils.Interfaces;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;
import pl.edu.zut.mad.appwizut2.utils.WeekParityChecker;

public class CaldroidCustomFragment extends CaldroidFragment implements SwipeRefreshLayout.OnRefreshListener {

    private final WeekParityChecker checker = new WeekParityChecker();
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd");
    private static final SimpleDateFormat REVERSED_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat FOR_EVENTS_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    public static final String INSTANCE_COMPRESSED_KEY = "compressed_data";
    public static final String INSTANCE_COMPRESSED_SIZE = "compressed_size";
    private static ArrayList<DayParity> parityList;
    private List<ListItemContainer> currentData;
    private List<ListItemContainer> compresedData;

    private TextView clickedDate;
    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    OfflineHandler offlineHandler;
    String strDate="";

    /**
     * ustawienie modelu danych offline dla fragmentu
     * @param context
     */
    protected void initModel(Context context){
        if (HTTPLinks.ANNOUNCEMENTS.equals(HTTPLinks.ANNOUNCEMENTS)){
            offlineHandler = new OfflineHandler(context, OfflineHandler.OfflineDataHandlerKeys.ANNOUNCEMENTS);
        }else if (HTTPLinks.ANNOUNCEMENTS.equals(HTTPLinks.PLAN_CHANGES)){
            offlineHandler = new OfflineHandler(context, OfflineHandler.OfflineDataHandlerKeys.PLAN_CHANGES);
        }

    }

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // michalbednarski: Hacky workaround for Caldroid's saved state mishandling
        if (savedInstanceState != null) {
            // Delete info for child fragment manager
            savedInstanceState.remove("android:support:fragments");
        }
        // Call super
        super.onCreate(savedInstanceState);


        if (parityList == null) {
            try {
                parityList = new AsyncTaskGetParityList().execute().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        initModel(getContext());
        // TODO: We completely disabled Caldroid's state saving (since it's only causing problems)
        // Now we have to implement retaining currently selected month/year outselves
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: remove initUI method
        // (It shouldn't belong to onCreateView, this forces you to know day parity synchronously)
        initUI();

        // Get calendar view from superclass
        ViewGroup calendarView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        // Disable state saving on superclass'es view
        // This is workaround for Caldroid's improper handling of state saving
        for (int i = 0; i < calendarView.getChildCount(); i++) {
            calendarView.getChildAt(i).setSaveFromParentEnabled(false);
        }


        //setting toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Kalendarz");

        // Wrap calendarView into out fragment
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.calendar_layout, container, false);
        clickedDate = (TextView) wrapper.findViewById(R.id.dateTextView);
        ///////////////////////////////Setting for RecyclerView and ProgressBar
        itemListView = (RecyclerView) wrapper.findViewById(R.id.itemList);

        swipeRefreshLayout = (SwipeRefreshLayout)wrapper.findViewById(R.id.item_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar)wrapper.findViewById(R.id.item_list_progress_bar);
        progressBar.clearAnimation();
        progressBar.setVisibility(View.GONE);

        itemListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        itemListView.setLayoutManager(layoutManager);

        //inicjalizacja pustego adaptera w celu uniknięcia błędu nieokreślonego layoutu
        itemListView.setAdapter(new ListItemAdapter(new ArrayList<ListItemContainer>()));
        ////////////////////////////////////////////////////////////////////////////////

        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        return wrapper;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){
            Integer size = savedInstanceState.getInt(FeedFragment.INSTANCE_CURRENT_SIZE);
            currentData = new ArrayList<>();
            for (int i = 0; i < size;i++){
                currentData.add((ListItemContainer)savedInstanceState.getSerializable(FeedFragment.INSTANCE_CURRENT_KEY + i));
            }
            size = savedInstanceState.getInt(INSTANCE_COMPRESSED_SIZE);
            compresedData = new ArrayList<>();
            for (int i = 0; i < size;i++){
                compresedData.add((ListItemContainer)savedInstanceState.getSerializable(INSTANCE_COMPRESSED_KEY + i));
            }
            ListItemAdapter listItemAdapter = new ListItemAdapter(compresedData);
            itemListView.setAdapter(listItemAdapter);
            if(currentData.size() != 0) {
                clearProgressBar();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(currentData != null && compresedData != null) {
            for (int i = 0; i < currentData.size(); i++) {
                outState.putSerializable(FeedFragment.INSTANCE_CURRENT_KEY + i, currentData.get(i));

            }
            outState.putInt(FeedFragment.INSTANCE_CURRENT_SIZE, currentData.size());
            for (int i = 0; i < compresedData.size(); i++) {
                outState.putSerializable(INSTANCE_COMPRESSED_KEY + i, compresedData.get(i));

            }
            outState.putInt(INSTANCE_COMPRESSED_SIZE, compresedData.size());
        }
    }

    private void initUI() {

        // SETTING THE BACKGROUND
        // Create a hash map
        HashMap hm = new HashMap();
        // Put elements to the map

        HashMap events = new HashMap();
        if (parityList != null) {
            for (DayParity dayParities : parityList) {
                String parity = dayParities.getParity();
                if (parity.equals("parzysty")) {
                    hm.put(ParseDate(dayParities.getDate()), R.color.even);
                } else {
                    hm.put(ParseDate(dayParities.getDate()), R.color.uneven);
                }
                int eventsCount = dayParities.getEventsCount();
                if(eventsCount > 0){
                    events.put(dayParities.getDate(),eventsCount);
                }
            }
        }
        HashMap<String, Object> extraData = getExtraData();
        extraData.put("EVENTS", events);

        if (!hm.isEmpty()) {
            setBackgroundResourceForDates(hm);
        }
        setCaldroidListener(listener);
        refreshView();
    }

    @Override
    protected void retrieveInitialArgs() {
        setThemeResource(R.style.CaldroidCustomized);
        super.retrieveInitialArgs();
    }


    // Setup listener
    public CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {

            strDate = FOR_EVENTS_FORMATTER.format(date);
            if (currentData.size() != 0){
                compresedData = new ArrayList<>();
                int i = 0;
                for(ListItemContainer item : currentData){
                    String tmp = item.getDate().substring(0,10);
                    if(i < 6 && tmp.equals(strDate)){
                        compresedData.add(item);
                        i++;
                    }
                    if(i >= 5)
                        break;
                }
                ListItemAdapter listItemAdapter = new ListItemAdapter(compresedData);
                itemListView.setAdapter(listItemAdapter);
                clearProgressBar();
            }else {
                progressBar.animate();
                progressBar.setVisibility(View.VISIBLE);
                refresh();
            }
            clickedDate.setText("Wydarzenia " + REVERSED_FORMATTER.format(date));
        }
    };


    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str) {
        Date dateStr = null;
        try {
            dateStr = FORMATTER.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public void refresh(){
        DownloadContentTask task = new DownloadContentTask();
        task.execute(HTTPLinks.ANNOUNCEMENTS);

    }
    @Override
    public void onRefresh() {
        refresh();
    }


    private class AsyncTaskGetParityList extends
            AsyncTask<Void, Void, ArrayList<DayParity>> {

        ArrayList<DayParity> tempArray = null;

        @Override
        protected ArrayList<DayParity> doInBackground(Void... params) {

            tempArray = checker.getAllParity();
            if (tempArray != null) {
                return tempArray;
            } else {
                Log.i(TAG, "Nie mozna pobrac");

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");

        }

        @Override
        protected void onPostExecute(ArrayList<DayParity> result) {
            Log.i(TAG, "onPostExecute");
            initUI();
        }

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
            if (HttpConnect.isOnline(getContext())) {
                HttpConnect connection = new HttpConnect(HTTPLinks.ANNOUNCEMENTS);
                String pageContent = connection.getPage();
                currentData = FeedFragment.createItemList(pageContent);
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

            if (currentData != null){
                compresedData = new ArrayList<>();
                int i = 0;
                for(ListItemContainer item : currentData){
                    String date = item.getDate().substring(0,10);
                    if(i < 6 && date.equals(strDate)){
                        compresedData.add(item);
                        i++;
                    }
                    if(i >= 5)
                        break;
                }
                ListItemAdapter listItemAdapter = new ListItemAdapter(compresedData);
                itemListView.setAdapter(listItemAdapter);
            }
            clearProgressBar();
            swipeRefreshLayout.setRefreshing(false);
        }




    }
}
