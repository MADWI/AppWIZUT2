package pl.edu.zut.mad.appwizut2;


import android.os.Bundle;
import android.os.PersistableBundle;
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
import android.view.View;
import android.widget.Toast;


import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


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
    //        new DrawerFragmentItem(R.id.event_calendar, "cal", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[Calendar]")),
            new DrawerFragmentItem(R.id.about_us,       "abo", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[About]")),
            new DrawerFragmentItem(R.id.announcements,  "ann", PlaceholderFragment.class, PlaceholderFragment.makeArguments("[Announcements]"))
    };

    private static final String PREF_LAST_DRAWER_FRAGMENT = "last_selected_drawer_fragment";

    private CaldroidFragment caldroidFragment;
    //private CaldroidFragment dialogCaldroidFragment;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    private Bundle state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //caldroidFragment = new CaldroidFragment();

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
            showCalendar();
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
        }else if(id == R.id.event_calendar){
            showCalendar();
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

    private void showCalendar() {

        //TODO: fix rotate
        caldroidFragment = new CaldroidFragment();
        caldroidFragment.setCaldroidListener(listener);

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
            caldroidFragment.setBackgroundResourceForDates(hm);

            caldroidFragment.refreshView();
            caldroidFragment.setArguments(bundle);


        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.main_content, caldroidFragment);
        t.commit();

    }

    // CUSTOM FUNCTION FOR PARSING STRING TO DATA
    private Date ParseDate(String date_str)
    {
        Date dateStr = null;
        try {
            dateStr = formatter.parse(date_str);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dateStr;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }

    // Setup listener
    private CaldroidListener listener = new CaldroidListener() {
        @Override
        public void onSelectDate(Date date, View view) {
            Toast.makeText(getApplicationContext(), formatter.format(date),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            String text = "month: " + month + " year: " + year;
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(getApplicationContext(),
                    "Long click " + formatter.format(date), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onCaldroidViewCreated() {
            if (caldroidFragment.getLeftArrowButton() != null) {
                Toast.makeText(getApplicationContext(),
                        "Caldroid view is created", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
