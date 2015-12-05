package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;

/**
 * Helper class for loading schedule
 */
// TODO: Refactor all the Loaders/OfflineHandlers/Checkers; make some common superclass
public class ScheduleLoader {

    /** Log tag */
    private static final String TAG = "ScheduleLoader";

    private static final String PARSED_SCHEDULE_URL = "http://bm29640.zut.edu.pl/parsed-schedules/PlanGrup/%s.json";
    private static final String PDF_SCHEDULE_URL = "http://wi.zut.edu.pl/Wydruki/PlanGrup/%s.pdf";


    private Context mContext;

    public ScheduleLoader(Context context) {
        mContext = context.getApplicationContext(); // Don't leak activity
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


    public void getSchedule(final ScheduleLoadedListener callback) {
        final String group = "Stacjonarne/I1-110"; // TODO: load from settings


        new AsyncTask<Void, Void, Timetable>() {
            @Override
            protected Timetable doInBackground(Void... params) {
                File cacheFile = new File(mContext.getFilesDir(), "Schedule");

                String json = null;
                long lastModified = 0;
                boolean isCached;
                boolean isDownloaded = false;

                // Load cache and info
                DataInputStream cacheInput = null;
                try {
                    cacheInput = new DataInputStream(new FileInputStream(cacheFile));

                    // 1: String indicating group for which the schedule is for
                    isCached = group.equals(cacheInput.readUTF());
                    if (!isCached) {
                        throw new Exception(); // Skip cache load
                    }

                    // 2: Last modified
                    lastModified = cacheInput.readLong();

                    // 3: Actual cached data
                    json = cacheInput.readUTF();

                } catch (Exception e) {
                    // No cache, force load
                    isCached = false;
                } finally {
                    closeQuietly(cacheInput);
                }

                // Download data
                HttpConnect conn = new HttpConnect(String.format(PARSED_SCHEDULE_URL, group));
                if (isCached) {
                    conn.ifModifiedSince(lastModified);
                }
                try {
                    if (!conn.isNotModified()) {
                        lastModified = conn.getLastModified();
                        json = conn.readAllAndClose();
                        isDownloaded = true;
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Unable to download data", e);
                }

                // Save data to cache
                if (isDownloaded) {
                    DataOutputStream cacheOutput = null;
                    try {
                        cacheOutput = new DataOutputStream(new FileOutputStream(cacheFile));

                        // 1: String indicating group for which the schedule is for
                        cacheOutput.writeUTF(group);

                        // 2: Last modified
                        cacheOutput.writeLong(lastModified);

                        // 3: Actual cached data
                        cacheOutput.writeUTF(json);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to write cache", e);
                    } finally {
                        closeQuietly(cacheOutput);
                    }
                }

                try {
                    if (json != null) {
                        return parseSchedule(new JSONObject(json));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse schedule", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Timetable timetable) {
                callback.onScheduleLoaded(timetable);
            }
        }.execute();
    }

    public interface ScheduleLoadedListener {
        void onScheduleLoaded(Timetable timetable);
    }


    // TODO: Make public? (If so, then move to more appropriate class)
    // Based on Apache Commons
    private static void closeQuietly(Closeable input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) {
        }
    }
}
