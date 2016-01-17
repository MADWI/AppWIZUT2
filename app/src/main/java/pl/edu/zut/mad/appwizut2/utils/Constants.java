package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;

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
     * Path where parsed schedules are downloaded from
     */
    public static final String PARSED_SCHEDULE_URL = "http://bm29640.zut.edu.pl/parsed-schedules/PlanGrup/%s.json";

    /**
     * Path where pdf schedules are downloaded from
     */
    public static final String PDF_SCHEDULE_URL = "http://wi.zut.edu.pl/plan/Wydruki/PlanGrup/%s.pdf";

    /**
     * Default bus stops
     */
    public static final BusStop[] DEFAULT_BUS_STOPS = new BusStop[]{
            new BusStop("4", " Żołnierska -> Pomorzany", 226059),
            new BusStop("5", " Żołnierska -> Stocznia Szczecińska", 226112),
            new BusStop("7", " Żołnierska -> Basen Górniczy", 226179),
            new BusStop("53", " Klonowica Zajezdnia -> Stocznia Szczecińska", 226503),
            new BusStop("53", " Klonowica Zajezdnia -> Pomorzany Dobrzyńska", 230637),
            new BusStop("60", " Żołnierska -> Cukrowa", 226769),
            new BusStop("60", " Klonowica Zajezdnia -> Stocznia Szczecińska", 230967),
            new BusStop("75", " Klonowica Zajezdnia -> Dworzec Główny", 227431),
            new BusStop("75", " Klonowica Zajezdnia -> Krzekowo", 231628),
            new BusStop("80", " Klonowica Zajezdnia -> Rugiańska", 227604)
    };

    /**
     * Number of upcoming departures displayed on bus timetable
     */
    public static final int DISPLAYED_DEPARTURES_COUNT = 5;
}
