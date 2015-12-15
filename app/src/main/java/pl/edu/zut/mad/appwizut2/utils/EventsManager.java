package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.network.HttpConnect;

/**
 * Created by Marcin on 2015-12-04.
 */
public class EventsManager {

    private static List<ListItemContainer> currentData;
    Context ctx;

    EventsManager(Context ctx){
        this.ctx = ctx;
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
        new DownloadContentTask().execute(HTTPLinks.ANNOUNCEMENTS);
    }

    private class DownloadContentTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                if (HttpConnect.isOnline(ctx)) {
                    HttpConnect connection = new HttpConnect(params[0]);
                    String pageContent = connection.getPage();
                    currentData = createItemList(pageContent);
                }
            }catch (Exception e){
                Log.i("EventsManagerException", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("EventsManager", "onPostExecute");
        }
    }

    private static List<ListItemContainer> createItemList(String pageContent) {
        List<ListItemContainer> itemList = new ArrayList<>();
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(Constans.TAG_ENTRY);

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                String date = item.getString(Constans.TAG_DATE).substring(0,10);
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
