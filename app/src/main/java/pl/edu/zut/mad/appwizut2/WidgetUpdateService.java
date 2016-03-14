package pl.edu.zut.mad.appwizut2;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;

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

        // Stop this service
        mIsLoading = false;
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
