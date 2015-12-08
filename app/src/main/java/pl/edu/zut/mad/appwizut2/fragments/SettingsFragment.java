package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import pl.edu.zut.mad.appwizut2.R;

/**
 * Created by Waldemar on 21.11.2015.
 *
 * Fragment for displaying settings
 *
 * Note: unlike other fragments this is not from support library,
 *       because this was simpliest way to match system settings with material themed activity
 */
public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_general);
    }
}
