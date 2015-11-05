package pl.edu.zut.mad.appwizut2;

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

/**
 * Created by macko on 04.11.2015.
 */
public class HTTPConnect {

    private static final String TAG = "HTTPConnect";
    private String addressURL = null;
    private String content;

    HTTPConnect (String addressURL) {
        // serviceUrl = "http://www.wi.zut.edu.pl";
        //serviceUrl = "http://www.wi.zut.edu.pl/ogloszenia?format=json";
        this.addressURL = addressURL;
    }

    public String getContent(){

        HttpURLConnection urlConnection = null;

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
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }

            return sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG,"URL address is not valid" + e.getMessage());
        } catch (SocketTimeoutException e) {
            Log.e(TAG,"socket tiemout" + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
