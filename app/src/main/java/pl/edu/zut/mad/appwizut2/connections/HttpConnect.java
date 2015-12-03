package pl.edu.zut.mad.appwizut2.connections;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnect {
    /***
     * Zmienna do debuggowania.
     */
    private static final String TAG = "HttpConnect";

    /** Zmienna przechowujaca zrodlo strony jako String */
    private String strona;

    private HttpURLConnection urlConnection;
    private URL url;

    /**
     * Konstruktor sluzacy do polaczenia ze strona WWW.
     * @param adres
     */
    public HttpConnect(String adres) {
        strona = "";

        try {
            url = new URL(adres);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda do pobrania zrodla strony jako String.
     *
     * @return zadana strona jako String.
     */
    public String getPage() {
        Log.i(TAG, "getPage");
        if (executeHttpGet() == false) {
            return "";
        }
        return strona;
    }

    /**
     * Metoda realizujaca pobieranie strony
     *
     * @return true, jezeli sie powiodlo.
     */
    private boolean executeHttpGet() {
        Log.i(TAG, "executeHttpGet");
        InputStream is = null;
        BufferedReader reader = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if(urlConnection == null)
                return false;

            is = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"),8);

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                sb.append(line + NL);
            }
            strona = sb.toString();

        } catch (IOException e) {
            Log.e(TAG, "Exception (executeHttpGet) " + e.toString());
            e.printStackTrace();
            return false;
        } finally {
            urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Exception 2 (executeHttpGet) " + e2.toString());
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Metoda sprawdzajaca polaczenie z Internetem
     *
     * @param ctx
     *            kontekst aplikacji
     *
     * @return true jezeli stwierdzono polaczenie
     */
    public static boolean isOnline(Context ctx) {
        Log.i(TAG, "isOnline...");

        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
