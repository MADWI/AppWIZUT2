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

    /** "This stop name -> Final stop name" */
    // TODO: Split into two fields?
    private final String mLineFromTo;


    public BusStop(String lineName, String lineFromTo, int idInApi) {
        mLineName = lineName;
        mLineFromTo = lineFromTo;
        mIdInApi = idInApi;
    }

    public int getIdInApi() {
        return mIdInApi;
    }

    public String getLineName() {
        return mLineName;
    }

    public String getFromTo() {
        return mLineFromTo;
    }



    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("idInApi", mIdInApi);
        json.put("lineName", mLineName);
        json.put("lineFromTo", mLineFromTo);
        return json;
    }

    public BusStop(JSONObject json) throws JSONException {
        mIdInApi = json.getInt("idInApi");
        mLineName = json.getString("lineName");
        mLineFromTo = json.getString("lineFromTo");
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
