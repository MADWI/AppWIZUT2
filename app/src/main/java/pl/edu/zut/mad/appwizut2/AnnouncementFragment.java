package pl.edu.zut.mad.appwizut2;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
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
 * Created by macko on 04.11.2015.
 */
public class AnnouncementFragment extends Fragment {

    private static List<ListItemContainer> result;
    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "created";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "text";
    private static final String TAG_ENTRY = "entry";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.item_list, container, false);
        RecyclerView listItem = (RecyclerView) rootView.findViewById(R.id.itemList);
        listItem.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listItem.setLayoutManager(layoutManager);

        // only for development environment
        // change to AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HTTPConnect con = new HTTPConnect(HTTPLinks.ANNOUNCEMENTS);
        String content = con.getContent();

        if (content != null) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_ENTRY);
                result = new ArrayList<ListItemContainer>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    ListItemContainer announcement = new ListItemContainer();
                    JSONObject jAnnouncement = jsonArray.getJSONObject(i);
                    announcement.setTitle(jAnnouncement.getString(TAG_TITLE));
                    announcement.setDate(jAnnouncement.getString(TAG_DATE));
                    announcement.setAuthor(jAnnouncement.getString(TAG_AUTHOR));
                    announcement.setBody(jAnnouncement.getString(TAG_BODY));
                    result.add(announcement);
                }
                ListItemAdapter listItemAdapter = new ListItemAdapter(result);
                listItem.setAdapter(listItemAdapter);
            } catch (JSONException e) {
                Log.e("JSON", e.getMessage());
            }
        } else {
            Toast.makeText(getContext(), R.string.err_internet, Toast.LENGTH_SHORT).show();
            Log.e("Internet", "No internet connection");
        }

        return rootView;
    }
}
