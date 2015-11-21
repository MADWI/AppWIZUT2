package pl.edu.zut.mad.appwizut2;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.HashMap;

import hirondelle.date4j.DateTime;

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

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.custom_cell, null);
        }

        int width = cellView.getWidth();
        int topPadding = cellView.getPaddingTop();
        int leftPadding = cellView.getPaddingLeft();
        int bottomPadding = cellView.getPaddingBottom();
        int rightPadding = cellView.getPaddingRight();

        LinearLayout eventsContainer = (LinearLayout) cellView.findViewById(R.id.events_container);
        TextView tv1 = (TextView) cellView.findViewById(R.id.tv1);

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
        }else {
            cellView.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg);
        }


        tv1.setText("" + dateTime.getDay());

        //TODO set dynamically width of date and events_container doesn't work...
        //tv1.setWidth(width / 4);
        //eventsContainer.setMinimumWidth( width/4 );
        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding);
        // Set custom color if required
        setCustomResources(dateTime, cellView, tv1);

        return cellView;
    }
}
