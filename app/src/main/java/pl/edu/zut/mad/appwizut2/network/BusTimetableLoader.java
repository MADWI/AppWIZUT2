package pl.edu.zut.mad.appwizut2.network;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import pl.edu.zut.mad.appwizut2.models.BusHours;
import pl.edu.zut.mad.appwizut2.models.BusStop;
import pl.edu.zut.mad.appwizut2.utils.SelectedBuses;

/**
 * Data loader for loading data about bus departures from bus.avris.it
 */
public class BusTimetableLoader extends BaseDataLoader<List<BusHours>, BusTimetableLoader.CachedData> {

    private static final String TAG = "BusTimetableLoader2";

    private static final boolean ENABLE_COMPRESSION = true;

    private static final String ATTR_UPDATE_ID = "id";
    private static final String ATTR_DEPARTURES = "departures";
    private static final String ATTR_DAY = "type";

    private static final Pattern MINUTE_PATTERN = Pattern.compile("\\D*(\\d+)\\D*");

    BusTimetableLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @Override
    protected String getCacheName() {
        return "BusTimetable";
    }

    private BusStop[] getSelectedBusStops() {
        return SelectedBuses.getBusStops(getContext());
    }

    /**
     * Download JSON as text from bus.avris.it api, handling their custom compression
     */
    public static String downloadFromAvrisApi(String url) throws IOException {
        if (ENABLE_COMPRESSION) {
            return new HttpConnect(url) {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new InflaterInputStream(super.getInputStream());
                }
            }.readAllAndClose();
        } else {
            return new HttpConnect(url + "?kompresja=brak").readAllAndClose();
        }
    }

    @Override
    protected CachedData doDownload(CachedData cachedData) throws IOException {
        // Read update id
        int updateId;
        try {
            updateId = new JSONObject(downloadFromAvrisApi(HTTPLinks.BUS_UPDATE_CHECK)).getInt(ATTR_UPDATE_ID);
        } catch (JSONException e) {
            throw new IOException(e);
        }
        if (updateId < 0) {
            updateId = 0;
        }

        // Download lines that don't match
        // TODO: Http pipelining
        boolean linesUpdated = false;
        CachedData newData = new CachedData();
        HashMap<Integer, CachedLine> cachedLines = cachedData != null ? cachedData.cachedLines : null;

        for (BusStop busStop : getSelectedBusStops()) {
            int idInApi = busStop.getIdInApi();
            try {
                CachedLine cachedLine = cachedLines != null ? cachedLines.get(idInApi) : null;
                if (cachedLine == null || cachedLine.updateId != updateId) {
                    cachedLine = new CachedLine(
                            updateId,
                            downloadFromAvrisApi(String.format(Locale.US, HTTPLinks.BUS_STOP_HOURS, idInApi))
                    );
                    linesUpdated = true;
                }
                newData.cachedLines.put(idInApi, cachedLine);
            } catch (IOException e) {
                Log.w(TAG, "Failed to download, stop id=" + idInApi, e);
            }
        }


        // Return result
        if (!linesUpdated) {
            return cachedData;
        }

        return newData;
    }

    @Override
    protected List<BusHours> parseData(CachedData cachedData) throws JSONException {
        ArrayList<BusHours> results = new ArrayList<>();

        HashMap<Integer, CachedLine> cachedLines = cachedData.cachedLines;

        // Iterate over stops
        for (BusStop busStop : getSelectedBusStops()) {
            int idInApi = busStop.getIdInApi();
            if (!cachedLines.containsKey(idInApi)) {
                Log.w(TAG, "Stop missing during parsing, stop id=" + idInApi);
                continue;
            }
            JSONObject object = new JSONObject(cachedLines.get(idInApi).data);
            JSONArray departuresByDayType = object.getJSONArray(ATTR_DEPARTURES);

            Map<String, int[]> hoursByDayType = new ArrayMap<>();

            // Iterate over day types
            for (int i = 0; i < departuresByDayType.length(); i++) {
                JSONObject departuresOnDayType = departuresByDayType.getJSONObject(i);
                Object rawDepaturesByHour = departuresOnDayType.get(ATTR_DEPARTURES);
                // Inner departures can be JSONObject {"hour": ["m#"]}
                // or empty JSONArray if results are not available
                if (!(rawDepaturesByHour instanceof JSONObject)) {
                    if (!(rawDepaturesByHour instanceof JSONArray) || ((JSONArray) rawDepaturesByHour).length() != 0) {
                        Log.w(TAG, "Inner departures has unexpected type for line id=" + idInApi);
                    }
                    continue;
                }
                JSONObject departuresByHour = (JSONObject) rawDepaturesByHour;

                List<Integer> minutesInDay = new LinkedList<>();

                // Iterate over hours
                for (int hour = 0; hour < 23; hour++) {
                    JSONArray minutes = departuresByHour.optJSONArray(String.valueOf(hour));
                    if (minutes == null) {
                        continue;
                    }
                    for (int j = 0; j < minutes.length(); j++) {
                        String minuteString = minutes.getString(j);
                        Matcher matcher = MINUTE_PATTERN.matcher(minuteString);
                        if (matcher.matches()) {
                            int minute = Integer.parseInt(matcher.group(1));
                            minutesInDay.add(hour * 60 + minute);
                        }
                    }
                }

                hoursByDayType.put(departuresOnDayType.getString(ATTR_DAY), toIntArray(minutesInDay));
            }
            results.add(new BusHours(busStop, hoursByDayType));
        }
        return results;
    }


    static class CachedData implements Serializable {
        final HashMap<Integer, CachedLine> cachedLines = new HashMap<>();
    }

    static class CachedLine implements Serializable {
        final int updateId;
        final String data;

        CachedLine(int updateId, String data) {
            this.updateId = updateId;
            this.data = data;
        }
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int index = 0;
        for (Integer integer : list) {
            array[index++] = integer;
        }
        return array;
    }
}
