package pl.edu.zut.mad.appwizut2.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.zut.mad.appwizut2.models.DayParity;

/**
 * Data loader for week parity
 */
public class WeekParityLoader extends BaseDataLoader<List<DayParity>, WeekParityLoader.RawData> {

    private static final Pattern DATE_PATTERN = Pattern.compile("_(\\d+)_(\\d+)_(\\d+)");
    private static final int DATE_GROUP_YEAR = 1;
    private static final int DATE_GROUP_MONTH = 2;
    private static final int DATE_GROUP_DAY = 3;

    protected WeekParityLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @Override
    protected String getCacheName() {
        return "WeekParity";
    }

    @Override
    protected RawData doDownload(RawData cachedData) throws IOException {
        HttpConnect conn = new HttpConnect(HTTPLinks.WEEK_PARITY);
        if (cachedData != null) {
            conn.ifModifiedSince(cachedData.mLastModified);
        }
        if (conn.isNotModified()) {
            conn.close();
            return cachedData;
        }
        RawData newData = new RawData();
        newData.mLastModified = conn.getLastModified();
        newData.mDataJson = conn.readAllAndClose();
        return newData;
    }

    @Override
    protected List<DayParity> parseData(RawData rawData) throws JSONException {
        List<DayParity> daysParityList = new ArrayList<>();

        JSONObject pageSrcObject = new JSONObject(rawData.mDataJson);
        Iterator<String> dates = pageSrcObject.keys();

        while (dates.hasNext()) {

            // Parse date
            String rawDate = dates.next();

            Matcher matcher = DATE_PATTERN.matcher(rawDate);
            if (!matcher.matches()) {
                continue;
            }

            int yearJSON = Integer.parseInt(matcher.group(DATE_GROUP_YEAR));
            int monthJSON = Integer.parseInt(matcher.group(DATE_GROUP_MONTH));
            int dayJSON = Integer.parseInt(matcher.group(DATE_GROUP_DAY));

            GregorianCalendar date = new GregorianCalendar(yearJSON,
                    monthJSON - 1, dayJSON);

            // Parse parity
            String rawDayType = pageSrcObject.getString(rawDate);
            DayParity.Parity parity;
            if (rawDayType.equals("p")) {
                parity = DayParity.Parity.EVEN;
            } else if (rawDayType.equals("n")) {
                parity = DayParity.Parity.ODD;
            } else {
                continue;
            }

            // Add to list
            daysParityList.add(new DayParity(date, parity));
        }

        Collections.sort(daysParityList);
        return daysParityList;
    }

    static final class RawData implements Serializable {
        long mLastModified;
        String mDataJson;
    }
}
