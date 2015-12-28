package pl.edu.zut.mad.appwizut2.network;

import android.support.annotation.NonNull;

import pl.edu.zut.mad.appwizut2.utils.HTTPLinks;

/**
 * Loader for announcements feed
 */
public class AnnouncementsLoader extends FeedLoader {

    protected AnnouncementsLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @NonNull
    @Override
    protected String getFeedAddress() {
        return HTTPLinks.ANNOUNCEMENTS;
    }

    @Override
    protected String getCacheName() {
        return "AnnouncementsFeed";
    }
}
