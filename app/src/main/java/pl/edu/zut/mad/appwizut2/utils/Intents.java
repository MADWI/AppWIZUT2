package pl.edu.zut.mad.appwizut2.utils;

import java.io.File;

//import mad.widget.UpdateWidgetService;
//import mad.widget.activities.CalendarActivity;
//import mad.widget.activities.MyPrefs;
//import mad.widget.activities.PlanChangesActivity;

////import pl.edu.zut.mad.appwizut2.UpdateWidgetService;
import pl.edu.zut.mad.appwizut2.UpdateWidgetService;
import pl.edu.zut.mad.appwizut2.activities.SettingsActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

/**
 * Intencje uzyte w aplikacji
 *
 * @author Sebastian Swierczek
 */
public class Intents {

    /**
     * Intencja odswiezania widgetu
     *
     * @param context
     *          kontekst aplikacji
     *
     * @return intencja odswiezania widgetu
     */
    public static final Intent actionRefresh(final Context context) {
        final Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        return intent;
    }

    /**
     * Intencja zmian w planie
     *
     * @param context
     *          kontekst aplikacji
     * @return intencja zmian w planie
     */
//    public static final Intent actionPlanChanges(final Context context) {
//        final Intent intent = new Intent(context, PlanChangesActivity.class);
//        intent.setAction(Constans.ACTION_WIDGET_GET_PLAN_CHANGES);
//        return intent;
//
//    }

    /**
     * Intencja okna ustawien
     *
     * @param context
     *          kontekst aplikacji
     *
     * @return intencja okna ustawien
     */
    public static final Intent actionSettings(final Context context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        return intent;

    }

    /**
     * Intencja do otwierania aktywności kalendarza
     *
     * @param context
     *          kontekst aplikacji
     *
     * @return intencja okna kalendarza
     */
//    public static final Intent actionCalendar(final Context context) {
//        final Intent intent = new Intent(context, CalendarActivity.class);
//        return intent;
//
//    }

    /**
     * Intencja polaczenia ze strona SKN M. A. D.
     *
     * @param context
     *          kontekst aplikacji
     * @return intencja polaczenia ze strona SKN M. A. D.
     */
    public static final Intent actionWebpage(final Context context) {
        final Uri uri = Uri.parse("http://www.mad.zut.edu.pl");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return intent;

    }

    /**
     * Intencja otwierajaca plik .pdf z planem
     *
     * @param context
     *          kontekst aplikacji
     * @param grupa
     *          numer grupy wybranej przez uzytkownika
     * @return intencja otwierajaca plik .pdf z planem, gdy plan nie istnieje null
     */
    public static final Intent actionShowPlan(final Context context, String grupa) {
        File SDCardRoot = Environment.getExternalStorageDirectory();
        File file = new File(SDCardRoot + Constans.PLAN_FOLDER + "/" + grupa
                + ".pdf");

        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            return intent;
        } else
            return null;

    }

    public static PendingIntent createPendingActivity(final Context context,
                                                      final Intent intent) {
        return PendingIntent.getActivity(context, intent.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent createPendingService(final Context context,
                                                     final Intent intent) {
        return PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
