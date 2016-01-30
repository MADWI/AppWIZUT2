package pl.edu.zut.mad.appwizut2.network;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.ListItemContainer;

/**
 * Loader for events feed
 */
public class EventsLoader extends FeedLoader {

    /** Keys used in json feed entry */
    protected static final String ATTR_DATE = "when";

    protected EventsLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    @NonNull
    @Override
    protected String getFeedAddress() {
        return HTTPLinks.EVENTS;
    }

    @Override
    protected String getCacheName() {
        return "EventsFeed";
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

            // TODO: Make date field real Date
            entry.setDate(rawEntry.getString(ATTR_DATE));

            // Don't pass author, it's set to
            // "Kalendarz wydarzeń na WIZUT - Wydział Informatyki ZUT Szczecin"
            // which is not informative and breaks layout
            entry.setAuthor("");

            // TODO: Make id field real int or don't parseInt later
            entry.setId(String.valueOf(rawEntry.getString(ATTR_ID).hashCode()));

            entry.setBody(rawEntry.getString(ATTR_BODY));
            entries.add(entry);
        }

        return entries;
    }
}
