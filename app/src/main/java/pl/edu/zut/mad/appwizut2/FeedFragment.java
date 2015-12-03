package pl.edu.zut.mad.appwizut2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by macko on 07.11.2015.
 */
public abstract class FeedFragment extends Fragment{

    private static final String TAG_TITLE = "title";
    private static final String TAG_DATE = "created";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_BODY = "text";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_ID = "id";
    private String pageContent;
    private RecyclerView itemListView;
    private String addressUrl;
    private Context context;
    private static final Pattern patternQuot = Pattern.compile("(&quot;)");

    protected void setFeedUrl(String addressUrl) {
        this.addressUrl = addressUrl;
    }

    protected View initView(LayoutInflater inflater, ViewGroup container, Context context) {
        this.context = context;

        View rootView = inflater.inflate(R.layout.item_list, container, false);
        itemListView = (RecyclerView) rootView.findViewById(R.id.itemList);
        itemListView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        itemListView.setLayoutManager(layoutManager);

        DownloadContentTask task = new DownloadContentTask();
        task.execute(addressUrl);

        return rootView;
    }

    private List<ListItemContainer> createItemList(String pageContent) {
        List<ListItemContainer> itemList = new ArrayList<>();
        try {
            JSONObject jsonPageContent = new JSONObject(pageContent);
            JSONArray arrayContent = jsonPageContent.getJSONArray(TAG_ENTRY);

            for (int i = 0; i < arrayContent.length(); i++) {
                ListItemContainer listItemContainer = new ListItemContainer();
                JSONObject item = arrayContent.getJSONObject(i);
                listItemContainer.setTitle(item.getString(TAG_TITLE));
                listItemContainer.setDate(item.getString(TAG_DATE));
                listItemContainer.setAuthor(item.getString(TAG_AUTHOR));
                listItemContainer.setId(item.getString(TAG_ID));

                String body = item.getString(TAG_BODY);
                Matcher matcher = patternQuot.matcher(body);
                body = matcher.replaceAll("\"");

                listItemContainer.setBody(body);
                itemList.add(listItemContainer);
            }

        } catch (JSONException e) {
            Log.e("FeedFragment: ","JSONException:" + e.getMessage());
            return null;
        }
        return itemList;
    }

    private class DownloadContentTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading");
            progressDialog.show();

            if (!HTTConnect.isOnline(context)) {
                cancel(true);
                progressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            HTTConnect connection = new HTTConnect(addressUrl);
            pageContent = connection.getPage();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            List<ListItemContainer> itemList = createItemList(pageContent);
            if(itemList != null){
                ListItemAdapter listItemAdapter = new ListItemAdapter(itemList);
                itemListView.setAdapter(listItemAdapter);
            }
            progressDialog.dismiss();
        }
    }
}
