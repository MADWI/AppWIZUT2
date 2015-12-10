package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Helper class for loading schedule
 */
// TODO: Refactor all the Loaders/OfflineHandlers/Checkers; make some common superclass
public class ScheduleLoader extends BaseDataLoader<Timetable> {

    /** Log tag */
    private static final String TAG = "ScheduleLoader";

    /**
     * Value saved at beginning of cache file
     * to avoid loading cache meant for different app version
     */
    private static final int CACHE_VERSION = 1;

    /**
     * Group for which we're loading data
     * in format used in paths on server e.g. "Stacjonarne/I1-110"
     */
    private String mGroup;

    /**
     * Group for which we have data cached
     *
     * @see {@link #cachedVersionMatchesSettings()}
     */
    private String mHaveCachedForGroup;

    /**
     * Date the schedule was last modified
     */
    private long mLastModified;

    /**
     * Schedule that we currently use; may be from or
     */
    private String mJsonScheduleAsString;

    ScheduleLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
        mGroup = getGroupFromSettings(getContext());
    }

    @Override
    protected void onSettingsChanged() {
        String newGroup = getGroupFromSettings(getContext());
        if (newGroup != null && !newGroup.equals(mHaveCachedForGroup)) {
            mGroup = newGroup;
            requestRefresh();
        }
    }

    @Override
    protected String getCacheName() {
        return "Schedule";
    }

    /**
     * True if the version we have currently cached matches current settings
     */
    private boolean cachedVersionMatchesSettings() {
        return mHaveCachedForGroup != null && mHaveCachedForGroup.equals(mGroup);
    }

    @Override
    protected boolean doDownload(boolean skipCache) {
        String group = mGroup;
        HttpConnect conn = new HttpConnect(String.format(Constants.PARSED_SCHEDULE_URL, group));
        if (cachedVersionMatchesSettings() && !skipCache) {
            conn.ifModifiedSince(mLastModified);
        }
        try {
            if (!conn.isNotModified()) {
                mLastModified = conn.getLastModified();
                mJsonScheduleAsString = conn.readAllAndClose();
                mHaveCachedForGroup = group;
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to download data", e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadFromCache(File cacheFile) {
        DataInputStream cacheInput = null;
        try {
            cacheInput = new DataInputStream(new FileInputStream(cacheFile));

            // 1: Cache file version
            if (cacheInput.readInt() != CACHE_VERSION) {
                mHaveCachedForGroup = null;
                return false;
            }

            // 2: String indicating group for which the schedule is for
            mHaveCachedForGroup = cacheInput.readUTF();

            // 3: Last modified
            mLastModified = cacheInput.readLong();

            // 4: Actual cached data
            mJsonScheduleAsString = cacheInput.readUTF();

        } catch (Exception e) {
            // No cache, force load
            mHaveCachedForGroup = null;
            return false;
        } finally {
            IoUtils.closeQuietly(cacheInput);
        }
        return true;
    }

    @Override
    protected boolean saveToCache(File cacheFile) {
        DataOutputStream cacheOutput = null;
        try {
            cacheOutput = new DataOutputStream(new FileOutputStream(cacheFile));

            // 1: Cache version
            if (mHaveCachedForGroup == null || mJsonScheduleAsString == null) {
                cacheOutput.writeInt(0);
                return true;
            }
            cacheOutput.writeInt(CACHE_VERSION);

            // 2: String indicating group for which the schedule is for
            cacheOutput.writeUTF(mHaveCachedForGroup);

            // 3: Last modified
            cacheOutput.writeLong(mLastModified);

            // 4: Actual cached data
            cacheOutput.writeUTF(mJsonScheduleAsString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write cache", e);
            return false;
        } finally {
            IoUtils.closeQuietly(cacheOutput);
        }
        return true;
    }

    @Override
    protected Timetable getData() {
        if (mJsonScheduleAsString == null || !cachedVersionMatchesSettings()) {
            return null;
        }
        try {
            return parseSchedule(new JSONObject(mJsonScheduleAsString));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse schedule", e);
            return null;
        }
    }

    private Timetable parseSchedule(JSONObject json) {
        try {
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

        } catch (Exception e) {
            // TODO: handling
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    static String getGroupFromSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String type = prefs.getString(Constans.PREF_STUDIES_TYPE, null);
        String group = prefs.getString(Constans.PREF_GROUP, null);

        if (type == null || group == null) {
            return null;
        }

        return type + "/" + group;
    }

}
