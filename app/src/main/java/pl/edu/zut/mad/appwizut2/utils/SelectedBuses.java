package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import pl.edu.zut.mad.appwizut2.models.BusStop;

/**
 * Class managing list of selected buses
 *
 * This is separate class to isolate data from loaded values
 * in order to avoid losing entries that failed to load into fragment from selected list
 */
public class SelectedBuses {
    private static final String TAG = "SelectedBuses";

    private static final String PREF_BUSES = "selected_bus_stops";

    /**
     * Get selected buses
     *
     * Returns default list if we can't load data from settings
     */
    public static BusStop[] getBusStops(Context context) {
        try {
            // Get from SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            JSONArray jsonArray = new JSONArray(prefs.getString(PREF_BUSES, ""));

            // Convert from JSONArray
            BusStop[] stops = new BusStop[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                stops[i] = new BusStop(jsonArray.getJSONObject(i));
            }
            return stops;
        } catch (JSONException e) {
            return Constants.DEFAULT_BUS_STOPS.clone();
        }
    }

    /**
     * Save array of bus stops in settings
     */
    private static void saveBusStops(Context context, BusStop[] busStops) {
        try {
            // Convert to JSONArray
            JSONArray jsonArray = new JSONArray();
            for (BusStop busStop : busStops) {
                jsonArray.put(busStop.toJsonObject());
            }

            // Save in SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs
                    .edit()
                    .putString(PREF_BUSES, jsonArray.toString())
                    .apply();
        } catch (JSONException e) {
            Log.e(TAG, "Cannot save buses", e);
        }
    }

    /**
     * Find stop with specified id in list
     *
     * Returns index in array or -1 if not found
     */
    private static int findStopWithId(BusStop[] busStops, int id) {
        for (int i = 0; i < busStops.length; i++) {
            if (busStops[i].getIdInApi() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Move bus in the list
     *
     * @param movedId {@link BusStop#getIdInApi()} of bus being moved
     * @param putInPlaceOfId {@link BusStop#getIdInApi()} of bus at position we should drop moved bus
     */
    public static void moveBusInList(Context context, int movedId, int putInPlaceOfId) {
        // Load bus stops
        BusStop[] busStops = getBusStops(context);

        // Translate ids to positions
        int takeFromPosition = findStopWithId(busStops, movedId);
        int putAtPosition = findStopWithId(busStops, putInPlaceOfId);

        if (takeFromPosition == -1 || putAtPosition == -1) {
            Log.e(TAG, "Cannot move bus");
            return;
        }

        // Move in array
        BusStop movedStop = busStops[takeFromPosition];
        if (takeFromPosition > putAtPosition) {
            // Shift remaining stops to upper indexes
            System.arraycopy(busStops, putAtPosition, busStops, putAtPosition + 1, takeFromPosition - putAtPosition);
        } else {
            // Shift remaining stops to lower indexes
            System.arraycopy(busStops, takeFromPosition + 1, busStops, takeFromPosition, putAtPosition - takeFromPosition);
        }
        busStops[putAtPosition] = movedStop;

        // Put in settings
        saveBusStops(context, busStops);
    }
}
