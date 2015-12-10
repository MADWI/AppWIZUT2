package pl.edu.zut.mad.appwizut2.utils;


import android.appwidget.AppWidgetManager;

/**
 * Klasa definiujaca stale uzyte w aplikacji
 *
 *
 * @author Sebastian Swierczek, Dawid Glinski
 */
public class Constans {

    /** Stala definiujaca nazwe pliku ustawien */
    public static final String PREFERENCES_NAME = "MAD_Widget_Preferences";

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

    /** Stala definiujaca maksymalna dlugosc tytulu zmiany w planie */
    public static final int MAX_TITLE_LENGTH = 30;


    /** Stała definiująca ostatnią datę pobrania dni dla danych offline w SharedPreferences */
    public static final String LAST_DAY_PARITY_UPDATE = "last_date_update";

    /** Stala definiujaca nazwe parzystosci tygodnia */
    public static final String WEEK_PARITY = "week_parity";

    /** Stala definiujaca nazwe parzystosci tygodnia nastepnego */
    public static final String WEEK_PARITY_NEXT = "week_parity_next";

    /** Stala definiujaca nazwe naglowka zmian w planie */
    public static final String TITLE_PLAN_CHANGES = "title_plan_changes";

    /** Stala definiujaca nazwe tresci zmian w planie */
    public static final String BODY_PLAN_CHANGES = "body_plan_changes";

    /** Stala definiujaca identyfikatory instancji widgetu */
    public static final String APP_WIDGET_IDS_TITLE = AppWidgetManager.EXTRA_APPWIDGET_IDS;

    /** Stala definiujaca nazwe akcji dla zmian w planie */
    public static final String ACTION_WIDGET_GET_PLAN_CHANGES = "PlanChangesAction";

    /** offline data */
    public static final String OFFLINE_DATA_FOLDER = "/Data";

}
