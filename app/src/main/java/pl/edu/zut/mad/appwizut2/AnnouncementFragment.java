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

    private String pageContent;
    private RecyclerView listItem;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.item_list, container, false);
        listItem = (RecyclerView) rootView.findViewById(R.id.itemList);
        listItem.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        listItem.setLayoutManager(layoutManager);

        HTTPConnect con = new HTTPConnect(HTTPLinks.ANNOUNCEMENTS, getContext());
        pageContent = con.getContent();

        if (pageContent != null) {
            ListItemBuilder listItemBuilder = new ListItemBuilder();
            listItemBuilder.createListItem(pageContent, listItem);
        } else {
            Toast.makeText(getContext(), R.string.err_internet, Toast.LENGTH_SHORT).show();
            Log.e("Internet", "No internet connection");
        }
        return rootView;
    }
}



