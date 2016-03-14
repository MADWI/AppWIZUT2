package pl.edu.zut.mad.appwizut2.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.fragments.AboutUsFragment;
import pl.edu.zut.mad.appwizut2.fragments.AnnouncementFragment;
import pl.edu.zut.mad.appwizut2.fragments.BusTimetableFragment;
import pl.edu.zut.mad.appwizut2.fragments.CaldroidCustomFragment;
import pl.edu.zut.mad.appwizut2.fragments.PlanChangesFragment;
import pl.edu.zut.mad.appwizut2.fragments.TimetableFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAB_CHANGES_IN_SCHEDULE = "chg";
    public static final String TAB_CALENDAR = "cal";
    public static final String TAB_TIMETABLE = "tim";
    public static final String TAB_ABOUT_US = "abo";
    public static final String TAB_ANNOUNCEMENTS = "ann";
    public static final String TAB_BUS_TIMETABLE = "tra";

    /**
     * Fragments selectable from drawer
     *
     * Arguments are:
     *   id - R.id.[...] matching name in 'menu/activity_main_drawer.xml'
     *   name - unique name used internally for remembering which fragment was recently open
     *   fragmentClass - class for fragment
     *   fragmentArguments - arguments for fragment (optional)
     *
     * Note: Drawer menu items that don't open fragment (e.g. ones opening activity)
     *       should not go here but to 'if (id == R.id.[...]'
     *       in {@link #onNavigationItemSelected(MenuItem)} method
     *       and should specify android:checkable="false" in xml
     */
    private static final DrawerFragmentItem[] DRAWER_FRAGMENTS = new DrawerFragmentItem[]{
            new DrawerFragmentItem(R.id.plan_changes, TAB_CHANGES_IN_SCHEDULE, PlanChangesFragment.class),
            new DrawerFragmentItem(R.id.event_calendar, TAB_CALENDAR, CaldroidCustomFragment.class),
            new DrawerFragmentItem(R.id.timetable, TAB_TIMETABLE, TimetableFragment.class),
            new DrawerFragmentItem(R.id.about_us, TAB_ABOUT_US, AboutUsFragment.class),
            new DrawerFragmentItem(R.id.announcements, TAB_ANNOUNCEMENTS, AnnouncementFragment.class),
            new DrawerFragmentItem(R.id.public_transport, TAB_BUS_TIMETABLE, BusTimetableFragment.class)
    };

    /**
     * Get Intent that can be used to open MainActivity and select the specified tab
     *
     * @param tab One of the TAB_* constants (e.g. {@link #TAB_TIMETABLE})
     */
    public static Intent getIntentToOpenWithTab(Context context, String tab) {
        return new Intent(
                ACTION_PREFIX_OPEN_FRAGMENT + tab,
                null,
                context,
                MainActivity.class
        );
    }

    /**
     * Action prefix for opening this activity and selecting fragment from drawer
     *
     * Note: As these actions are not registered in AndroidManifest.xml, they must be used with
     *       explicit Intent.
     *
     * @see #getIntentToOpenWithTab(Context, String)
     * @see #getDrawerItemFromIntent(Intent)
     */
    private static final String ACTION_PREFIX_OPEN_FRAGMENT = "pl.edu.zut.mad.appwizut2.OPEN_FRAGMENT.";

    private static final String PREF_LAST_DRAWER_FRAGMENT = "last_selected_drawer_fragment";
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Choose and open fragment
        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            // Choose fragment based on Intent
            DrawerFragmentItem item = getDrawerItemFromIntent(getIntent());

            // If above didn't picked anything, choose fragment based on settings
            if (item == null) {
                item = findDrawerItemFragmentWithName(prefs.getString("default_tab", null));
            }

            // If above didn't picked anything, choose fragment last selected fragment
            if (item == null) {
                item = findDrawerItemFragmentWithName(prefs.getString(PREF_LAST_DRAWER_FRAGMENT, null));
            }

            // If nothing above chosen anything, use default fragment
            if (item == null) {
                item = DRAWER_FRAGMENTS[0];
            }

            // Actually open the fragment
            openFragment(item);
            mNavigationView.setCheckedItem(item.id);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Open fragment based on Intent
        DrawerFragmentItem item = getDrawerItemFromIntent(intent);
        if (item != null) {
            openFragment(item);
            mNavigationView.setCheckedItem(item.id);
        }
    }

    private static DrawerFragmentItem getDrawerItemFromIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null && action.startsWith(ACTION_PREFIX_OPEN_FRAGMENT)) {
            return findDrawerItemFragmentWithName(action.substring(ACTION_PREFIX_OPEN_FRAGMENT.length()));
        }
        return null;
    }

    private void openFragment(DrawerFragmentItem item) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, item.createFragmentInstance())
                .commit();
    }

    private void rememberSelectedItem(DrawerFragmentItem item) {
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putString(PREF_LAST_DRAWER_FRAGMENT, item.name)
                .apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.event_calendar) {
            DrawerFragmentItem drawerFragmentItem = findDrawerItemFragmentWithId(id);
            if (drawerFragmentItem != null) {
                openFragment(drawerFragmentItem);
                rememberSelectedItem(drawerFragmentItem);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Only actions that don't open fragment go here
        // See note at DRAWER_FRAGMENTS above
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }else {
            DrawerFragmentItem drawerFragmentItem = findDrawerItemFragmentWithId(id);
            if (drawerFragmentItem != null) {
                openFragment(drawerFragmentItem);
                rememberSelectedItem(drawerFragmentItem);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private static DrawerFragmentItem findDrawerItemFragmentWithId(int id) {
        for (DrawerFragmentItem item : DRAWER_FRAGMENTS) {
            if (item.id == id) {
                return item;
            }
        }

        return null;
    }

    private static DrawerFragmentItem findDrawerItemFragmentWithName(String name) {
        for (DrawerFragmentItem item : DRAWER_FRAGMENTS) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        // If we didn't found fragment that was recently selected return null
        return null;
    }

    /**
     * See {@link #DRAWER_FRAGMENTS}
     */
    private static class DrawerFragmentItem {
        final int id;
        final String name;
        final Class<? extends Fragment> fragmentClass;
        final Bundle fragmentArguments;

        DrawerFragmentItem(int id, String name, Class<? extends Fragment> fragmentClass, Bundle fragmentArguments) {
            this.id = id;
            this.name = name;
            this.fragmentClass = fragmentClass;
            this.fragmentArguments = fragmentArguments;
        }

        DrawerFragmentItem(int id, String name, Class<? extends Fragment> fragmentClass) {
            this(id, name, fragmentClass, null);
        }

        Fragment createFragmentInstance() {
            try {
                Fragment fragment = fragmentClass.newInstance();
                fragment.setArguments(fragmentArguments);
                return fragment;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
