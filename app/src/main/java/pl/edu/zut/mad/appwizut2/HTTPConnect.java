package pl.edu.zut.mad.appwizut2;

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

    private String serviceUrl = null;
    private String content;

    HTTPConnect () {
        // serviceUrl = "http://www.wi.zut.edu.pl";
        serviceUrl = "http://www.wi.zut.edu.pl/ogloszenia?format=json";
    }

    public String getContent(){

        HttpURLConnection urlConnection = null;

        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            // handle issues
            int statusCode = urlConnection.getResponseCode();

            // handle errors
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            // create JSON object from content
            InputStream is = urlConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }

            return sb.toString();

        } catch (MalformedURLException e) {
            // URL is invalid
        } catch (SocketTimeoutException e) {
            // data retrival or connection timed out
        } catch (IOException e) {
            // Toast.makeText(A, "No internet connection", Toast.LENGTH_LONG).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
