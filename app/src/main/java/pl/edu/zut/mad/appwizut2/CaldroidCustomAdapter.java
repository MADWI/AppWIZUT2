package pl.edu.zut.mad.appwizut2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Date;
import java.util.HashMap;

import hirondelle.date4j.DateTime;
import pl.edu.zut.mad.appwizut2.utils.Constants;
import pl.edu.zut.mad.appwizut2.views.EventsIndicatorView;

/**
 * Created by Marcin on 2015-11-16.
 */
public class CaldroidCustomAdapter extends CaldroidGridAdapter {

    public CaldroidCustomAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        HashMap events = (HashMap) extraData.get(Constants.EVENTS_COUNT_KEY);

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.custom_cell, null);
        }

        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        EventsIndicatorView eventsIndicator = (EventsIndicatorView) cellView.findViewById(R.id.events_indicator);
        TextView tv1 = (TextView) cellView.findViewById(R.id.tv1);

        // Example of setting event count
        //eventsIndicator.setLineCount(new Random().nextInt(7));


        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        Resources resources = context.getResources();

        tv1.setTextColor(resources.getColor(R.color.caldroid_white));

        // Set color of the dates in previous / next month
        if (dateTime.getMonth() != month) {
            tv1.setTextColor(resources
                    .getColor(com.caldroid.R.color.caldroid_darker_gray));
        }

        int day = dateTime.getDay();
        int month = dateTime.getMonth();
        int year = dateTime.getYear();

        // Match FOR_EVENTS_FORMATTER
        // TODO: clean up
        String strDate = year + "-";
        strDate += (month < 10) ? "0" + month + "-" : month + "-";
        strDate += (day < 10) ? "0" + day : day;


        if (events != null && events.containsKey(strDate)) {
            int count = (int )events.get(strDate);
            eventsIndicator.setLineCount(count);
        } else {
            eventsIndicator.setLineCount(0);
        }
        tv1.setText("" + dateTime.getDay());


        // Set custom color if required
        setCustomResources(dateTime, cellView, tv1);

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        // MB: Couldn't reproduce, but keeping it for now
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);

        return cellView;
    }

    /**
     * Set background for view, adding to it a border as we use for today date
     */
    private void setBackgroundResourceWithBorder(View view, int backgroundResource) {
        Resources resources = view.getContext().getResources();
        Drawable backgroundDrawable = resources.getDrawable(backgroundResource);

        // We only support wrapping ColorDrawable
        if (backgroundDrawable instanceof ColorDrawable) {
            // Load our base border drawable
            GradientDrawable
                    withBorderDrawable = (GradientDrawable) resources.getDrawable(R.drawable.today_date_border).mutate();

            // Set color in it
            withBorderDrawable.setColor(((ColorDrawable) backgroundDrawable).getColor());

            // Set that drawable as background
            // TODO: Replace with setBackground once we drop API 15 support
            // They seem to be the same thing, however
            view.setBackgroundDrawable(withBorderDrawable);
        } else {
            view.setBackgroundResource(backgroundResource);
        }
    }

    /**
     * Apply styles for this cell specified with e.g. {@link CaldroidFragment#setBackgroundResourceForDate(int, Date)}
     */
    @SuppressWarnings("unchecked")
    protected void setCustomResources(DateTime dateTime, View backgroundView,
                                      TextView textView) {
        // Get requested custom background resource (same way as original Caldroid implementation)
        HashMap<DateTime, Integer> backgroundForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData
                .get(CaldroidFragment._BACKGROUND_FOR_DATETIME_MAP);
        Integer backgroundResourceObj = null;

        if (backgroundForDateTimeMap != null) {
            // Get background resource for the dateTime
            backgroundResourceObj = backgroundForDateTimeMap.get(dateTime);
        }

        // Apply default value if we don't have any
        int backgroundResource = backgroundResourceObj != null ? backgroundResourceObj : R.color.calendar_default;

        // Set it, framing it if it's today
        if (dateTime.equals(getToday())) {
            setBackgroundResourceWithBorder(backgroundView, backgroundResource);
        } else {
            backgroundView.setBackgroundResource(backgroundResource);
        }

        // Setting custom text color is same as in superclass implementation
        // Not used for now but if we'd removed it we'd later could end puzzled why we cannot set it

        // Set custom text color
        HashMap<DateTime, Integer> textColorForDateTimeMap = (HashMap<DateTime, Integer>) caldroidData
                .get(CaldroidFragment._TEXT_COLOR_FOR_DATETIME_MAP);
        if (textColorForDateTimeMap != null) {
            // Get textColor for the dateTime
            Integer textColorResource = textColorForDateTimeMap.get(dateTime);

            // Set it
            if (textColorResource != null) {
                textView.setTextColor(resources.getColor(textColorResource));
            }
        }
    }
}
