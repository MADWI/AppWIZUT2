package pl.edu.zut.mad.appwizut2.activities;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.network.PlanDownloader;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


public class MyGroups extends Activity implements OnClickListener {

    /**
     * Zmienna do debuggowania.
     */
    private static final String TAG = "SettingActivity";

    /**
     * savedInstanceState entry name used storing contents of page 2 during group selection
     */
    private static final String STATE_AVAILABLE_GROUPS = "avGroups";

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

    /**
     * Grupy które mogą być wybrane z menu na drugiej stronie,
     * null jeśli nie przeszło się jeszcze na drugą stronę
     */
    private String[] mAvailableGroups = null;

    /** Metoda wywolywana przy starcie aktywnosci */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_preferences);
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
		 * get Resources
		 */
        res = getApplicationContext().getResources();

        if (savedInstanceState != null) {
            mAvailableGroups = savedInstanceState.getStringArray(STATE_AVAILABLE_GROUPS);
            if (mAvailableGroups != null) {
                switchToGroupSelection();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(STATE_AVAILABLE_GROUPS, mAvailableGroups);
    }

    /** Metoda wywolywana przez klikniecie w dane View */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNextPrefs:
                if (pick_studies.getVisibility() == View.VISIBLE) {
                    Resources res = getResources();
                    rodzaj = res.getStringArray(R.array.list_rodzaj_studiow_internal)[spinType.getSelectedItemPosition()];
                    kierunek = res.getStringArray(R.array.list_kierunek_studiow_internal)[spinDegree.getSelectedItemPosition()];
                    stopien = spinLevel.getSelectedItemPosition() + 1;
                    rok = spinYear.getSelectedItemPosition() + 1;

                    if (HttpConnect.isOnline(this.getApplicationContext())) {
                        downloadGroups = new AsyncTaskDownloadGroups();
                        downloadGroups.execute();
                    }
                } else {
                    // second click , save group/type and refresh layout

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

                    editor.putString(Constans.PREF_GROUP,
                            spinGroup.getSelectedItem().toString());

                    editor.putString(Constans.PREF_STUDIES_TYPE,
                            spinType.getSelectedItem().toString());

                    editor.apply();

                    DataLoadingManager.getInstance(this).dispatchSettingsChanged();

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
            AsyncTask<Void, Void, String[]> {


        /** Wykonywanie zadan w tle watku glownego */
        @Override
        protected String[] doInBackground(Void... params) {
            String[] tempGroups = null;

            tempGroups = PlanDownloader.getGroups(rodzaj, kierunek, stopien,
                    rok);

            return tempGroups;
        }

        /** Metoda wykonywana przed doInBackground() */
        @Override
        protected void onPreExecute() {
            progresDialog = ProgressDialog.show(MyGroups.this,
                    res.getString(R.string.download_groups_title),
                    res.getString(R.string.refreshing_body), true, true);
            progresDialog.setCancelable(false);

        }

        /** Metoda wykonywana po doInBackground() */
        @Override
        protected void onPostExecute(String[] result) {
            progresDialog.dismiss();

            if (result != null && result.length > 0) {
                mAvailableGroups = result;
                switchToGroupSelection();

            } else
                Toast.makeText(MyGroups.this, getString(R.string.cannot_download_groups), Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * Switch view to group selection (step 2)
     */
    private void switchToGroupSelection() {
        pick_studies.setVisibility(View.INVISIBLE);
        pic_group.setVisibility(View.VISIBLE);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                MyGroups.this, android.R.layout.simple_spinner_item, mAvailableGroups);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinGroup.setAdapter(spinnerArrayAdapter);
    }

}
