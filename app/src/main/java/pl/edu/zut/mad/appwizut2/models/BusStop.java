package pl.edu.zut.mad.appwizut2.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about bus stop that are choosen once and not updated on refresh
 * (that is, not hours when bus arrives)
 */
public class BusStop {

    /**
     * Id of stop as present in avris api
     */
    private final int mIdInApi;

    /** aka. line number */
    private final String mLineName;

    /** This stop name */
    private final String mStopName;

    /** Final stop name */
    private final String mDestinationName;


    public BusStop(String lineName, String stopName, String destinationName, int idInApi) {
        mLineName = lineName;
        mStopName = stopName;
        mDestinationName = destinationName;
        mIdInApi = idInApi;
    }

    public int getIdInApi() {
        return mIdInApi;
    }

    public String getLineName() {
        return mLineName;
    }

    public String getStopName() {
        return mStopName;
    }

    public String getFromTo() {
        return mStopName + " -> " + mDestinationName;
    }



    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("idInApi", mIdInApi);
        json.put("lineName", mLineName);
        json.put("stopName", mStopName);
        json.put("destinationName", mDestinationName);
        return json;
    }

    public BusStop(JSONObject json) throws JSONException {
        mIdInApi = json.getInt("idInApi");
        mLineName = json.getString("lineName");
        mStopName = json.getString("stopName");
        mDestinationName = json.getString("destinationName");
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof BusStop) && (((BusStop) o).mIdInApi == mIdInApi);
    }

    @Override
    public int hashCode() {
        return mIdInApi;
    }
}
