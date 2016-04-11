package pl.edu.zut.mad.appwizut2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Helper service for asynchronously loading data for {@link WidgetProvider}
 */
public class WidgetUpdateService extends Service implements DataLoadingManager.MultipleOneshotLoadCallback {

    /**
     * Intent action indicating that widget update is initiated by user (as opposed to timer)
     */
    public static final String ACTION_REFRESH_BY_USER = "pl.edu.zut.mad.appwizut2.WIDGET_USER_REFRESH";

    /**
     * Maximum time between two consecutive refresh button presses to trigger full refresh
     */
    private static final int FULL_REFRESH_TRIGGER_TIME = 10000;

    /**
     * Time of last refresh request from user in {@link System#currentTimeMillis()}
     */
    private static long sLastRefreshRequest;

    private boolean mIsLoading = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if we're already in update process
        if (mIsLoading) {
            return START_NOT_STICKY;
        }
        mIsLoading = true;

        // Show loading indicator
        WidgetProvider.showWidgetLoading(this, AppWidgetManager.getInstance(this));

        // Check if we're requested to do full refresh
        boolean requestRefresh = false;
        if (ACTION_REFRESH_BY_USER.equals(intent.getAction())) {
            long currentTime = System.currentTimeMillis();
            requestRefresh = sLastRefreshRequest > currentTime - FULL_REFRESH_TRIGGER_TIME && sLastRefreshRequest < currentTime;
            sLastRefreshRequest = currentTime;
        }

        // Load required data
        DataLoadingManager
                .getInstance(this)
                .multipleOneshotLoad(WidgetProvider.LOADER_CLASSES, this, requestRefresh);
        return START_NOT_STICKY;
    }

    @Override
    public void onLoaded(Object[] results) {
        // We received result from all loaders
        // Pass the results to widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetUpdateService.this);
        WidgetProvider.updateTheWidget(
                this,
                appWidgetManager,
                results
        );

        // Schedule next update
        setupNextUpdate(this);

        // Stop this service
        mIsLoading = false;
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static void setupNextUpdate(Context context) {
        // Check if widget is added
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        boolean widgetEnabled = ids != null && ids.length != 0;

        // Get widget update interval from preferences
        int widgetUpdateInterval;
        if (widgetEnabled) {
            // This setting is stored as string because ListPreference uses strings instead of ints
            widgetUpdateInterval =
                    Integer.parseInt(
                            PreferenceManager
                                    .getDefaultSharedPreferences(context)
                                    .getString(
                                            Constants.PREF_WIDGET_UPDATE_INTERVAL,
                                            String.valueOf(Constants.DEFAULT_WIDGET_UPDATE_INTERVAL)
                                    )
                    );
        } else {
            widgetUpdateInterval = 0;
        }

        // Set or clear alarm
        PendingIntent alarmIntent = PendingIntent.getService(
                context,
                0,
                new Intent(context, WidgetUpdateService.class),
                0
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (widgetUpdateInterval > 0) {
            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + widgetUpdateInterval,
                    widgetUpdateInterval,
                    alarmIntent
            );
        } else {
            alarmManager.cancel(alarmIntent);
        }
    }
}
