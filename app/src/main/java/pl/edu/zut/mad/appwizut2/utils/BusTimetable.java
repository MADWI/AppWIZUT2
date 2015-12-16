package pl.edu.zut.mad.appwizut2.utils;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.BusTimetableModel;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader.BusModelDayType;
import pl.edu.zut.mad.appwizut2.network.HTTPLinks;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;

/**
 * Created by Bartosz Kozajda on 23/11/2015.
 */
public class BusTimetable extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static HttpConnect site = null;
    private String lineInfo ="";
    private String lineNo = "";


    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    LayoutInflater inflater;
    ViewGroup container;


    // JSON Node names
    private static final String TAG_DEPARTURES = "departures";
    private static final String TAG_TYPE = "type";
    private static final String TAG_D_LINE = "line";
    private static final String TAG_DEPARTURES2 = "departures";
    private static final String TAG_HOUR_INFO = "hour_info";
    public static final String CURRENT_DATA_KEY = "current_data";
    // JSON editable node names
    private static String TAG_HOUR = "";
    private static String TAG_HOUR2 = "";
    private static String TAG_LINE="";
    private static String depart = "";
    private static String depart2 = "";

    private String hour2;
    private int hour;
    String str[], str2[];


    JSONArray departures = null;
    ArrayList<HashMap<String,String>> departuresList;
    BusTimetableLoader loader;


    protected View initView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.bus_list_layout, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar)rootView.findViewById(R.id.item_list_progress_bar);


        loader = new BusTimetableLoader(getBaseModel(),getContext());
        loader.getData(false, new BusTimetableLoader.BusTimetableLoaderCallback() {
            @Override
            public void foundData(ArrayList<BusTimetableModel> data) {
                if (isFragmentUIActive() && data != null) {

                    parseDataFromLoader(data);
                }

            }
        });


        return rootView;
    }

    public void onRefresh(){
        swipeRefreshLayout.setRefreshing(true);
        //refreshList(inflater, container);
        refresh();
    }

    private void refresh(){
        loader.getData(false, new BusTimetableLoader.BusTimetableLoaderCallback() {
            @Override
            public void foundData(ArrayList<BusTimetableModel> data) {
                parseDataFromLoader(data);
            }
        });
    }

    private void refreshList(LayoutInflater inflater, ViewGroup container) {
        initUI();
    }

    public void initUI() {
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hour2 = Integer.toString(hour);

        TAG_HOUR = hour2;
        TAG_HOUR2 = Integer.toString(hour + 1);

        //departuresList = new ArrayList<>();

        // Calling async task to get json
        //new GetTimetables().execute();


    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null){

            departuresList = (ArrayList<HashMap<String,String>>)savedInstanceState.getSerializable(CURRENT_DATA_KEY);
            if (departuresList != null) {
                ListAdapter adapter = new SimpleAdapter(getActivity(), departuresList,
                        R.layout.bus_timetable_layout, new String[]{TAG_D_LINE, TAG_TYPE,
                        TAG_HOUR_INFO}, new int[]{R.id.line, R.id.type, R.id.hour});
                clearProgressBar();
                setListAdapter(adapter);
            }else {

                refresh();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT_DATA_KEY,departuresList);
    }

    private void parseDataFromLoader(ArrayList<BusTimetableModel> data){
        departuresList = new ArrayList<>();
        SharedPreferences preferences = SharedPrefUtils.getSharedPreferences(getContext());
        String easterYear = preferences.getString(Constants.EASTER_YEAR_KEY,"");

        Calendar cal = Calendar.getInstance();

        if (easterYear.equals("") || !easterYear.equals(String.valueOf(cal.get(Calendar.YEAR)))){
            String easterDate = BusTimetableLoader.getEasterSundayDate(cal.get(Calendar.YEAR));
            preferences.edit().putString(Constants.EASTER_DATE_KEY,easterDate).commit();
        }
        int hour1 = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int day1 = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth1 = cal.get(Calendar.DAY_OF_MONTH);

        cal.add(Calendar.HOUR_OF_DAY,1);
        int hour2 = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.DAY_OF_WEEK,1);

        int day2 = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth2 = cal.get(Calendar.DAY_OF_MONTH);


        String[] easterDate = preferences.getString(Constants.EASTER_DATE_KEY,"").split(" ");
        int easterMonth = Integer.valueOf(easterDate[0]);
        int easterDay = Integer.valueOf(easterDate[1]);
        BusModelDayType dayType1 = BusModelDayType.WEEK_DAY;
        BusModelDayType dayType2 = BusModelDayType.WEEK_DAY;

        if (day1 == Calendar.SATURDAY){
            dayType1 = BusModelDayType.SATURDAY;
        }else if (day1 == Calendar.SUNDAY){
            dayType1 = BusModelDayType.SUNDAY;
        }

        if (day2 == Calendar.SATURDAY){
            dayType2 = BusModelDayType.SATURDAY;
        }else if (day2 == Calendar.SUNDAY){
            dayType2 = BusModelDayType.SUNDAY;
        }

        int month = cal.get(Calendar.MONTH);

        if (month == Calendar.DECEMBER){
            if (dayOfMonth1 == 25){
                dayType1 = BusModelDayType.HOLIDAYS;
            }else if (dayOfMonth1 == 24 || dayOfMonth1 == 31){
                dayType2 = BusModelDayType.HOLIDAYS;
            }

        }else if (month == Calendar.JANUARY){
            if (dayOfMonth1 == 1){
                dayType1 = BusModelDayType.HOLIDAYS;
            }

        }else if (month == easterMonth){
            if (dayOfMonth1 == easterDay){
                dayType1 = BusModelDayType.HOLIDAYS;
            }else if (dayOfMonth2 == easterDay){
                dayType2 = BusModelDayType.HOLIDAYS;
            }

        }else if (month >= Calendar.JULY && month <= Calendar.SEPTEMBER){
            if (day1 != Calendar.SATURDAY && day1 != Calendar.SUNDAY){
                dayType1 = BusModelDayType.WEEK_DAY_STUDENTS_HOLIDAY;
            }

            if (day2 != Calendar.SATURDAY && day1 != Calendar.SUNDAY){
                dayType2 = BusModelDayType.WEEK_DAY_STUDENTS_HOLIDAY;
            }
        }




        for (BusTimetableModel model : data){

            // tmp hashmap for single departure
            HashMap<String, String> departure = new HashMap<>();
            departure.put(TAG_D_LINE,model.getLineNumber());
            departure.put(TAG_TYPE,model.getLineInfo());


            HashMap<Integer,ArrayList<Integer>> dayInfo = model.getInfo().get(dayType1.toString());
            if (dayInfo == null){
                if (dayType1 == BusModelDayType.WEEK_DAY){
                    dayInfo = model.getInfo().get(BusModelDayType.WEEK_DAY_STUDENTS_YEAR.toString());
                }else if (dayType1 == BusModelDayType.SATURDAY || dayType1 == BusModelDayType.SUNDAY){
                    dayInfo = model.getInfo().get(BusModelDayType.WEEKEND.toString());
                }
            }
            ArrayList<Integer> hourOne = null;
            ArrayList<Integer> hourTwo = null;
            if (dayInfo != null){
                hourOne = dayInfo.get(hour1);
                hourTwo = dayInfo.get(hour2);
            }
            if (hour1 == 23){
                HashMap<Integer,ArrayList<Integer>> dayInfo2 = model.getInfo().get(dayType2.toString());
                if (dayInfo2 == null){
                    if (dayType2 == BusModelDayType.WEEK_DAY){
                        dayInfo2 = model.getInfo().get(BusModelDayType.WEEK_DAY_STUDENTS_YEAR.toString());
                    }else if (dayType2 == BusModelDayType.SUNDAY || dayType2 == BusModelDayType.SATURDAY){
                        dayInfo2 = model.getInfo().get(BusModelDayType.WEEKEND.toString());
                    }
                }
                if (dayInfo2 != null){
                    hourTwo = dayInfo2.get(hour2);
                }else {
                    hourTwo = null;
                }
            }

            String dep1 = "";
            if (hourOne != null){
                for (Integer thatMin : hourOne){
                    if (minute <= thatMin){
                        String thatMinS = String.valueOf(thatMin);
                        if (thatMin < 10)
                            thatMinS = "0" + thatMinS;

                        dep1 = dep1 + hour1 + ":" + thatMinS + "   ";
                    }
                }

            }

            String dep2 = "";
            if (hourTwo != null){
                for (Integer thatMin : hourTwo){
                    String thatMinS = String.valueOf(thatMin);
                    if (thatMin < 10)
                        thatMinS = "0" + thatMinS;
                    dep2 = dep2 + hour2 + ":" + thatMinS + "   ";

                }

            }
            if (!dep1.equals("") || !dep2.equals("")) {
                departure.put(TAG_HOUR_INFO, dep1 + dep2);
                departuresList.add(departure);
            }

        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFragmentUIActive()) {
                    ListAdapter adapter = new SimpleAdapter(getActivity(), departuresList,
                            R.layout.bus_timetable_layout, new String[]{TAG_D_LINE, TAG_TYPE,
                            TAG_HOUR_INFO}, new int[]{R.id.line, R.id.type, R.id.hour});

                    setListAdapter(adapter);
                    clearProgressBar();
                    swipeRefreshLayout.setRefreshing(false);

                }
            }
        });


    }

    private ArrayList<BusTimetableModel> getBaseModel(){
        BusTimetableModel bus4 = new BusTimetableModel("4"," Żołnierska -> Pomorzany",HTTPLinks.line4);
        BusTimetableModel bus5 = new BusTimetableModel("5"," Żołnierska -> Stocznia Szczecińska",HTTPLinks.line5);
        BusTimetableModel bus7 = new BusTimetableModel("7"," Żołnierska -> Basen Górniczy",HTTPLinks.line7);
        BusTimetableModel bus53 = new BusTimetableModel("53"," Klonowica Zajezdnia -> Stocznia Szczecińska",HTTPLinks.line53);
        BusTimetableModel bus53_2 = new BusTimetableModel("53"," Klonowica Zajezdnia -> Pomorzany Dobrzyńska",HTTPLinks.line53_2);
        BusTimetableModel bus60 = new BusTimetableModel("60"," Żołnierska -> Cukrowa",HTTPLinks.line60);
        BusTimetableModel bus60_2 = new BusTimetableModel("60"," Klonowica Zajezdnia -> Stocznia Szczecińska",HTTPLinks.line60_2);
        BusTimetableModel bus75 = new BusTimetableModel("75"," Klonowica Zajezdnia -> Dworzec Główny",HTTPLinks.line75);
        BusTimetableModel bus75_2 = new BusTimetableModel("75"," Klonowica Zajezdnia -> Krzekowo",HTTPLinks.line75_2);
        BusTimetableModel bus80 = new BusTimetableModel("80"," Klonowica Zajezdnia -> Rugiańska",HTTPLinks.line80);
        ArrayList<BusTimetableModel> buses = new ArrayList<>();
        buses.add(bus4);
        buses.add(bus5);
        buses.add(bus7);
        buses.add(bus53);
        buses.add(bus53_2);
        buses.add(bus60);
        buses.add(bus60_2);
        buses.add(bus75);
        buses.add(bus75_2);
        buses.add(bus80);
        return buses;
    }

    private void clearProgressBar(){
        if (progressBar != null) {
            progressBar.clearAnimation();
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
    }


    private boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }

    private class GetTimetables extends AsyncTask<Void, Void, Void> {
        private int minute= Calendar.getInstance().get(Calendar.MINUTE);
        private int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        private String dep="";
        private String dep_next_hour="";

        @Override
        protected Void doInBackground(Void... params) {
            String jsonStr="";

            // Making a request to url and getting response
            for(int o=0;o<10;o++) {
                switch (o) {
                    case 0:
                        lineNo = "4";
                        lineInfo = " Żołnierska -> Pomorzany";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line4);
                        break;
                    case 1:
                        lineNo = "5";
                        lineInfo = " Żołnierska -> Stocznia Szczecińska";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line5);
                        break;
                    case 2:
                        lineNo = "7";
                        lineInfo = " Żołnierska -> Basen Górniczy";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line7);
                        break;
                    case 3:
                        lineNo = "53";
                        lineInfo = " Klonowica Zajezdnia -> Stocznia Szczecińska";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line53);
                        break;
                    case 4:
                        lineNo = "53";
                        lineInfo = " Klonowica Zajezdnia -> Pomorzany Dobrzyńska";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line53_2);
                        break;
                    case 5:
                        lineNo = "60";
                        lineInfo = " Klonowica Zajezdnia -> Stocznia Szczecińska";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line60_2);
                        break;
                    case 6:
                        lineNo = "60";
                        lineInfo = " Żołnierska -> Cukrowa";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line60);
                        break;
                    case 7:
                        lineNo = "75";
                        lineInfo = " Klonowica Zajezdnia -> Dworzec Główny";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line75);
                        break;
                    case 8:
                        lineNo = "75";
                        lineInfo = " Klonowica Zajezdnia -> Krzekowo";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line75_2);
                        break;
                    case 9:
                        lineNo = "80";
                        lineInfo = " Klonowica Zajezdnia -> Rugiańska";
                        jsonStr = BusTimetable.getURLSource(HTTPLinks.line80);
                        break;
                }

                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        departures = jsonObj.getJSONArray(TAG_DEPARTURES);

                        // day recognize
                        int day_recognize = 0;
                        if (day == Calendar.SATURDAY)
                            day_recognize = 1;
                        else if (day == Calendar.SUNDAY)
                            day_recognize = 2;
                        else
                            day_recognize = 0;

                        // looping through All departures
                        for (int i = day_recognize; i < day_recognize + 1; i++) {
                            JSONObject c = departures.getJSONObject(i);

                            // Phone node is JSON Object
                            JSONObject departures2 = c.getJSONObject(TAG_DEPARTURES2);
                            depart = departures2.getString(TAG_HOUR).replaceAll("[a-zA-Z]+", "");
                            depart2 = departures2.getString(TAG_HOUR2).replaceAll("[a-zA-Z]+", "");

                            str = extractArray(depart);
                            str2 = extractArray(depart2);

                            // tmp hashmap for single departure
                            HashMap<String, String> departure = new HashMap<>();

                            // adding each child node to HashMap key => value
                            departure.put(TAG_LINE, lineNo);
                            departure.put(TAG_TYPE, lineInfo);

                            for (int k = 0; k < str.length; k++) {
                                if (minute <= Integer.parseInt(str[k])) {
                                    dep = dep + hour2 + ":" + str[k] + "  ";
                                }
                            }
                            /**
                             * Departures for next hour
                             */
                            for (int k = 0; k < str2.length; k++) {
                                dep_next_hour = dep_next_hour + Integer.toString(hour + 1) + ":" + str2[k] + "  ";
                            }

                            departure.put(TAG_HOUR, dep + dep_next_hour);

                            /**
                             * Cleaning befor next line
                             */
                            dep = "";
                            dep_next_hour = "";

                            departuresList.add(departure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            /**
             * Updating parsed JSON data into ListView
             * */
            if (isFragmentUIActive()) {
                ListAdapter adapter = new SimpleAdapter(getActivity(), departuresList,
                        R.layout.bus_timetable_layout, new String[]{TAG_D_LINE, TAG_TYPE,
                        TAG_HOUR}, new int[]{R.id.line, R.id.type, R.id.hour});

                clearProgressBar();
                setListAdapter(adapter);

            }
    }
}
    /**
     * Conversion String to String[]
     */
    protected String[] extractArray(final String str){
        final String strNoBrace = str.substring(1,str.length()-1);
        String[] tempResult = strNoBrace.split(",");

        if(tempResult==null) return null;
        String[] result = new String[tempResult.length];

        for(int i=0,size=tempResult.length;i<size;++i){
            String temp = tempResult[i];
            result[i] = temp.substring(1,temp.length()-1);
        }

        return result;
    }

    private static String getURLSource(String url) {
        String for_json;

        site = new HttpConnect(url);
        for_json = site.getPage();

        return for_json;
    }
}
