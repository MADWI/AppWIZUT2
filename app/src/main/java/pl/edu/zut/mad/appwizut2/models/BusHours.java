package pl.edu.zut.mad.appwizut2.models;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import pl.edu.zut.mad.appwizut2.utils.DateUtils;

/**
 * Information about bus departure hours for bus stop
 */
public class BusHours {
    private static final String TAG = "BusHours";

    /**
     * The bus stop these hours are for
     */
    private final BusStop mForStop;

    /**
     * Map of "DAY TYPE" -> int[] { minutes in day of departures }
     *
     * @see #minuteNrToHhMmString(int)
     */
    private final Map<String, int[]> mHoursByDayType;

    public BusHours(BusStop forStop, Map<String, int[]> hoursByDayType) {
        mForStop = forStop;
        mHoursByDayType = hoursByDayType;
    }

    @Nullable
    private int[] getHoursForDayTypeSubstring(String dayTypeSubstring) {
        dayTypeSubstring = dayTypeSubstring.toUpperCase(Locale.ENGLISH);
        for (String dayType : mHoursByDayType.keySet()) {
            if (dayType.toUpperCase(Locale.ENGLISH).contains(dayTypeSubstring)) {
                return mHoursByDayType.get(dayTypeSubstring);
            }
        }
        return null;
    }

    /**
     * Get departure hours for given Bus for given day
     */
    @Nullable
    public int[] getHoursForDay(GregorianCalendar date) {
        int month = date.get(Calendar.MONTH);
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        int weekday = date.get(Calendar.DAY_OF_WEEK);

        int[] found;

        // New year
        if (month == Calendar.JANUARY && dayOfMonth == 1) {
            found = getHoursForDayTypeSubstring("NOWY ROK");
            if (found != null) return found;
        }

        if (month == Calendar.DECEMBER) {
            // Christmas
            if (dayOfMonth == 24 || dayOfMonth == 25) {
                found = getHoursForDayTypeSubstring("BOŻE NARODZENIE");
                if (found != null) return found;
            }

            // New Year's Eve
            if (dayOfMonth == 31) {
                found = getHoursForDayTypeSubstring("DNI ŚWIĄTECZNE");
                if (found != null) return found;
            }
        }

        // Easter
        if (weekday == Calendar.SUNDAY && (dayOfMonth + " " + month).equals(DateUtils.getEasterSundayDate(date.get(Calendar.YEAR)))) {
            found = getHoursForDayTypeSubstring("WIELKANOC");
            if (found != null) return found;
        }

        // Sundays
        if (weekday == Calendar.SUNDAY) {
            found = getHoursForDayTypeSubstring("DNI ŚWIĄTECZNE");
            if (found != null) return found;
        }

        // Saturdays
        if (weekday == Calendar.SATURDAY) {
            found = getHoursForDayTypeSubstring("SOBOTY");
            if (found != null) return found;
        }

        // Work days inside/outside students holiday
        boolean studentsHoliday = month >= Calendar.JULY && month <= Calendar.SEPTEMBER;
        if (studentsHoliday) {
            found = getHoursForDayTypeSubstring("DNI POWSZEDNIE W OKRESIE WAKACJI LETNICH I WE WRZEŚNIU");
            if (found != null) return found;
        } else {
            found = getHoursForDayTypeSubstring("DNI POWSZEDNIE Z WYJĄTKIEM WAKACJI LETNICH I WRZEŚNIA");
            if (found != null) return found;
        }

        // Work days
        found = getHoursForDayTypeSubstring("DNI POWSZEDNIE");
        if (found != null) return found;

        // Nothing above matched??
        // Map empty?
        if (mHoursByDayType.size() == 0) {
            Log.i(TAG, "Empty map of days for stop id=" + mForStop.getIdInApi() + " linename=" + mForStop.getLineName());
            return null;
        }

        // Return any from list
        Log.i(TAG, "Couldn't match day type for stop id=" + mForStop.getIdInApi() + " linename=" + mForStop.getLineName());
        return mHoursByDayType.values().iterator().next();
    }

    /**
     * Convert number of minutes to readable string
     *
     * eg. 65 -> "1:05"
     */
    public static String minuteNrToHhMmString(int minuteNr) {
        int hour = minuteNr / 60;
        int minute = minuteNr % 60;
        return String.format(Locale.US, "%d:%02d", hour, minute);
    }

    public BusStop getStop() {
        return mForStop;
    }
}
