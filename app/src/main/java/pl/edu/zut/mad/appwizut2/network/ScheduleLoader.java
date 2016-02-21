package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.utils.Constants;
import pl.edu.zut.mad.appwizut2.utils.JsonUtils;

/**
 * Helper class for loading schedule
 */
public class ScheduleLoader extends BaseDataLoader<Timetable, ScheduleLoader.RawData> {

    /** Log tag */
    private static final String TAG = "ScheduleLoader";

    /**
     * Date format in which are dates stored in calendar
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy",java.util.Locale.US);


    ScheduleLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @Override
    protected String getCacheName() {
        return "Schedule";
    }

    /**
     * True if the version we have currently cached matches current settings
     */
    @Override
    protected boolean cacheIsValidForCurrentSettings(RawData cachedData) {
        return cachedData.mCachedForGroup.equals(getGroupFromSettings(getContext()));
    }

    @Override
    protected RawData doDownload(RawData cachedData) throws IOException {
        String group = getGroupFromSettings(getContext());
        if (group == null) {
            return null;
        }
        HttpConnect conn = new HttpConnect(String.format(HTTPLinks.PARSED_SCHEDULE_URL, group));
        if (cachedData != null) {
            conn.ifModifiedSince(cachedData.mLastModified);
        }
        if (!conn.isNotModified()) {
            RawData downloadedData = new RawData();
            downloadedData.mLastModified = conn.getLastModified();
            downloadedData.mJsonScheduleAsString = conn.readAllAndClose();
            downloadedData.mCachedForGroup = group;
            return downloadedData;
        } else {
            conn.close();
            return cachedData;
        }
    }

    @Override
    protected Timetable parseData(RawData rawData) throws JSONException {
        JSONObject json = new JSONObject(rawData.mJsonScheduleAsString);
        // Parse hours list
        JSONArray hoursJsonArray = json.getJSONArray("hours");
        int hoursCount = hoursJsonArray.length();
        Timetable.TimeRange[] ranges = new Timetable.TimeRange[hoursCount];
        for (int i = 0; i < hoursCount; i++) {
            JSONArray hourJsonArray = hoursJsonArray.getJSONArray(i);
            ranges[i] = new Timetable.TimeRange(
                    hourJsonArray.getJSONArray(0).getInt(0),
                    hourJsonArray.getJSONArray(0).getInt(1),
                    hourJsonArray.getJSONArray(1).getInt(0),
                    hourJsonArray.getJSONArray(1).getInt(1)
            );
        }

        // Parse days
        JSONArray daySchedulesJson = json.getJSONArray("days");
        int daysInSchedule = daySchedulesJson.length();
        Timetable.Day[] days = new Timetable.Day[daysInSchedule];
        List<Timetable.Hour> dayTasks = new ArrayList<>();
        for (int dayNr = 0; dayNr < daysInSchedule; dayNr++) {
            JSONObject dayScheduleJson = daySchedulesJson.getJSONObject(dayNr);
            JSONArray dayTasksJson = dayScheduleJson.getJSONArray("hours");

            dayTasks.clear();
            for (int hour = 0; hour < hoursCount; hour++) {
                JSONArray tasksInHour = dayTasksJson.getJSONArray(hour);
                for (int i = 0; i < tasksInHour.length(); i++) {
                    JSONObject taskJson = tasksInHour.getJSONObject(i);
                    dayTasks.add(new Timetable.Hour(
                            JsonUtils.optString(taskJson, "name"),
                            JsonUtils.optString(taskJson, "type"),
                            JsonUtils.optString(taskJson, "room"),
                            JsonUtils.optString(taskJson, "teacher"),
                            JsonUtils.optString(taskJson, "rawWG"),
                            ranges[hour]
                    ));
                }
            }

            Timetable.Hour[] tasksInDay = dayTasks.toArray(new Timetable.Hour[dayTasks.size()]);

            Date date;
            try {
                date = DATE_FORMAT.parse(dayScheduleJson.getString("rawDate"));
            } catch (ParseException e) {
                Log.e(TAG, "Unable to parse date", e);
                continue;
            }
            GregorianCalendar dateCal = new GregorianCalendar();
            dateCal.setTime(date);

            days[dayNr] = new Timetable.Day(dateCal, tasksInDay);
        }

        return new Timetable(days);
    }

    @Nullable
    static String getGroupFromSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String type = prefs.getString(Constants.PREF_STUDIES_TYPE, null);
        String group = prefs.getString(Constants.PREF_GROUP, null);

        if (type == null || group == null) {
            return null;
        }

        return type + "/" + group;
    }

    public boolean isConfigured() {
        return getGroupFromSettings(getContext()) != null;
    }

    static class RawData implements Serializable {
        /**
         * Group for which this cache is for
         */
        String mCachedForGroup;

        /**
         * Date the schedule was last modified
         */
        long mLastModified;

        /**
         * Schedule that we currently have
         */
        String mJsonScheduleAsString;
    }

}
