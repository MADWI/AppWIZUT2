package pl.edu.zut.mad.appwizut2.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.utils.WeekParityChecker;

public class CaldroidCustomFragment extends CaldroidFragment {

    private final WeekParityChecker checker = new WeekParityChecker();
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy.MM.dd");
    private final ArrayList<DayParity> parityList;
    private static Bundle bundle;
    private boolean enableExecuteRefresh = true;

    public CaldroidCustomFragment() throws ExecutionException, InterruptedException {

        parityList = new AsyncTaskGetParityList().execute().get();

    }

    public static CaldroidCustomFragment newInstance() throws ExecutionException, InterruptedException {
        CaldroidCustomFragment fragment = new CaldroidCustomFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void initUI(){

        // TODO fix rotate

        bundle = new Bundle();
        Calendar cal = Calendar.getInstance();
        bundle.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        bundle.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        bundle.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
        bundle.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

        // SETTING THE BACKGROUND
        // Create a hash map
        HashMap hm = new HashMap();
        // Put elements to the map

        if (parityList != null) {
            for (DayParity dayParities : parityList) {
                String parity = dayParities.getParity();
                if (parity.equals("parzysty")) {
                    hm.put(ParseDate(dayParities.getDate()), R.color.even);
                } else{
                    hm.put(ParseDate(dayParities.getDate()), R.color.uneven);
                }
                //TODO: set days_off
            }
        }

        setBackgroundResourceForDates(hm);
        setCaldroidListener(listener);
        refreshView();
    }

    // Setup listener
    public CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            Toast.makeText(getActivity().getApplicationContext(), FORMATTER.format(date),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            String text = "month: " + month + " year: " + year;
            Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Long click " + FORMATTER.format(date), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onCaldroidViewCreated() {
            if (getLeftArrowButton() != null) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Caldroid view is created", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    public Date ParseDate(String date_str)
    {
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
            enableExecuteRefresh = false;

        }

        @Override
        protected void onPostExecute(ArrayList<DayParity> result) {
            Log.i(TAG, "onPostExecute");
            initUI();
            enableExecuteRefresh = true;
        }

    }
}
