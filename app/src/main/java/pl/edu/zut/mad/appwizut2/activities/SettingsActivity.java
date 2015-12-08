package pl.edu.zut.mad.appwizut2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.LinearLayout;
import android.view.View;
import android.view.LayoutInflater;

import pl.edu.zut.mad.appwizut2.R;

import pl.edu.zut.mad.appwizut2.fragments.SettingsFragment;

/**
 * Created by Waldemar on 21.11.2015.
 *
 *  A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
//extends AppCompatActivity
public class SettingsActivity extends PreferenceActivity  {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Metoda wywolywana przy starcie aktywnosci */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_general);

//        if (savedInstanceState == null) {
//            getFragmentManager()
//                    .beginTransaction()
//                    .add(android.R.id.content, new SettingsFragment())
//                    .commit();
//        }

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Preference groups = (Preference) findPreference("groups");
        groups.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, MyGroups.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SettingsActivity.this.startActivity(intent);

                return true;
            }
        });

    }

//    /**
//     * Set up the {@link android.app.ActionBar}, if the API is available.
//     */
//        private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            // Show the Up button in the action bar.
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//    }

}
