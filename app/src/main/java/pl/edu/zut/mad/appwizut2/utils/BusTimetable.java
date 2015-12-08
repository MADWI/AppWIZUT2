package pl.edu.zut.mad.appwizut2.utils;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.network.HTTPLinks;

/**
 * Created by Bartosz Kozajda on 23/11/2015.
 */
public class BusTimetable extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private ProgressDialog pDialog;
    private static HttpConnect site = null;
    private String lineInfo ="";
    private String lineNo = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    LayoutInflater inflater;
    ViewGroup container;


    // JSON Node names
    private static final String TAG_DEPARTURES = "departures";
    private static final String TAG_TYPE = "type";
    private static final String TAG_DEPARTURES2 = "departures";
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

    protected View initView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.bus_list_layout, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        return rootView;
    }

    public void onRefresh(){
        swipeRefreshLayout.setRefreshing(true);
        refreshList(inflater, container);
    }

    private void refreshList(LayoutInflater inflater, ViewGroup container) {
        initUI();
        swipeRefreshLayout.setRefreshing(false);
    }

    public void initUI() {
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hour2 = Integer.toString(hour);

        TAG_HOUR = hour2;
        TAG_HOUR2 = Integer.toString(hour + 1);

        departuresList = new ArrayList<HashMap<String, String>>();

        // Calling async task to get json
        new GetTimetables().execute();
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
                            HashMap<String, String> departure = new HashMap<String, String>();

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
            ListAdapter adapter = new SimpleAdapter(getActivity(), departuresList,
                    R.layout.bus_timetable_layout, new String[] { TAG_LINE, TAG_TYPE,
                    TAG_HOUR }, new int[] {R.id.line, R.id.type, R.id.hour });

            setListAdapter(adapter);
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
        String for_json = "";

        site = new HttpConnect(url);
        for_json = site.getPage();

        return for_json;
    }
}
