package pl.edu.zut.mad.appwizut2.network;

/**
 * Created by barto on 28/11/2015.
 */
public class HTTPLinks {

    public final static String BUS = "https://bus.avris.it/api/";
    public final static String BUS_LINE_ID = BUS + "linia-%d";
    public final static String BUS_LINES = BUS + "linie";
    public final static String BUS_STOP_HOURS = BUS + "przystanek-%d";
    public final static String BUS_UPDATE_CHECK = BUS + "najnowsza-wersja";

    /**
     * Zmienna zawierajaca adres strony z danymi o parzystosci tygodnia w formacie
     * JSON
     */
    public static final String WEEK_PARITY = "http://wi.zut.edu.pl/components/com_kalendarztygodni/zapis.json";

    public static final String PLAN_CHANGES = "http://wi.zut.edu.pl/plan-zajec/zmiany-w-planie?format=json";
    public static final String ANNOUNCEMENTS = "http://wi.zut.edu.pl/ogloszenia?format=json";
    public static final String EVENTS = "http://wi.zut.edu.pl/kalendarz?format=json";
    public static final String NEWS = "http://wi.zut.edu.pl/aktualnosci?format=json";


    /**
     * Path where parsed schedules are downloaded from
     */
    public static final String PARSED_SCHEDULE_URL = "http://bm29640.zut.edu.pl/parsed-schedules/PlanGrup/%s.json";

    /**
     * Path where pdf schedules are downloaded from
     */
    public static final String PDF_SCHEDULE_URL = "http://wi.zut.edu.pl/plan/Wydruki/PlanGrup/%s.pdf";
}