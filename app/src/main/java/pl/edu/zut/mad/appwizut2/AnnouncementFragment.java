package pl.edu.zut.mad.appwizut2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by macko on 04.11.2015.
 */
public class AnnouncementFragment extends FeedFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.setFeedUrl(HTTPLinks.ANNOUNCEMENTS);
        View rootView = super.initView(inflater, container, getContext());
        return rootView;
    }

}



