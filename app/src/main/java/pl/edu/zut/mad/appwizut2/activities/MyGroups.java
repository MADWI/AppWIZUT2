package pl.edu.zut.mad.appwizut2.activities;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;
import pl.edu.zut.mad.appwizut2.utils.Intents;
import pl.edu.zut.mad.appwizut2.utils.SharedPrefUtils;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.network.PlanDownloader;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MyGroups extends Activity implements OnClickListener {

    /**
     * Zmienna do debuggowania.
     */
    private static final String TAG = "SettingActivity";

    /** Obiekt klasy SharedPreferences (plik ustawien) */
    private SharedPreferences preferences;

    /**
     * Obiekt klasy Resources, odwolujacy sie stringow w pliku
     * res/values/strings.xml
     */
    private Resources res;

    /** Obiekt w ktorym beda przechowywane grupy pobrane ze strony */
    private AsyncTaskDownloadGroups downloadGroups;

    // elementy widoku
    /** Obiekt klasy Button - przycisk Dalej */
    private Button next;

    /** Obiekt klasy Button - przycisk Anuluj */
    private Button cancel;

    /** Obiekt klasy Spinner - rodzaj studiow */
    private Spinner spinType;

    /** Obiekt klasy Spinner - kierunek studiow */
    private Spinner spinDegree;

    /** Obiekt klasy Spinner - stopien studiow */
    private Spinner spinLevel;

    /** Obiekt klasy Spinner - rok studiow */
    private Spinner spinYear;

    /** Obiekt klasy Spinner - grupa dziekanska */
    private Spinner spinGroup;

    /** Obiekt klasy ProgressDialog */
    private ProgressDialog progresDialog;

    /**
     * Obiekt klasy RelativeLayout - layout wyboru rodzaju, kierunku, stopnia i
     * roku studiow
     *
     */
    private RelativeLayout pick_studies;

    /** Obiekt klasy RelativeLayout - layout wyboru grupy dziekanskiej */
    private RelativeLayout pic_group;

    // selected items
    /** Zmienna w ktorej przechowywany jest rodzaj studiow */
    private String rodzaj;

    /** Zmienna w ktorej przechowywany jest kierunek studiow */
    private String kierunek;

    /** Zmienna w ktorej przechowywany jest stopie� studiow */
    private int stopien;

    /** Zmienna w ktorej przechowywany jest rok studiow */
    private int rok;

    /** Metoda wywolywana przy starcie aktywnosci */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_preferences);
        Log.i(TAG, "onCreate");
		/*
		 * Connect object with view elements
		 */
        next = (Button) findViewById(R.id.btnNextPrefs);
        next.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.btnCancelPrefs);
        cancel.setOnClickListener(this);

        spinType = (Spinner) findViewById(R.id.spinType);
        spinDegree = (Spinner) findViewById(R.id.spinDegree);
        spinLevel = (Spinner) findViewById(R.id.spinLevel);
        spinYear = (Spinner) findViewById(R.id.spinYear);
        spinGroup = (Spinner) findViewById(R.id.spinGroup);
        pick_studies = (RelativeLayout) findViewById(R.id.pick_studies_layout);
        pic_group = (RelativeLayout) findViewById(R.id.pic_group_layout);

		/*
		 * get SharedPreferences
		 */
        preferences = SharedPrefUtils
                .getSharedPreferences(getApplicationContext());
		/*
		 * get Resources
		 */
        res = getApplicationContext().getResources();

    }

    /** Metoda wywolywana przez klikniecie w dane View */
    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick");
        switch (v.getId()) {
            case R.id.btnNextPrefs:
                Log.i(TAG, "onClick button next");

                if (pick_studies.getVisibility() == View.VISIBLE) {
                    rodzaj = spinType.getSelectedItem().toString();
                    kierunek = spinDegree.getSelectedItem().toString();
                    stopien = spinLevel.getSelectedItemPosition() + 1;
                    rok = spinYear.getSelectedItemPosition() + 1;

                    Log.d(TAG,
                            rodzaj + " " + kierunek + " "
                                    + Integer.toString(stopien) + " "
                                    + Integer.toString(rok));
                    if (HttpConnect.isOnline(this.getApplicationContext())) {
                        downloadGroups = new AsyncTaskDownloadGroups();
                        downloadGroups.execute(this.getApplicationContext());
                    }
                } else {
                    // second click , save group/type and refresh layout

                    SharedPrefUtils.saveString(preferences, Constans.GROUP,
                            spinGroup.getSelectedItem().toString());

                    SharedPrefUtils.saveString(preferences, Constans.TYPE, spinType
                            .getSelectedItem().toString());

                    Intent refresh = Intents.actionRefresh(this);
                    startService(refresh);

                    finish();
                }

                break;
            case R.id.btnCancelPrefs:
                finish();
                break;

        }
    }

    /** Klasa realizujaca pobieranie grup dziekanskich ze strony */
    private class AsyncTaskDownloadGroups extends
            AsyncTask<Context, Void, String[]> {

        /** Obiekt klasy Context */
        private Context ctx;

        /** Wykonywanie zadan w tle watku glownego */
        @Override
        protected String[] doInBackground(Context... params) {
            Log.i(TAG, "doInBackground");
            ctx = params[0];

            String[] tempGroups = null;

            tempGroups = PlanDownloader.getGroups(rodzaj, kierunek, stopien,
                    rok);

            return tempGroups;
        }

        /** Metoda wykonywana przed doInBackground() */
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            progresDialog = ProgressDialog.show(MyGroups.this,
                    res.getString(R.string.download_groups_title),
                    res.getString(R.string.refreshing_body), true, true);
            progresDialog.setCancelable(false);

        }

        /** Metoda wykonywana po doInBackground() */
        @Override
        protected void onPostExecute(String[] result) {
            Log.i(TAG, "onPostExecute");
            progresDialog.dismiss();

            if (result != null && result.length > 0) {
                pick_studies.setVisibility(View.INVISIBLE);
                pic_group.setVisibility(View.VISIBLE);

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                        ctx, android.R.layout.simple_spinner_item, result);

                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinGroup.setAdapter(spinnerArrayAdapter);

            } else
                Toast.makeText(ctx,
                        ctx.getString(R.string.cannot_download_groups),
                        Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();

    }

}
