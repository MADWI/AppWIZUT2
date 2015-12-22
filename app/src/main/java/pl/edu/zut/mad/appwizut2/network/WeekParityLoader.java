package pl.edu.zut.mad.appwizut2.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.DayParity;

/**
 * Data loader for week parity
 */
public class WeekParityLoader extends BaseDataLoader<List<DayParity>, WeekParityLoader.RawData> {

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
        String pageSource = rawData.mDataJson;
        ArrayList<DayParity> daysParityList = new ArrayList<DayParity>();

        /*Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);*/
        //GregorianCalendar today = new GregorianCalendar();
        String[] weekdays = new DateFormatSymbols().getWeekdays();

        JSONObject pageSrcObject = new JSONObject(pageSource);
        Iterator<String> dates = pageSrcObject.keys();

        while (dates.hasNext()) {

            // TODO: clean this up (Might need to move few things in DayParity)
            String tempDate = dates.next();
            int charAfterYear = tempDate.indexOf("_", 1);
            int charAfterMonth = tempDate.indexOf("_", charAfterYear + 1);

            int yearJSON = Integer.parseInt(tempDate.substring(1, charAfterYear));
            int monthJSON = Integer.parseInt(tempDate.substring(
                    charAfterYear + 1, charAfterMonth));
            int dayJSON = Integer.parseInt(tempDate.substring(charAfterMonth + 1,
                    tempDate.length()));
            GregorianCalendar dateJSON = new GregorianCalendar(yearJSON,
                    monthJSON - 1, dayJSON);

            String monthString;
            String dayString;

            if (monthJSON < 10)
                monthString = "0" + Integer.toString(monthJSON);
            else
                monthString = Integer.toString(monthJSON);

            if (dayJSON < 10)
                dayString = "0" + Integer.toString(dayJSON);
            else
                dayString = Integer.toString(dayJSON);

            String date = Integer.toString(yearJSON) + "." + monthString + "."
                    + dayString;

            //ilość wydarzeń w danym dniu
            //int eventsCount = mEventsManager.getEventsCountOnDay(date);

            String dayType = pageSrcObject.getString(tempDate);
            if (dayType.equals("x"))
                dayType = "---";
            else if (dayType.equals("p"))
                dayType = "parzysty";
            else if (dayType.equals("n"))
                dayType = "nieparzysty";
            else
                dayType = "?";

            String dayOfTheWeek = weekdays[dateJSON.get(Calendar.DAY_OF_WEEK)];
            daysParityList.add(new DayParity(date, dayType, dayOfTheWeek,
                    dateJSON));

        }

        Collections.sort(daysParityList, new DayParity.CustomComparator());
        return daysParityList;

    }

    static final class RawData implements Serializable {
        long mLastModified;
        String mDataJson;
    }
}
