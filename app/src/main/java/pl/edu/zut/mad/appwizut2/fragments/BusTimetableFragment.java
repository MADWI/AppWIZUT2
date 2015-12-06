package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.utils.BusTimetable;

/**
 * Created by barto on 23/11/2015.
 */
public class BusTimetableFragment extends BusTimetable{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.initUI();
        View rootView = super.initView(inflater, container);
        return rootView;
    }
}
