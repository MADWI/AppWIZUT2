package pl.edu.zut.mad.appwizut2.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import pl.edu.zut.mad.appwizut2.CaldroidCustomAdapter;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.utils.WeekParityChecker;

public class CaldroidCustomFragment extends CaldroidFragment {

    private final WeekParityChecker checker = new WeekParityChecker();
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd");
    private static final SimpleDateFormat REVERSED_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    private static ArrayList<DayParity> parityList;
    private TextView clickedDate;


    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        // TODO Auto-generated method stub
        return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // michalbednarski: Hacky workaround for Caldroid's saved state mishandling
        if (savedInstanceState != null) {
            // Delete info for child fragment manager
            savedInstanceState.remove("android:support:fragments");
        }
        // Call super
        super.onCreate(savedInstanceState);

        if (parityList == null) {
            try {
                parityList = new AsyncTaskGetParityList().execute().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // TODO: We completely disabled Caldroid's state saving (since it's only causing problems)
        // Now we have to implement retaining currently selected month/year outselves
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: remove initUI method
        // (It shouldn't belong to onCreateView, this forces you to know day parity synchronously)
        initUI();

        // Get calendar view from superclass
        ViewGroup calendarView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        // Disable state saving on superclass'es view
        // This is workaround for Caldroid's improper handling of state saving
        for (int i = 0; i < calendarView.getChildCount(); i++) {
            calendarView.getChildAt(i).setSaveFromParentEnabled(false);
        }

        //setting toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_calendar);

        // Wrap calendarView into out fragment
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.calendar_layout, container, false);
        clickedDate = (TextView) wrapper.findViewById(R.id.dateTextView);
        ((ViewGroup) wrapper.findViewById(R.id.calendar_goes_here)).addView(calendarView, 0);

        return wrapper;
    }

    private void initUI() {

        // SETTING THE BACKGROUND
        // Create a hash map
        HashMap hm = new HashMap();
        // Put elements to the map

        if (parityList != null) {
            for (DayParity dayParities : parityList) {
                String parity = dayParities.getParity();
                if (parity.equals("parzysty")) {
                    hm.put(ParseDate(dayParities.getDate()), R.color.even);
                } else {
                    hm.put(ParseDate(dayParities.getDate()), R.color.uneven);
                }
            }
        }
        if (!hm.isEmpty()) {
            setBackgroundResourceForDates(hm);
        }
        setCaldroidListener(listener);
        refreshView();
    }

    @Override
    protected void retrieveInitialArgs() {
        setThemeResource(R.style.CaldroidCustomized);
        super.retrieveInitialArgs();
    }


    // Setup listener
    public CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            clickedDate.setText("Wydarzenia " + REVERSED_FORMATTER.format(date));
        }
    };


    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str) {
        Date dateStr = null;
        try {
            dateStr = FORMATTER.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }


    private class AsyncTaskGetParityList extends
            AsyncTask<Void, Void, ArrayList<DayParity>> {

        ArrayList<DayParity> tempArray = null;

        @Override
        protected ArrayList<DayParity> doInBackground(Void... params) {

            tempArray = checker.getAllParity();
            if (tempArray != null) {
                return tempArray;
            } else {
                Log.i(TAG, "Nie mozna pobrac");

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");

        }

        @Override
        protected void onPostExecute(ArrayList<DayParity> result) {
            Log.i(TAG, "onPostExecute");
            initUI();
        }

    }
}
