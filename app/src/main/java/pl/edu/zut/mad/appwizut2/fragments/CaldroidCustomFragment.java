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
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.models.ListItemAdapter;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;
import pl.edu.zut.mad.appwizut2.network.WeekParityLoader;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;

public class CaldroidCustomFragment extends CaldroidFragment implements SwipeRefreshLayout.OnRefreshListener {

    private final static String CURRENT_MONTH = "current_month";
    private final static String CURRENT_YEAR = "current_year";
    private final static String CURRENT_CLICKED_DATE = "clicked_date";
    private List<DayParity> parityList;
    private ArrayList<ListItemContainer> eventsData = new ArrayList<>();
    private ArrayList<ListItemContainer> eventsInDay = new ArrayList<>();

    private TextView clickedDate;
    private RecyclerView itemListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    OfflineHandler offlineHandler;
    String strDate="";
    private int mMonth = 0;
    private int mYear = 0;

    private WeekParityLoader mParityLoader;

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
            mMonth = savedInstanceState.getInt(CURRENT_MONTH);
            mYear = savedInstanceState.getInt(CURRENT_YEAR);

        }
        // Call super
        super.onCreate(savedInstanceState);


        initModel(getContext());
        // TODO: We completely disabled Caldroid's state saving (since it's only causing problems)
        // Now we have to implement retaining currently selected month/year outselves
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: remove initUI method
        // (It shouldn't belong to onCreateView, this forces you to know day parity synchronously)
        initUI();
        if(mMonth != 0 && mYear != 0){
            month = mMonth;
            year = mYear;
        }

        // Get calendar view from superclass
        ViewGroup calendarView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        // Disable state saving on superclass'es view
        // This is workaround for Caldroid's improper handling of state saving
        for (int i = 0; i < calendarView.getChildCount(); i++) {
            calendarView.getChildAt(i).setSaveFromParentEnabled(false);
        }


        //setting toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_calendar);

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
        initializeAdapter();
        ////////////////////////////////////////////////////////////////////////////////

        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        // Initialize data load
        mParityLoader = DataLoadingManager.getInstance(getContext()).getLoader(WeekParityLoader.class);
        mParityLoader.registerAndLoad(mParityListener);

        return wrapper;
    }

    @Override
    public void onDestroyView() {
        mParityLoader.unregister(mParityListener);
        mParityLoader = null;
        super.onDestroyView();
    }

    private void initializeAdapter(){
        ListItemAdapter listItemAdapter = new ListItemAdapter(eventsInDay);
        itemListView.setAdapter(listItemAdapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){

            eventsData = (ArrayList<ListItemContainer>) savedInstanceState.getSerializable(Constans.INSTANCE_CURRENT_KEY);
            eventsInDay = (ArrayList<ListItemContainer>) savedInstanceState.getSerializable(Constans.INSTANCE_EVENTS_KEY);

            String selectedDate = savedInstanceState.getString(CURRENT_CLICKED_DATE);
            if(selectedDate != null ){
                clickedDate.setText(selectedDate);
            }
            initializeAdapter();
            if(eventsData.size() != 0) {
                clearProgressBar();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(eventsData != null && eventsInDay != null) {

            outState.putSerializable(Constans.INSTANCE_CURRENT_KEY, eventsData);
            outState.putSerializable(Constans.INSTANCE_EVENTS_KEY, eventsInDay);

        }
        if(listener != null) {
            outState.putString(CURRENT_CLICKED_DATE, clickedDate.getText().toString());
        }
        outState.putInt(CURRENT_MONTH, getMonth());
        outState.putInt(CURRENT_YEAR, getYear());
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
                //int eventsCount = dayParities.getEventsCount();
                int eventsCount = 0; // TODO: Get events count
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
            if (eventsData.size() != 0){
                eventsInDay = new ArrayList<>();
                for(ListItemContainer item : eventsData){
                    String tmp = item.getDate().substring(0,10);
                    if(tmp.equals(strDate)){
                        eventsInDay.add(item);
                    }
                }
                initializeAdapter();
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
                eventsData = FeedFragment.createItemList(pageContent);
                offlineHandler.setCurrentOfflineData(eventsData);
                offlineHandler.saveCurrentData();

            }else {
                eventsData = offlineHandler.getCurrentData(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (eventsData != null){
                eventsInDay = new ArrayList<>();
                for(ListItemContainer item : eventsData){
                    String date = item.getDate().substring(0,10);
                    if(date.equals(strDate)){
                        eventsInDay.add(item);
                    }
                }
                initializeAdapter();
            }
            clearProgressBar();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private final BaseDataLoader.DataLoadedListener<List<DayParity>> mParityListener = new BaseDataLoader.DataLoadedListener<List<DayParity>>() {
        @Override
        public void onDataLoaded(List<DayParity> data) {
            parityList = data;
            initUI();
        }
    };
}
