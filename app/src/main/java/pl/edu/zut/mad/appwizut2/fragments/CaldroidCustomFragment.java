package pl.edu.zut.mad.appwizut2.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import pl.edu.zut.mad.appwizut2.R;

public class CaldroidCustomFragment extends CaldroidFragment {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initUI();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initUI(){
       // setCaldroidListener(listener);

        Bundle bundle = new Bundle();
        Calendar cal = Calendar.getInstance();
        bundle.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        bundle.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        bundle.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
        bundle.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

        // SETTING THE BACKGROUND
        // Create a hash map
        HashMap hm = new HashMap();
        // Put elements to the map
        //TODO: parsing Date from JSON instead hardcoding

        hm.put(ParseDate("2015/11/02"), R.color.uneven);
        hm.put(ParseDate("2015/11/03"), R.color.uneven);
        hm.put(ParseDate("2015/11/06"), R.color.uneven);
        hm.put(ParseDate("2015/11/12"), R.color.uneven);
        hm.put(ParseDate("2015/11/16"), R.color.uneven);
        hm.put(ParseDate("2015/11/17"), R.color.uneven);
        hm.put(ParseDate("2015/11/18"), R.color.uneven);
        hm.put(ParseDate("2015/11/20"), R.color.uneven);
        hm.put(ParseDate("2015/11/26"), R.color.uneven);
        hm.put(ParseDate("2015/11/30"), R.color.uneven);
        hm.put(ParseDate("2015/11/04"), R.color.even);
        hm.put(ParseDate("2015/11/05"), R.color.even);
        hm.put(ParseDate("2015/11/09"), R.color.even);
        hm.put(ParseDate("2015/11/10"), R.color.even);
        hm.put(ParseDate("2015/11/13"), R.color.even);
        hm.put(ParseDate("2015/11/19"), R.color.even);
        hm.put(ParseDate("2015/11/23"), R.color.even);
        hm.put(ParseDate("2015/11/24"), R.color.even);
        hm.put(ParseDate("2015/11/25"), R.color.even);
        hm.put(ParseDate("2015/11/27"), R.color.even);
        hm.put(ParseDate("2015/11/07"), R.color.days_off);
        hm.put(ParseDate("2015/11/08"), R.color.days_off);
        hm.put(ParseDate("2015/11/11"), R.color.days_off);
        hm.put(ParseDate("2015/11/14"), R.color.days_off);
        hm.put(ParseDate("2015/11/15"), R.color.days_off);
        hm.put(ParseDate("2015/11/21"), R.color.days_off);
        hm.put(ParseDate("2015/11/22"), R.color.days_off);
        hm.put(ParseDate("2015/11/28"), R.color.days_off);
        hm.put(ParseDate("2015/11/29"), R.color.days_off);

        // Uncomment this to customize startDayOfWeek
        // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
        // CaldroidFragment.TUESDAY); // Tuesday

        // Uncomment this line to use Caldroid in compact mode
        bundle.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

        // Uncomment this line to use dark theme
//            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
        setBackgroundResourceForDates(hm);

        refreshView();
      //  setArguments(bundle);


    }

   /* // Setup listener
    public CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            Toast.makeText(ctx.getApplicationContext(), FORMATTER.format(date),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            String text = "month: " + month + " year: " + year;
            Toast.makeText(ctx.getApplicationContext(), text, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(ctx.getApplicationContext(),
                    "Long click " + FORMATTER.format(date), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onCaldroidViewCreated() {
            if (getLeftArrowButton() != null) {
                Toast.makeText(ctx.getApplicationContext(),
                        "Caldroid view is created", Toast.LENGTH_SHORT).show();
            }
        }
    };*/

    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str)
    {
        Date dateStr = null;
        try {
            dateStr = FORMATTER.parse(date_str);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dateStr;
    }
}
