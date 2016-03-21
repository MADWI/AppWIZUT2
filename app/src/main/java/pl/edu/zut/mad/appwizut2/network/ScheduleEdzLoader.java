package pl.edu.zut.mad.appwizut2.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.zut.mad.appwizut2.models.Timetable;

/**
 * Helper class for loading schedule
 */
public class ScheduleEdzLoader extends BaseDataLoader<Timetable, ScheduleEdzLoader.RawData> {

    /** Log tag */
    private static final String TAG = "ScheduleEdzLoader";

    /**
     * Date format in which are dates stored in calendar
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd",java.util.Locale.US);

    private static final Pattern HOUR_PATTERN = Pattern.compile("(\\d\\d):(\\d\\d)");


    ScheduleEdzLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @Override
    protected String getCacheName() {
        return "ScheduleEdziekanat";
    }

    @Override
    protected RawData doDownload(RawData cachedData) throws IOException {
        return cachedData;
    }

    @Override
    protected Timetable parseData(RawData rawData) throws JSONException {
        JSONArray table = new JSONArray(rawData.mJsonScheduleAsString);

        GregorianCalendar forDay = null;
        List<Timetable.Hour> hoursInDay = new ArrayList<>();
        List<Timetable.Day> days = new ArrayList<>();

        // Start from 1 - skip header
        for (int i = 1; i < table.length(); i++) {
            JSONArray row = table.getJSONArray(i);
            if (row.length() == 1) {
                // Header
                // Finish currently built day
                if (forDay != null) {
                    days.add(new Timetable.Day(
                            forDay, hoursInDay.toArray(new Timetable.Hour[hoursInDay.size()])
                    ));
                    hoursInDay.clear();
                }

                // Start next day
                try {
                    forDay = new GregorianCalendar();
                    forDay.setTime(DATE_FORMAT.parse(row.getString(0)));
                } catch (ParseException e) {
                    throw new JSONException(e.toString());
                }
            } else if (row.length() == 10 && forDay != null) {
                // Actual row
                Matcher startHour = HOUR_PATTERN.matcher(row.getString(2));
                Matcher endHour = HOUR_PATTERN.matcher(row.getString(3));
                if (!startHour.find() || !endHour.find()) {
                    throw new JSONException("Unable to match hour");
                }
                Timetable.Hour hour = new Timetable.Hour(
                        row.getString(4), // name
                        row.getString(8), // type
                        row.getString(6), // room
                        row.getString(5), // teacher
                        null, // No information about group/parity
                        new Timetable.TimeRange(
                                Integer.parseInt(startHour.group(1)),
                                Integer.parseInt(startHour.group(2)),
                                Integer.parseInt(endHour.group(1)),
                                Integer.parseInt(endHour.group(2))
                        )
                );
                hoursInDay.add(hour);
            } else if (row.length() == 9 && forDay != null) {
                // Actual row in old, 9-column format
                Matcher startHour = HOUR_PATTERN.matcher(row.getString(1));
                Matcher endHour = HOUR_PATTERN.matcher(row.getString(2));
                if (!startHour.find() || !endHour.find()) {
                    throw new JSONException("Unable to match hour");
                }
                Timetable.Hour hour = new Timetable.Hour(
                        row.getString(3),
                        row.getString(7),
                        row.getString(5),
                        row.getString(4),
                        null, // No information about group/parity
                        new Timetable.TimeRange(
                                Integer.parseInt(startHour.group(1)),
                                Integer.parseInt(startHour.group(2)),
                                Integer.parseInt(endHour.group(1)),
                                Integer.parseInt(endHour.group(2))
                        )
                );
                hoursInDay.add(hour);
            } else {
                Log.e(TAG, "Unrecognized row");
            }
        }

        // Add last day to days
        days.add(new Timetable.Day(
                forDay, hoursInDay.toArray(new Timetable.Hour[hoursInDay.size()])
        ));

        // Build the Timetable object
        return new Timetable(days.toArray(new Timetable.Day[days.size()]));
    }

    public void setSourceTableJson(String tableJson) {
        RawData newData = new RawData();
        newData.mJsonScheduleAsString = tableJson;
        manuallySetCachedData(newData);
    }


    static class RawData implements Serializable {
        /**
         * Schedule that we currently have
         */
        String mJsonScheduleAsString;
    }

}
