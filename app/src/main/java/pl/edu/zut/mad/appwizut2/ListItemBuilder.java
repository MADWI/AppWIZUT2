package pl.edu.zut.mad.appwizut2;

/**
 * Created by macko on 05.11.2015.
 */

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which create list of items
 * from content given as a paremeter
 */

public class ListItemBuilder {

    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "created";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "text";
    private static final String TAG_ENTRY = "entry";

    boolean createListItem(String content, RecyclerView listItemView) {
        try {
            JSONObject jsonPageContent = new JSONObject(content);
            JSONArray arrayContent = jsonPageContent.getJSONArray(TAG_ENTRY);
            List<ListItemContainer> result = new ArrayList<>();

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer announcement = new ListItemContainer();
                JSONObject jAnnouncement = arrayContent.getJSONObject(i);
                announcement.setTitle(jAnnouncement.getString(TAG_TITLE));
                announcement.setDate(jAnnouncement.getString(TAG_DATE));
                announcement.setAuthor(jAnnouncement.getString(TAG_AUTHOR));
                announcement.setBody(jAnnouncement.getString(TAG_BODY));
                result.add(announcement);
            }
            ListItemAdapter listItemAdapter = new ListItemAdapter(result);
            listItemView.setAdapter(listItemAdapter);

        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
            return false;
        }
        return true;
    }
}
