package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.FeedLoader;
import pl.edu.zut.mad.appwizut2.network.PlanChangesLoader;

/**
 * Created by macko on 05.11.2015.
 */
public class PlanChangesFragment extends FeedFragment {

    @Override
    protected FeedLoader createLoader() {
        return DataLoadingManager.getInstance(getContext()).getLoader(PlanChangesLoader.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_plan_changes);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
