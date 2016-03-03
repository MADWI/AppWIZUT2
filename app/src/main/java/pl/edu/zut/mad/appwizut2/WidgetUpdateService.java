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

    private boolean mIsLoading = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if we're already in update process
        if (mIsLoading) {
            return START_NOT_STICKY;
        }
        mIsLoading = true;

        // Load required data
        DataLoadingManager
                .getInstance(this)
                .multipleOneshotLoad(WidgetProvider.LOADER_CLASSES, this);
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
