package pl.edu.zut.mad.appwizut2.network;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.ListItemContainer;

/**
 * Base loader for JSON feeds
 *
 * As my data loader framework requires separate class for each data kind,
 * each feed is represented by different class
 */
public abstract class FeedLoader extends BaseDataLoader<List<ListItemContainer>, FeedLoader.RawData> {

    /**
     * Name of key in root object in json feed containing entries as array
     */
    private static final String ATTR_ENTRIES = "entry";

    /** Keys used in json feed entry */
    private static final String ATTR_DATE = "created";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_AUTHOR = "author";
    private static final String ATTR_BODY = "content";
    private static final String ATTR_ID = "id";

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
        List<ListItemContainer> entries = new ArrayList<>();
        JSONObject jsonPageContent = new JSONObject(rawData.feedJson);
        JSONArray rawEntries = jsonPageContent.getJSONArray(ATTR_ENTRIES);

        for (int i = 0; i < rawEntries.length(); i++) {
            JSONObject rawEntry = rawEntries.getJSONObject(i);
            ListItemContainer entry = new ListItemContainer();
            entry.setTitle(rawEntry.getString(ATTR_TITLE));
            entry.setDate(rawEntry.getString(ATTR_DATE));
            entry.setAuthor(rawEntry.getString(ATTR_AUTHOR));
            entry.setId(rawEntry.getString(ATTR_ID));
            entry.setBody(rawEntry.getString(ATTR_BODY));
            entries.add(entry);
        }

        return entries;
    }

    static final class RawData implements Serializable {
        String feedJson;

        RawData(String feedJson) {
            this.feedJson = feedJson;
        }
    }

}
