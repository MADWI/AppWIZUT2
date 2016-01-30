package pl.edu.zut.mad.appwizut2;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.HashMap;

import hirondelle.date4j.DateTime;
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

        HashMap events = (HashMap) extraData.get("EVENTS");

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


        if (!(dateTime.equals(getToday()))) {
            cellView.setBackgroundResource(R.color.calendar_default);
        } else {
            cellView.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg);
        }

        int day = dateTime.getDay();
        int month = dateTime.getMonth();
        int year = dateTime.getYear();

        // Match FOR_EVENTS_FORMATTER
        // TODO: clean up
        String strDate = year + "-";
        strDate += (month < 10) ? "0" + month + "-" : month + "-";
        strDate += (day < 10) ? "0" + day : day;


        if(events.containsKey(strDate)) {
            int count = (int )events.get(strDate);
            eventsIndicator.setLineCount(count);
        } else {
            eventsIndicator.setLineCount(0);
        }
        tv1.setText("" + dateTime.getDay());


        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);
        // Set custom color if required
        setCustomResources(dateTime, cellView, tv1);

        return cellView;
    }
}
