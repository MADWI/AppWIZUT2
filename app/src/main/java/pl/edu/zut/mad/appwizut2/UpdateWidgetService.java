package pl.edu.zut.mad.appwizut2;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

//import pl.edu.zut.mad.appwizut2.models.MessagePlanChanges;
//import pl.edu.zut.mad.appwizut2.network.GetPlanChanges;
import pl.edu.zut.mad.appwizut2.utils.WeekParityChecker;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.Intents;
import pl.edu.zut.mad.appwizut2.utils.SharedPrefUtils;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;
import pl.edu.zut.mad.appwizut2.network.PlanDownloader;


/**
 * Usluga aktualizujaca widget
 *
 * @version 1.1.0
 */
public class UpdateWidgetService extends IntentService {

    /**
     * Zmienna do debuggowania
     */
    private static final String TAG = "UpdateWidgetService";

    /**
     * Obiekt klasy WeekParityChecker
     */
    private final WeekParityChecker checker = new WeekParityChecker();

    /**
     * Obiekt klasy GetPlanChanges
     */
    //private final GetPlanChanges PlanChanges = new GetPlanChanges();

    /**
     * Obiekt klasy MessagePlanChanges
     */
    //private MessagePlanChanges lastMessage = new MessagePlanChanges();

    // fields Strings

    /**
     * Zmienna przechowujaca informacje o grupie uzytkownika.
     */
    private String userGroup = " ";

    /**
     * Zmienna przechowujaca informacje o typie studiow uzytkownika.
     */
    private String userStudiesType = " ";

    /**
     * Domyslny konstruktor klasy
     */
    public UpdateWidgetService() {
        super("UpdateWidgetService");

    }

    /**
     * Metoda wywolywana kazdorazowo przy starcie uslugi
     *
     * @param intent  intencja ktora aktualizujemy
     * @param flags   dodatkowe dane o konkretnym wywolaniu
     * @param startId unikalny numer id reprezentujacy dane wywolanie
     * @return wartosc wskazujaca jak powinien zachowac sie system dla danego
     *         stanu wywolania
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStart");

        Log.i(TAG, "onStart ended");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());
//        ComponentName thisWidget = new ComponentName(this.getApplicationContext(),
//                MadWidgetProvider.class.getName());

//        // remote view to show progress bar
//        RemoteViews remoteViewsStarting = new RemoteViews(this
//                .getApplicationContext().getPackageName(), R.layout.widget_layout);

//        remoteViewsStarting.setViewVisibility(R.id.ProgressBarLayout, View.VISIBLE);
//        // finally update widget by RemoteView
//        appWidgetManager.updateAppWidget(thisWidget, remoteViewsStarting);

        // get SharedPreferences
        SharedPreferences ustawienia = SharedPrefUtils.getSharedPreferences(this
                .getApplicationContext());

        // get user group
        userGroup = ustawienia.getString(Constans.GROUP,
                getString(R.string.empty_group));

        // get user studies type
        userStudiesType = ustawienia.getString(Constans.TYPE, "");

        Log.d(TAG, "Grupa " + userGroup);
        Log.d(TAG, "Typ " + userStudiesType);

        // get week parity and plan changes
        String[] weekParity = null;

        try {
//           weekParity = getWeekParityAndPlanChanges();
        } catch (NullPointerException e) {

        }

//        // remote view to update widget layout
//        RemoteViews remoteViews = new RemoteViews(this.getApplicationContext()
//                .getPackageName(), R.layout.widget_layout);

//        // download plan if no exist and setonclick
//        downloadPlanIfNotExist(remoteViews);

        // set user group
//        remoteViews.setTextViewText(R.id.btnPobierzPlan, userGroup);

        // set week parity
//       if (weekParity != null)
//            setWeekParity(remoteViews, weekParity, ustawienia);

//        // show last plan change title
//        if (!lastMessage.getTitle().equals("")) {
//            remoteViews.setTextViewText(R.id.tv_zmiany_tytul, lastMessage.getTitle());
//            SharedPrefUtils.saveString(ustawienia, Constans.TITLE_PLAN_CHANGES,
//                    lastMessage.getTitle());
//        } else
//            remoteViews.setTextViewText(R.id.tv_zmiany_tytul,
//                    SharedPrefUtils.loadString(ustawienia, Constans.TITLE_PLAN_CHANGES));

        // set plan changes body
        // if there is no messages
//        if (lastMessage.getBody().equals(getString(R.string.no_messages))) {
//            remoteViews.setTextViewText(R.id.tv_zmiany_tresc, lastMessage.getBody());
//            // if messages downloaded successfull
//        } else if (!lastMessage.getBody().equals("")) {
//            String bodyLastMessage = lastMessage.getBody().substring(0, 65) + "...";
//            remoteViews.setTextViewText(R.id.tv_zmiany_tresc, bodyLastMessage);
//
//            SharedPrefUtils.saveString(ustawienia, Constans.BODY_PLAN_CHANGES,
//                    bodyLastMessage);
//            if (lastMessage.getBody().equals(getString(R.string.no_messages))) {
//                remoteViews.setTextViewText(R.id.tv_zmiany_tresc, bodyLastMessage);
//            }
//            // if have no Internet connectivity
//        } else {
//            remoteViews.setTextViewText(R.id.tv_zmiany_tresc,
//                    SharedPrefUtils.loadString(ustawienia, Constans.BODY_PLAN_CHANGES));
//        }

//        setOnClicks(remoteViews);

        // hide progress bar
//        remoteViews.setViewVisibility(R.id.ProgressBarLayout, View.INVISIBLE);

        // finally update widget by RemoteView
//        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }


//    private void downloadPlanIfNotExist(RemoteViews remoteViews) {
//        if (PlanDownloader.planExistsAndNew(this.getApplicationContext(),
//                userStudiesType, userGroup)) {
//            Log.d(TAG, "Plan istnieje na dysku");
//
//            // plan show onClick
//            Intent showPlanIntent = Intents.actionShowPlan(
//                    this.getApplicationContext(), userGroup);
//
//            PendingIntent pendingIntentPlan = Intents.createPendingActivity(
//                    this.getApplicationContext(), showPlanIntent);
//            remoteViews.setOnClickPendingIntent(R.id.btnPobierzPlan,
//                    pendingIntentPlan);
//        } else {
//            Log.d(TAG, "Plan nie isteniej�, pobieram...");
//            if (HttpConnect.isOnline(this.getApplicationContext())) {
//
//                if (PlanDownloader.downloadPlan(this.getApplicationContext(),
//                        userStudiesType, userGroup)) {
//
//                    // plan show onClick
//                    Intent showPlanIntent = Intents.actionShowPlan(
//                            this.getApplicationContext(), userGroup);
//
//                    PendingIntent pendingIntentPlan = Intents.createPendingActivity(
//                            this.getApplicationContext(), showPlanIntent);
//                    remoteViews.setOnClickPendingIntent(R.id.btnPobierzPlan,
//                            pendingIntentPlan);
//
//                    Log.d(TAG, "Pobrano plan");
//                } else {
//                    Toast.makeText(this.getApplicationContext(),
//                            this.getString(R.string.cannot_download_plan), Toast.LENGTH_LONG)
//                            .show();
//                    Log.d(TAG, "Niepobrano planu ");
//                }
//            }
//
//        }
//    }

//    private void setWeekParity(RemoteViews remoteViews, String[] weekParity, SharedPreferences ustawienia) {
//        if (weekParity != null && weekParity[0] != null) {
//            remoteViews.setTextViewText(R.id.tv_tydzien_current, weekParity[0]);
//            SharedPrefUtils.saveString(ustawienia, Constans.WEEK_PARITY,
//                    weekParity[0]);
//            Log.d(TAG, "Week parity 1 !=null");
//        } else {
//            remoteViews.setTextViewText(R.id.tv_tydzien_current,
//                    SharedPrefUtils.loadString(ustawienia, Constans.WEEK_PARITY));
//            Log.d(TAG, "Week parity 1 ==null");
//
//        }
//
//        // set next day week parity
//        if (weekParity != null && weekParity[1] != null) {
//            remoteViews.setTextViewText(R.id.tv_tydzien, weekParity[1]);
//            SharedPrefUtils.saveString(ustawienia, Constans.WEEK_PARITY_NEXT,
//                    weekParity[1]);
//            Log.d(TAG, "Week parity 2 !=null");
//        } else {
//            remoteViews.setTextViewText(R.id.tv_tydzien,
//                    SharedPrefUtils.loadString(ustawienia, Constans.WEEK_PARITY_NEXT));
//            Log.d(TAG, "Week parity 2 ==null");
//
//        }
//    }


//    private void setOnClicks(RemoteViews remoteViews) {
//        // refresh OnClick
//        Intent refreshIntent = Intents.actionRefresh(this.getApplicationContext());
//        PendingIntent pendingIntentRefresh = Intents.createPendingService(
//                this.getApplicationContext(), refreshIntent);
//        remoteViews.setOnClickPendingIntent(R.id.imb_odswiez, pendingIntentRefresh);
//
//        // planChanges OnClick
//        Intent planIntent = Intents.actionPlanChanges(this.getApplicationContext());
//        PendingIntent pendingIntentPlan = Intents.createPendingActivity(
//                this.getApplicationContext(), planIntent);
//        remoteViews.setOnClickPendingIntent(R.id.btZmianyPlanu, pendingIntentPlan);
//
//        // settings OnClick
//        Intent settingsIntent = Intents
//                .actionSettings(this.getApplicationContext());
//        PendingIntent pendingSettingsIntent = Intents.createPendingActivity(
//                this.getApplicationContext(), settingsIntent);
//        remoteViews.setOnClickPendingIntent(R.id.imb_ustawienia,
//                pendingSettingsIntent);
//
//        // calendar OnClick
//        Intent calendarIntent = Intents
//                .actionCalendar(this.getApplicationContext());
//        PendingIntent pendingCalendarIntent = Intents.createPendingActivity(
//                this.getApplicationContext(), calendarIntent);
//        remoteViews.setOnClickPendingIntent(R.id.btnPokazKalendarz,
//                pendingCalendarIntent);
//
//        // open webapge OnClick
//        Intent webpageIntent = Intents.actionWebpage(this.getApplicationContext());
//        PendingIntent webpageSettingsIntent = Intents.createPendingActivity(
//                this.getApplicationContext(), webpageIntent);
//        remoteViews.setOnClickPendingIntent(R.id.btnWeb, webpageSettingsIntent);
//    }

//    private String[] getWeekParityAndPlanChanges() throws NullPointerException {
//        if (HttpConnect.isOnline(this.getApplicationContext())) {
//
//
//            Log.i(TAG, "Getting week parity...");
//
//            String[] weekParity = checker.getParity();
//
//            Log.i(TAG, "Week parity = " + weekParity[0]);
//            Log.i(TAG, "Week parity next = " + weekParity[1]);
//
//            Log.i(TAG, "Getting last plan change...");
//            lastMessage = PlanChanges.getLastMessage();
//            if (lastMessage == null) {
//                lastMessage = new MessagePlanChanges();
//                lastMessage.setBody(getString(R.string.no_messages));
//
//            } else {
//                Log.i(TAG, "last plan change= " + lastMessage.getTitle() + "success");
//
//                String temp = lastMessage.getTitle();
//                temp = temp.substring(0, 1).toUpperCase()
//                        + temp.substring(1, temp.length());
//
//                if (temp.length() >= Constans.MAX_TITLE_LENGTH) {
//                    temp = temp.substring(0, Constans.MAX_TITLE_LENGTH) + "...";
//                    lastMessage.setTitle(temp);
//                } else
//                    lastMessage.setTitle(temp);
//
//                Spanned sp = Html.fromHtml(lastMessage.getBody().trim());
//                temp = sp.toString().replaceAll("[\r\n]{1,}$", "");
//                lastMessage.setBody(temp);
//
//            }
//            return weekParity;
//
//        }
//        return null;
//    }
}