package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.BusTimetableModel;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.Constants;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;
import pl.edu.zut.mad.appwizut2.utils.SharedPrefUtils;

/**
 * @author Damian Malarczyk
 * implementation based on first implementation of bus timetable data by Bartosz Kozajda
 */
public class BusTimetableLoader  {

    public ArrayList<BusTimetableModel> lineModels;

    private Context context;
    private OfflineHandler<BusTimetableModel> offlineHandler;

    // JSON Node names
    private static final String TAG_DEPARTURES = "departures";
    private static final String TAG_DAY = "type";

    private static final String TAG_LAST_UPDATE = "time";


    private static final String TAG_DEV_LOG = "bus_loader";


    /**
     *
     * @param lineModels list of BusTimeTableModels which client wants to load
     * @param context
     */
   public  BusTimetableLoader(@NonNull ArrayList<BusTimetableModel> lineModels,@NonNull Context context){
       this.lineModels = lineModels;
       this.context = context;
       this.offlineHandler = new OfflineHandler<>(context, OfflineHandler.OfflineDataHandlerKeys.BUS_TIMETABLE);
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public void getData(boolean offline,@NonNull BusTimetableLoaderCallback callback){
        new GetBusInfo().execute(callback);

    }

    public interface BusTimetableLoaderCallback {
        void foundData(ArrayList<BusTimetableModel> data);
    }


    private class GetBusInfo extends AsyncTask<BusTimetableLoaderCallback,Void,ArrayList<BusTimetableModel>> {
        @Override
        protected ArrayList<BusTimetableModel> doInBackground(@NonNull BusTimetableLoaderCallback... params) {
            if (!HttpConnect.isOnline(context)) {
                ArrayList<BusTimetableModel> currentData = offlineHandler.getCurrentData(false);
                params[0].foundData(currentData);
                return currentData;

            }

            SharedPreferences preferences = SharedPrefUtils.getSharedPreferences(context);

            String source = getURLSource(HTTPLinks.lineUpdate);
            String newUpdateDate;


            try {
                JSONObject jsonObj = new JSONObject(source);
                String lastOnlineUpdate = jsonObj.getString(TAG_LAST_UPDATE);
                if (lastOnlineUpdate.equals(preferences.getString(Constants.BUS_TIMETABLE_LASTUPDATE, ""))) {

                    ArrayList<BusTimetableModel> currentData = offlineHandler.getCurrentData(false);
                    params[0].foundData(currentData);
                    return currentData;

                } else {
                    newUpdateDate = lastOnlineUpdate;
                }
            } catch (JSONException e) {

                Log.e(TAG_DEV_LOG,"couldn't load bus timetable last update date");
                params[0].foundData(null);
                return null;

            }

            ArrayList<BusTimetableModel> newModels = new ArrayList<>();
            for (BusTimetableModel model : lineModels) {
                String jsonStr = BusTimetableLoader.getURLSource(model.getLineLink());
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        JSONArray departures = jsonObj.getJSONArray(TAG_DEPARTURES);


                        HashMap<String, HashMap<Integer, ArrayList<Integer>>> dayInfo = new HashMap<>();

                        for (int i = 0; i < departures.length(); i++) {
                            try {
                                JSONObject departureObject = departures.getJSONObject(i);
                                JSONObject departuresTime = departureObject.getJSONObject(TAG_DEPARTURES);
                                HashMap<Integer, ArrayList<Integer>> hourInfo = new HashMap<>();
                                for (int j = 0; j < 24; j++) {
                                    try {
                                        JSONArray depart = departuresTime.getJSONArray(Integer.toString(j));

                                        ArrayList<Integer> minutes = new ArrayList<>();
                                        for (int k = 0; k < depart.length(); k++) {

                                            minutes.add(Integer.parseInt(depart.getString(k).replaceAll("[a-zA-Z]+", "")));


                                        }
                                        hourInfo.put(j, minutes);

                                    } catch (JSONException e) {
                                        //Log.e(TAG_DEV_LOG,"no value for hour = " + j);
                                        //e.printStackTrace();
                                    }


                                }
                                dayInfo.put(departureObject.getString(TAG_DAY), hourInfo);
                            } catch (JSONException e) {
                                //not really an issue in most cases, throws error because sometimes departures for given type of day are empty
                                //in this case departure isn't of type JSONObject but empty JSONArray so the exception is thrown
                                //Log.e(TAG_DEV_LOG,"json inner parse error");
                                //e.printStackTrace();
                            }

                        }
                        BusTimetableModel newModel = model;
                        newModel.setInfo(dayInfo);
                        newModels.add(newModel);
                    } catch (JSONException e) {
                        Log.e(TAG_DEV_LOG,"json main parse error");
                        e.printStackTrace();
                        return null;
                    }
                    offlineHandler.setCurrentOfflineData(newModels);
                    offlineHandler.saveCurrentData();
                    preferences.edit().putString(Constants.BUS_TIMETABLE_LASTUPDATE, newUpdateDate).commit();
                } else {
                    Log.e(TAG_DEV_LOG, "Couldn't get any data from the url");
                }
            }


            params[0].foundData(newModels);
            return newModels;

        }


        @Override
        protected void onPostExecute(ArrayList<BusTimetableModel> busTimetableModels) {
            if (busTimetableModels != null) {
                lineModels = busTimetableModels;
            }
        }
    }


    private static String getURLSource(String url) {
        String for_json;

        HttpConnect site = new HttpConnect(url);

        try {
            for_json = site.readAllAndClose();
        } catch (IOException e) {
            Log.e(TAG_DEV_LOG,"couldn't load site content");
            return null;
        }

        return for_json;
    }



    private static final String weekDayType = "DNI POWSZEDNIE";
    private static final String weekDayStudentsYearType = "DNI POWSZEDNIE Z WYJĄTKIEM WAKACJI LETNICH I WRZEŚNIA";
    private static final String weekDayStudentsHolidayType = "DNI POWSZEDNIE W OKRESIE WAKACJI LETNICH I WE WRZEŚNIU";
    private static final String saturdayDayType = "SOBOTY";
    private static final String sundayDayType = "DNI ŚWIĄTECZNE";
    private static final String weekendDayType = saturdayDayType + " I " + sundayDayType;
    private static final String holidaysDayType = "WIELKANOC, BOŻE NARODZENIE I NOWY ROK";

    public enum BusModelDayType {
        WEEK_DAY(weekDayType),
        WEEK_DAY_STUDENTS_YEAR(weekDayStudentsYearType),
        WEEK_DAY_STUDENTS_HOLIDAY(weekDayStudentsHolidayType),
        SATURDAY(saturdayDayType),
        SUNDAY(sundayDayType),
        WEEKEND(weekDayType),
        HOLIDAYS(holidaysDayType),
        NOT_HANDLED("");


        private final String text;

        /**
         * @param text
         */
        BusModelDayType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public BusModelDayType stringToModelDayType(String value){
        switch (value){
            case weekDayType:
                return BusModelDayType.WEEK_DAY;
            case weekDayStudentsHolidayType:
                return BusModelDayType.WEEK_DAY_STUDENTS_HOLIDAY;
            case weekDayStudentsYearType:
                return BusModelDayType.WEEK_DAY_STUDENTS_YEAR;
            case saturdayDayType:
                return BusModelDayType.SATURDAY;
            case sundayDayType:
                return BusModelDayType.SUNDAY;
            case weekendDayType:
                return BusModelDayType.WEEKEND;
            case holidaysDayType:
                return BusModelDayType.HOLIDAYS;
            default:
                return BusModelDayType.NOT_HANDLED;
        }
    }
    //usage of algorithm to find Easter Sunday date found by Carl Friedrich Gauss
    //usage example from http://stackoverflow.com/questions/26022233/calculate-the-date-of-easter-sunday
    public static String getEasterSundayDate(int year)
    {
        int a = year % 19,
                b = year / 100,
                c = year % 100,
                d = b / 4,
                e = b % 4,
                g = (8 * b + 13) / 25,
                h = (19 * a + b - d - g + 15) % 30,
                j = c / 4,
                k = c % 4,
                m = (a + 11 * h) / 319,
                r = (2 * e + 2 * j - k - h + m + 32) % 7,
                n = (h - m + r + 90) / 25,
                p = (h - m + r + n + 19) % 32;


        //n - month, p - day

        return String.valueOf(n) + " " + String.valueOf(p);
    }


}
