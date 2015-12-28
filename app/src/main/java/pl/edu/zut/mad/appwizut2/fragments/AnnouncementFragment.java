package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.AnnouncementsLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.FeedLoader;

/**
 * Created by macko on 04.11.2015.
 */
public class AnnouncementFragment extends FeedFragment {

    @Override
    protected FeedLoader createLoader() {
        return DataLoadingManager.getInstance(getContext()).getLoader(AnnouncementsLoader.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_announcements);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}



