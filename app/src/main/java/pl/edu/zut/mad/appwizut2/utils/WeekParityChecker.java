package pl.edu.zut.mad.appwizut2.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.models.DayParity;

/**
 * Klasa sprawdzajaca nieparzystosc/parzystosc dni tygodnia
 *
 */
public class WeekParityChecker {

    EventsManager mEventsManager = new EventsManager();

    /** Obiekt klasy HttpConnect, sluzacy do polaczenia ze strona */
    private static HttpConnect strona = null;

    /**
     * Zmienna zawierajaca adres strony z danymi o parzystosci tygodnia w formacie
     * JSON
     */
    private static String ZUT_WI_JSON = "http://wi.zut.edu.pl/components/com_kalendarztygodni/zapis.json";

    /**
     * Zmienna do debuggowania.
     */
    private static final String TAG = "WeekParityChecker";

    /** Domyslny konstruktor klasy. */
    public WeekParityChecker() {
    }

    /**
     * Metoda zwraca tablice stringow, ktora mowi czy dzien obecny i nastepny jest
     * nieparzysty/parzysty.
     *
     * @return Tablica stringow mowiaca o nieparzystosci/parzystosci dnia obecnego
     *         i nastepnego.
     */
    public String[] getParity() {
        String pageSource = WeekParityChecker.getURLSource(ZUT_WI_JSON);

        String[] currentWeek = new String[2];

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        int day = c.get(Calendar.DAY_OF_MONTH);

        c.add(Calendar.DAY_OF_YEAR, 1);
        int dayNext = c.get(Calendar.DAY_OF_MONTH);

        String today = "_" + Integer.toString(year) + "_" + Integer.toString(month)
                + "_" + Integer.toString(day);
        String tomorrow = "_" + Integer.toString(year) + "_"
                + Integer.toString(month) + "_" + Integer.toString(dayNext);

        Log.d(TAG, today + " " + tomorrow);

        try {
            JSONObject pageSrcObject = new JSONObject(pageSource);

            if (pageSrcObject.has(today)) {
                currentWeek[0] = pageSrcObject.getString(today);
            } else
                currentWeek[0] = "?";

            if (pageSrcObject.has(tomorrow))
                currentWeek[1] = pageSrcObject.getString(tomorrow);
            else
                currentWeek[1] = "?";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 2; i++) {
            if (currentWeek[i].equals("x")) {
                Log.e(TAG, currentWeek[i]);
                currentWeek[i] = "---";
            } else if (currentWeek[i].equals("p"))
                currentWeek[i] = "parzysty";
            else if (currentWeek[i].equals("n"))
                currentWeek[i] = "nieparzysty";
            else
                currentWeek[i] = "?";
        }

        return currentWeek;
    }

    /**
     * Metoda zwraca HashMap ze wszystkimi informacjami o
     * nieparzystosci/parzystosci danego dnia tygodnia
     *
     * @return HashMap z informacjami o wszystkich dniach (ich
     *         nieparzystosci/parzystosci)
     *         + ilość wydarzeń w danym dniu
     */
    public ArrayList<DayParity> getAllParity() {

        String pageSource = WeekParityChecker.getURLSource(ZUT_WI_JSON);
        ArrayList<DayParity> daysParityList = new ArrayList<DayParity>();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        GregorianCalendar today = new GregorianCalendar(year, month - 1, day);
        String[] weekdays = new DateFormatSymbols().getWeekdays();

        try {
            JSONObject pageSrcObject = new JSONObject(pageSource);
            JSONArray dates = pageSrcObject.names();

            for (int i = 0; i < dates.length(); i++) {
                if (pageSrcObject.has(dates.get(i).toString())) {

                    String tempDate = dates.get(i).toString();
                    int charAfterYear = tempDate.indexOf("_", 1);
                    int charAfterMonth = tempDate.indexOf("_", charAfterYear + 1);

                    int yearJSON = Integer.parseInt(tempDate.substring(1, charAfterYear));
                    int monthJSON = Integer.parseInt(tempDate.substring(
                            charAfterYear + 1, charAfterMonth));
                    int dayJSON = Integer.parseInt(tempDate.substring(charAfterMonth + 1,
                            tempDate.length()));
                    GregorianCalendar dateJSON = new GregorianCalendar(yearJSON,
                            monthJSON - 1, dayJSON);

                    if (dateJSON.after(today) || dateJSON.equals(today)) {

                        String monthString = "";
                        String dayString = "";

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
                        int eventsCount = mEventsManager.getEventsCountOnDay(date);

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
                                eventsCount, dateJSON));
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.sort(daysParityList, new CustomComparator());
        return daysParityList;
    }

    public class CustomComparator implements Comparator<DayParity> {
        @Override
        public int compare(DayParity o1, DayParity o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }

    /**
     * Metoda zwraca zrodlo strony jako String.
     *
     * @param url
     *          zmienna zawierajaca adres strony do pobrania.
     * @return zrodlo strony jako zmienna typu String.
     */
    private static String getURLSource(String url) {
        String do_obrobki = "";

        strona = new HttpConnect(url);
        do_obrobki = strona.getPage();

        return do_obrobki;
    }

}
