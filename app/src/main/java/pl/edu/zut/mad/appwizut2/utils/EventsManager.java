package pl.edu.zut.mad.appwizut2.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import pl.edu.zut.mad.appwizut2.connections.HttpConnect;
import pl.edu.zut.mad.appwizut2.models.ListItemContainer;

/**
 * Created by Marcin on 2015-12-04.
 */
public class EventsManager {
    private static final String TAG_DATE = "created";
    private static final String TAG_ENTRY = "entry";
    private static List<ListItemContainer> currentData;

    EventsManager(){
        fetchEventData();
    }

    public int getEventsCountOnDay(String date){
        int count = 0;
        if(currentData!=null) {
            for (ListItemContainer item : currentData) {
                if (item.getDate().equals(date)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void fetchEventData(){
        new DownloadContentTask().execute();
    }

    private class DownloadContentTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

                HttpConnect connection = new HttpConnect(HTTPLinks.ANNOUNCEMENTS);
                String pageContent = connection.getPage();
                currentData = createItemList(pageContent);


            return null;
        }

    }

    private static List<ListItemContainer> createItemList(String pageContent) {
        List<ListItemContainer> itemList = new ArrayList<>();
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(TAG_ENTRY);

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                String date = item.getString(TAG_DATE).substring(0,10);
                date = date.replace("-", ".");
                listItemContainer.setDate(date);

                itemList.add(listItemContainer);
            }

        } catch (JSONException e) {
            Log.i("EventsManager: ","JSONException:" + e.getMessage());
            return null;
        }
        return itemList;
    }
}
