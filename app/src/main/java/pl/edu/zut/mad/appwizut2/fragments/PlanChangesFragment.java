package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;

/**
 * Created by macko on 05.11.2015.
 */
public class PlanChangesFragment extends FeedFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setFeedUrl(HTTPLinks.PLAN_CHANGES);
        super.initModel(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = super.initView(inflater, container, getContext());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_plan_changes);
        return rootView;
    }
}
