package pl.edu.zut.mad.appwizut2.models;

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
}
