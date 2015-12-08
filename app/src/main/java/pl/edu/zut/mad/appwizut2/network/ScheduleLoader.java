package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
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
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Helper class for loading schedule
 */
// TODO: Refactor all the Loaders/OfflineHandlers/Checkers; make some common superclass
public class ScheduleLoader extends BaseDataLoader<Timetable> {

    /** Log tag */
    private static final String TAG = "ScheduleLoader";

    /**
     * Group for which we're loading data
     * in format used in paths on server e.g. "Stacjonarne/I1-110"
     */
    private String mGroup;

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
        if (!newGroup.equals(mGroup)) {
            mLastModified = 0;
            requestRefresh();
        }
    }

    @Override
    protected String getCacheName() {
        return "Schedule";
    }

    @Override
    protected boolean doDownload(boolean skipCache) {
        HttpConnect conn = new HttpConnect(String.format(Constants.PARSED_SCHEDULE_URL, mGroup));
        if (!skipCache) {
            conn.ifModifiedSince(mLastModified);
        }
        try {
            if (!conn.isNotModified()) {
                mLastModified = conn.getLastModified();
                mJsonScheduleAsString = conn.readAllAndClose();
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

            // 1: String indicating group for which the schedule is for
            boolean isCached = mGroup.equals(cacheInput.readUTF());
            if (!isCached) {
                throw new Exception(); // Skip cache load
            }

            // 2: Last modified
            mLastModified = cacheInput.readLong();

            // 3: Actual cached data
            mJsonScheduleAsString = cacheInput.readUTF();

        } catch (Exception e) {
            // No cache, force load
            mLastModified = 0;
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

            // 1: String indicating group for which the schedule is for
            cacheOutput.writeUTF(mGroup);

            // 2: Last modified
            cacheOutput.writeLong(mLastModified);

            // 3: Actual cached data
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
        if (mJsonScheduleAsString == null) {
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

    static String getGroupFromSettings(Context context) {
        return  "Stacjonarne/I1-110"; // TODO: really load from settings
    }

}
