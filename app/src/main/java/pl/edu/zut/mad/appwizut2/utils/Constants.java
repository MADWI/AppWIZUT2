package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;

import java.text.SimpleDateFormat;

import pl.edu.zut.mad.appwizut2.models.BusStop;

public class Constants {
    /**
     * Directory name inside {@link Context#getFilesDir()} from which files are provided with FileProvider
     */
    public static final String FILE_PROVIDER_FILE_PATH = "provided";

    /**
     * Authority of out FileProvider
     * @see #FILE_PROVIDER_FILE_PATH
     */
    public static final String FILE_PROVIDER_AUTHORITY = "pl.edu.zut.mad.appwizut2.files";

    /**
     * Default bus stops
     */
    public static final BusStop[] DEFAULT_BUS_STOPS = new BusStop[]{
            new BusStop("4", " Żołnierska", "Pomorzany", 226059),
            new BusStop("5", " Żołnierska", "Stocznia Szczecińska", 226112),
            new BusStop("7", " Żołnierska", "Basen Górniczy", 226179),
            new BusStop("53", " Klonowica Zajezdnia", "Stocznia Szczecińska", 226503),
            new BusStop("53", " Klonowica Zajezdnia", "Pomorzany Dobrzyńska", 230637),
            new BusStop("60", " Żołnierska", "Cukrowa", 226769),
            new BusStop("60", " Klonowica Zajezdnia", "Stocznia Szczecińska", 230967),
            new BusStop("75", " Klonowica Zajezdnia", "Dworzec Główny", 227431),
            new BusStop("75", " Klonowica Zajezdnia", "Krzekowo", 231628),
            new BusStop("80", " Klonowica Zajezdnia", "Rugiańska", 227604),
            new BusStop("105", " Klonowica Zajezdnia", "Dobra Osiedle", 292001)
    };

    /**
     * Number of upcoming departures displayed on bus timetable
     */
    public static final int DISPLAYED_DEPARTURES_COUNT = 7;


    /**
     * Name of preference for studies type
     *
     * "Stacjonarne" or "Niestacjonarne"
     */
    public static final String PREF_STUDIES_TYPE = "type";

    /**
     * Name of preference for group name
     *
     * eg. "I1-110"
     */
    public static final String PREF_GROUP = "group";

    /**
     * Source of data for Timetable
     *
     * "wizut" or "edziekanat"
     */
    public static final String PREF_TIMETABLE_DATA_SOURCE = "timetable_data_source";


    /** calendar - for format dates */
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault());
    public static final SimpleDateFormat REVERSED_FORMATTER = new SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
    public static final SimpleDateFormat FOR_EVENTS_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);

    /** calendar - key for bundle */
    public static final String CURRENT_CLICKED_DATE = "clicked_date";
    public static final String ARG_DATE = "argument_date";
}
