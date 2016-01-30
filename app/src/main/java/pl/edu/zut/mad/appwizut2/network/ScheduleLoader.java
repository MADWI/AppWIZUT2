package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Helper class for loading schedule
 */
// TODO: Refactor all the Loaders/OfflineHandlers/Checkers; make some common superclass
public class ScheduleLoader extends BaseDataLoader<Timetable, ScheduleLoader.RawData> {

    /** Log tag */
    private static final String TAG = "ScheduleLoader";

    /**
     * Value saved at beginning of cache file
     * to avoid loading cache meant for different app version
     */
    private static final int CACHE_VERSION = 1;


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
        HttpConnect conn = new HttpConnect(String.format(Constants.PARSED_SCHEDULE_URL, group));
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
        JSONArray weekdaySchedulesJson = json.getJSONArray("weekdaySchedules");
        Timetable.Hour[][] weekdaysSchedules = new Timetable.Hour[5][];
        List<Timetable.Hour> daySchedule = new ArrayList<>();
        for (int weekday = 0; weekday < 5; weekday++) {
            JSONArray dayScheduleJson = weekdaySchedulesJson.getJSONArray(weekday);
            daySchedule.clear();

            for (int hour = 0; hour < hoursCount; hour++) {
                JSONArray tasksInHour = dayScheduleJson.getJSONArray(hour);
                for (int i = 0; i < tasksInHour.length(); i++) {
                    JSONObject taskJson = tasksInHour.getJSONObject(i);
                    daySchedule.add(new Timetable.Hour(
                            taskJson.getString("name"),
                            taskJson.getString("type"),
                            taskJson.getString("room"),
                            taskJson.getString("teacher"),
                            taskJson.getString("rawWG"),
                            ranges[hour]
                    ));
                }
            }

            weekdaysSchedules[weekday] = daySchedule.toArray(new Timetable.Hour[daySchedule.size()]);
        }

        return new Timetable(weekdaysSchedules);
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
