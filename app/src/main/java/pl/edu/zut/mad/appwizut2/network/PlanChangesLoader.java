package pl.edu.zut.mad.appwizut2.network;

import android.support.annotation.NonNull;

/**
 * Loader for plan changes feed
 */
public class PlanChangesLoader extends FeedLoader {

    protected PlanChangesLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @NonNull
    @Override
    protected String getFeedAddress() {
        return HTTPLinks.PLAN_CHANGES;
    }

    @Override
    protected String getCacheName() {
        return "PlanChangesFeed";
    }
}
