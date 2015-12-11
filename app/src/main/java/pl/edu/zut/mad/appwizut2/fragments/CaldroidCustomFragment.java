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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.edu.zut.mad.appwizut2.CaldroidCustomAdapter;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;
import pl.edu.zut.mad.appwizut2.utils.Interfaces;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;
import pl.edu.zut.mad.appwizut2.utils.WeekParityChecker;

public class CaldroidCustomFragment extends CaldroidFragment implements SwipeRefreshLayout.OnRefreshListener {

    private WeekParityChecker checker;
    private static ArrayList<DayParity> parityList;
    private List<ListItemContainer> currentData = new ArrayList<>();
    private List<ListItemContainer> compresedData = new ArrayList<>();

    private TextView clickedDate;
    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    Bundle setBundle;
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

        checker = new WeekParityChecker(getActivity().getApplicationContext());


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
        initialAdapter();
        ////////////////////////////////////////////////////////////////////////////////

        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        return wrapper;
    }

    private void initialAdapter(){
        ListItemAdapter listItemAdapter = new ListItemAdapter(compresedData);
        itemListView.setAdapter(listItemAdapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (parityList == null) {
            new AsyncTaskGetParityList().execute();
        }
        if (savedInstanceState != null){

            currentData = getStates(Constans.INSTANCE_CURRENT_SIZE, Constans.INSTANCE_CURRENT_KEY, savedInstanceState);
            compresedData = getStates(Constans.INSTANCE_COMPRESSED_SIZE, Constans.INSTANCE_COMPRESSED_KEY, savedInstanceState);
            initialAdapter();
            if(currentData.size() != 0) {
                clearProgressBar();
            }
        }
    }

    private List<ListItemContainer> getStates(final String sizeKey, final String key, Bundle state){
        Integer size = state.getInt(sizeKey);
        List<ListItemContainer> tmp = new ArrayList<>();
        for (int i = 0; i < size;i++){
            tmp.add((ListItemContainer) state.getSerializable(key + i));
        }
        return tmp;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(currentData != null && compresedData != null) {
            setBundle = new Bundle();
            setStates(Constans.INSTANCE_CURRENT_SIZE,Constans.INSTANCE_CURRENT_KEY, true);
            setStates(Constans.INSTANCE_COMPRESSED_SIZE, Constans.INSTANCE_COMPRESSED_KEY, false);
            outState.putAll(setBundle);
        }
    }

    private void setStates(final String sizeKey, final String key, boolean b){
        List<ListItemContainer> tmp = b ? currentData : compresedData;
        for (int i = 0; i < tmp.size(); i++) {
            setBundle.putSerializable(key + i, tmp.get(i));
        }
        setBundle.putInt(sizeKey, tmp.size());
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

            strDate = Constans.FOR_EVENTS_FORMATTER.format(date);
            if (currentData.size() != 0){
                int i = 0;
                compresedData = new ArrayList<>();
                for(ListItemContainer item : currentData){
                    String tmp = item.getDate().substring(0,10);
                    if(i < 6 && tmp.equals(strDate)){
                        compresedData.add(item);
                        i++;
                    }
                    if(i >= 5)
                        break;
                }
                initialAdapter();
                clearProgressBar();
            }else {
                progressBar.animate();
                progressBar.setVisibility(View.VISIBLE);
                refresh();
            }
            clickedDate.setText("Wydarzenia " + Constans.REVERSED_FORMATTER.format(date));
        }
    };


    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str) {
        Date dateStr = null;
        try {
            dateStr = Constans.FORMATTER.parse(date_str);
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
            AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (HttpConnect.isOnline(getContext())) {
                parityList = checker.getAllParity();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
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
                HttpConnect connection = new HttpConnect(params[0]);
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
                initialAdapter();
            }
            clearProgressBar();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
