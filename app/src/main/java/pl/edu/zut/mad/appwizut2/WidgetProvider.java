package pl.edu.zut.mad.appwizut2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

import pl.edu.zut.mad.appwizut2.activities.MainActivity;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.PlanChangesLoader;
import pl.edu.zut.mad.appwizut2.network.ScheduleEdzLoader;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    /**
     * Array of classes from which data will be loaded for widget
     * and the results will be passed to corresponding array fields
     * in updateTheWidget loadersResults parameter
     */
    static final Class[] LOADER_CLASSES = new Class[]{
            ScheduleEdzLoader.class, // index 0
            PlanChangesLoader.class // index 1
    };

    /**
     * @param loadersResults See {@link #LOADER_CLASSES}, may be null if this is initial load without
     *                       data, but if not null always has same length as LOADER_CLASSES
     */
    static void updateTheWidget(Context context, AppWidgetManager appWidgetManager, Object[] loadersResults) {
        // Get results from loaders
        Timetable timetable;
        Timetable.Hour upcomingHour = null;
        List<ListItemContainer> changesInSchedule = null;

        if (loadersResults != null) {
            timetable = (Timetable) loadersResults[0];
            if (timetable != null) {
                upcomingHour = timetable.getUpcomingHour();
            }
            changesInSchedule = (List<ListItemContainer>) loadersResults[1];
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Fill data about schedule
        if (upcomingHour != null) {
            Timetable.TimeRange time = upcomingHour.getTime();

            views.setTextViewText(R.id.widget_upcoming_hour_time, time.fromHour + ":" + time.fromMinute);
            views.setTextViewText(R.id.widget_upcoming_hour_title, upcomingHour.getSubjectName());
            views.setTextViewText(R.id.widget_upcoming_hour_room, upcomingHour.getRoom());

            views.setViewVisibility(R.id.widget_upcoming_hour, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_upcoming_hour, View.GONE);
        }

        // Fill data about changes in schedule
        if (changesInSchedule != null && !changesInSchedule.isEmpty()) {
            ListItemContainer latestChange = changesInSchedule.get(0);
            String title = latestChange.getTitle();

            SpannableStringBuilder text = new SpannableStringBuilder();
            text
                    .append(title)
                    .append("\n\n")
                    .append(latestChange.getBody());

            // Make title bold
            text.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    0,
                    title.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Put in widget
            views.setTextViewText(R.id.widget_last_change, text);
        }

        // Hide loading indicator
        views.setViewVisibility(R.id.loading_indicator, View.INVISIBLE);

        // Set actions
        views.setOnClickPendingIntent(R.id.widget_mad_logo, PendingIntent.getActivity(
                context,
                0,
                new Intent(Intent.ACTION_VIEW, Uri.parse("http://mad.zut.edu.pl")),
                0
        ));
        views.setOnClickPendingIntent(R.id.widget_upcoming_hour, PendingIntent.getActivity(
                context,
                0,
                MainActivity.getIntentToOpenWithTab(context, MainActivity.TAB_TIMETABLE),
                0
        ));
        views.setOnClickPendingIntent(R.id.widget_last_change, PendingIntent.getActivity(
                context,
                0,
                MainActivity.getIntentToOpenWithTab(context, MainActivity.TAB_CHANGES_IN_SCHEDULE),
                0
        ));
        views.setOnClickPendingIntent(R.id.refresh_button, PendingIntent.getService(
                context,
                0,
                new Intent(
                        WidgetUpdateService.ACTION_REFRESH_BY_USER,
                        null,
                        context,
                        WidgetUpdateService.class
                ),
                0
        ));

        // Publish result
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProvider.class), views);
    }


    static void showWidgetLoading(Context context, AppWidgetManager appWidgetManager) {
        // Create RemoteViews showing loading indicator
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setViewVisibility(R.id.loading_indicator, View.VISIBLE);

        // Partially update app widget
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        showWidgetLoading(context, appWidgetManager);
        context.startService(
                new Intent(context, WidgetUpdateService.class)
        );
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

