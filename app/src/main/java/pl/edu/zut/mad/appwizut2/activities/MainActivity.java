package pl.edu.zut.mad.appwizut2.activities;


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
import android.widget.Toast;


import pl.edu.zut.mad.appwizut2.fragments.CaldroidCustomFragment;
import pl.edu.zut.mad.appwizut2.fragments.PlaceholderFragment;
import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.fragments.TimetableFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
            new DrawerFragmentItem(R.id.plan_changes,   "chg", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[Changes]")),
            new DrawerFragmentItem(R.id.event_calendar, "cal", CaldroidCustomFragment.class),
            new DrawerFragmentItem(R.id.about_us,       "abo", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[About]")),
            new DrawerFragmentItem(R.id.announcements,  "ann", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[Announcements]")),
            new DrawerFragmentItem(R.id.public_transport, "tra", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[Public Transport]")),
            new DrawerFragmentItem(R.id.timetable, "tim", TimetableFragment.class)
    };

    private static final String PREF_LAST_DRAWER_FRAGMENT = "last_selected_drawer_fragment";

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Open recently used fragment
        if (savedInstanceState == null) {
            String lastItemName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LAST_DRAWER_FRAGMENT, null);
            DrawerFragmentItem item = findDrawerItemFragmentWithName(lastItemName);
            openFragment(item);
            navigationView.setCheckedItem(item.id);
        }
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
            // TODO: open settings
            Toast.makeText(this, "TODO: open settings", Toast.LENGTH_SHORT).show();
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

        // If we didn't found fragment that was recently selected return default one
        return DRAWER_FRAGMENTS[0];
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
