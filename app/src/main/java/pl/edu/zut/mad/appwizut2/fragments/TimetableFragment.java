package pl.edu.zut.mad.appwizut2.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.edu.zut.mad.appwizut2.R;

/**
 * Created by Dawid on 19.11.2015.
 */
public class TimetableFragment extends Fragment {

    ViewPager vpager;

    TabLayout tabLayout;

    String[] days = {"PON", "WT", "ŚR", "CZW", "PT"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.timetable_layout, container, false);

        tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        vpager = (ViewPager) v.findViewById(R.id.pager);
        vpager.setAdapter(new TimetableAdapter(getContext()));
        tabLayout.setupWithViewPager(vpager);

        return v;
    }

    public class TimetableAdapter extends PagerAdapter
    {

        private Context ctx;
        public TimetableAdapter(Context ctx){
            this.ctx = ctx;
        }

        @Override
        public int getCount() {
            return days.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return days[position];
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView tView = new TextView(ctx);
            position++;
            tView.setText("Page number: " + position);
            tView.setTextColor(Color.RED);
            tView.setTextSize(20);
            container.addView(tView);
            return tView;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }
}
