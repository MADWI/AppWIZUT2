package pl.edu.zut.mad.appwizut2.network;

import android.support.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import pl.edu.zut.mad.appwizut2.fragments.FeedFragment;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;

/**
 * Base loader for JSON feeds
 *
 * As my data loader framework requires separate class for each data kind,
 * each feed is represented by different class
 */
public abstract class FeedLoader extends BaseDataLoader<List<ListItemContainer>, FeedLoader.RawData> {

    protected FeedLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    /**
     * Returns the address of JSON feed
     */
    @NonNull
    protected abstract String getFeedAddress();

    @Override
    protected RawData doDownload(RawData cachedData) throws IOException {
        // JSON feeds at www.wi.zut.edu.pl don't support If-Modified-Since
        // We just download them without cache validation
        return new RawData(new HttpConnect(getFeedAddress()).readAllAndClose());
    }

    @Override
    protected List<ListItemContainer> parseData(RawData rawData) throws JSONException {
        return FeedFragment.createItemList(rawData.feedJson);
    }

    static final class RawData implements Serializable {
        String feedJson;

        RawData(String feedJson) {
            this.feedJson = feedJson;
        }
    }

}
