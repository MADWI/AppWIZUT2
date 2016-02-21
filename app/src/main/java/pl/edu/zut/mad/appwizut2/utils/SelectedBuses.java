package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import pl.edu.zut.mad.appwizut2.models.BusStop;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;

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

    /**
     * Add new bus stop to list
     *
     * Note: this doesn't trigger refresh,
     *       you'll need to call {@link BusTimetableLoader#requestRefresh()} yourself
     */
    public static boolean addBusStop(Context context, BusStop newBusStop) {
        // Read old stops
        BusStop[] oldBusStops = getBusStops(context);
        int oldLength = oldBusStops.length;

        // Check if line already exists on list
        if (findStopWithId(oldBusStops, newBusStop.getIdInApi()) != -1) {
            return false;
        }

        // Append to array
        BusStop[] busStops = new BusStop[oldLength + 1];
        System.arraycopy(oldBusStops, 0, busStops, 0, oldLength);
        busStops[oldLength] = newBusStop;

        // Put in settings
        saveBusStops(context, busStops);
        return true;
    }

    /**
     * Remove bus from list
     *
     * @param removedStopId {@link BusStop#getIdInApi()} of bus being removed
     */
    public static void removeBusStop(Context context, int removedStopId) {
        // Read old stops
        BusStop[] oldBusStops = getBusStops(context);
        int oldLength = oldBusStops.length;

        // Translate id to position
        int removeFromPosition = findStopWithId(oldBusStops, removedStopId);

        if (removeFromPosition == -1) {
            Log.e(TAG, "Cannot remove bus");
            return;
        }

        // Shrink array
        BusStop[] busStops = new BusStop[oldLength - 1];
        System.arraycopy(oldBusStops, 0, busStops, 0, removeFromPosition);
        System.arraycopy(oldBusStops, removeFromPosition + 1, busStops, removeFromPosition, oldLength - removeFromPosition - 1);

        // Put in settings
        saveBusStops(context, busStops);
    }

    /**
     * Helper class to save state of buses and restore it
     */
    public static class Reverter {
        private final SharedPreferences mPreferences;
        private final String mOriginalValue;

        /**
         * Create Reverter and save setting value
         */
        public Reverter(Context context) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            mOriginalValue = mPreferences.getString(PREF_BUSES, "");
        }

        /**
         * Revert setting to state at constructor call
         */
        public void revert() {
            mPreferences
                    .edit()
                    .putString(PREF_BUSES, mOriginalValue)
                    .apply();
        }
    }
}
