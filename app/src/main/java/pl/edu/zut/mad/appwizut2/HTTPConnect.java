package pl.edu.zut.mad.appwizut2;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.Result;

/**
 * Created by macko on 04.11.2015.
 */
public class HTTPConnect {

    private static final String TAG = "HTTPConnect";

    private String addressURL;
    private Context context;
    private String pageContent;
    private HttpURLConnection urlConnection = null;

    HTTPConnect (String addressURL, Context context) {
        this.addressURL = addressURL;
        this.context = context;
    }

    public String getContent() {

        AsyncTaskGetPageContent getPageContent = new AsyncTaskGetPageContent();

        try {
            // wait until finish executing
            // TODO: 05.11.2015
            getPageContent.execute().get();
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return pageContent;
    }

    private class AsyncTaskGetPageContent extends AsyncTask<String, Boolean, Void> {

        private ProgressDialog progressDialog;
        private static final String TAG = "AsyncTaskGetPageContent";

        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            try {
                // create connection
                URL urlToRequest = new URL(addressURL);
                urlConnection = (HttpURLConnection) urlToRequest.openConnection();
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                // handle issues
                int statusCode = urlConnection.getResponseCode();
                Log.d("STATUS CODE", "" + statusCode);
                // handle errors
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // create JSON object from content
                InputStream is = urlConnection.getInputStream();
                StringBuilder pageContentBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    pageContentBuilder.append(nextLine);
                }
                pageContent = pageContentBuilder.toString();
                Log.i("pageContent", pageContent.substring(0,100));
            } catch (MalformedURLException e){
                Log.e(TAG, "MalformedURLException:" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException:" + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG,"onPreExecute");
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Pobieranie");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG,"onPostExecute");
            progressDialog.dismiss();
        }
    }
}
