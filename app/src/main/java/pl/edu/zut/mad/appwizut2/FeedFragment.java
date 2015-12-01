package pl.edu.zut.mad.appwizut2;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by macko on 07.11.2015.
 */
public abstract class FeedFragment extends Fragment{

    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "created";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "text";
    private static final String TAG_ENTRY = "entry";

    private String pageContent;
    private RecyclerView itemListView;
    private String addressUrl;

    protected void setFeedUrl(String addressUrl) {
        this.addressUrl = addressUrl;
    }

    protected View initView(LayoutInflater inflater, ViewGroup container, Context context) {
        View rootView = inflater.inflate(R.layout.item_list, container, false);
        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);
      //  itemListView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        itemListView.setLayoutManager(layoutManager);

        HTTPConnect con = new HTTPConnect(addressUrl, context);
        pageContent = con.getContent();

        if (pageContent != null) {
            createItemList();
        } else {
            Toast.makeText(context, R.string.err_internet, Toast.LENGTH_SHORT).show();
            Log.e("Internet", "No internet connection");
        }
        return rootView;
    }

    protected boolean createItemList() {
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(TAG_ENTRY);
            List<ListItemContainer> result = new ArrayList<>();

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                listItemContainer.setTitle(item.getString(TAG_TITLE));
                listItemContainer.setDate(item.getString(TAG_DATE));
                listItemContainer.setAuthor(item.getString(TAG_AUTHOR));
                listItemContainer.setBody(item.getString(TAG_BODY));
                result.add(listItemContainer);
            }
            ListItemAdapter listItemAdapter = new ListItemAdapter(result);
            itemListView.setAdapter(listItemAdapter);
        } catch (JSONException e) {
            Log.e("JSON: ", e.getMessage());
            return false;
        }
        return true;
    }
}
