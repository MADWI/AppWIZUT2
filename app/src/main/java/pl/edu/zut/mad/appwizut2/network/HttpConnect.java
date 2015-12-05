package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * One liner usage
 *     page = new HttpConnect(address).readAllAndClose()
 *
 * Usage with cache (TODO: probably need cleanup)
 *     HttpConnect conn = new HttpConnect(address)
 *     conn.ifModifiedSince(lastModified)
 *     if (!conn.isNotModified()) {
 *         lastModified = conn.getLastModified()
 *         page = conn.readAllAndClose()
 *     }
 */
public class HttpConnect {

    /***
     * Zmienna do debuggowania.
     */
    private static final String TAG = "HttpConnect";


    private HttpURLConnection mUrlConnection;

    private boolean mDidConnect = false;


    /**
     * Konstruktor sluzacy do polaczenia ze strona WWW.
     * @param address Address of page to load
     */
    public HttpConnect(String address) {
        try {
            URL url = new URL(address);
            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setConnectTimeout(5000);
            mUrlConnection.setReadTimeout(5000);
        } catch (Exception e) {
            // Can only fail with invalid argument, not due to network problem
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated Don't provide timeout (this constructor ignores it anyway)
     */
    public HttpConnect(int timeout, String address) {
        this(address);
    }

    /**
     * Only get resource if it's newer than provided date
     */
    public void ifModifiedSince(long lastModified) {
        mUrlConnection.setIfModifiedSince(lastModified);
    }

    long getLastModified() throws IOException {
        connectIfNeeded();
        return mUrlConnection.getLastModified();
    }

    /**
     * True if server returned "Not modified" response
     *
     * @see #ifModifiedSince(long)
     */
    public boolean isNotModified() throws IOException {
        connectIfNeeded();
        return mUrlConnection.getResponseCode() == 304;
    }

    /**
     * Metoda do pobrania zrodla strony jako String.
     *
     * In case of error returns empty String
     *
     * @return zadana strona jako String.
     * @deprecated Use {@link #readAllAndClose()} and catch exception
     */
    public String getPage() {
        Log.i(TAG, "getPage");
        try {
            return readAllAndClose();
        } catch (IOException e) {
            Log.e(TAG, "Exception during load through non throwing api", e);
            return ""; // Legacy...
        }
    }



    public String readAllAndClose() throws IOException {
        InputStreamReader reader = null;
        try {

            connectIfNeeded();

            reader = new InputStreamReader(mUrlConnection.getInputStream(), "iso-8859-1");

            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int charsRead;

            while ((charsRead = reader.read(buf)) != -1) {
                sb.append(buf, 0, charsRead);
            }

            return sb.toString();

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Failed to close reader", e2);
                }
            }
            close();
        }
    }

    private void connectIfNeeded() throws IOException {
        if (!mDidConnect) {
            mDidConnect = true;
            mUrlConnection.connect();
        }
    }

    public void close() {
        mUrlConnection.disconnect();
        mUrlConnection = null;
    }

    /**
     * Metoda sprawdzajaca polaczenie z Internetem
     *
     * Note: if this returns true, it doesn't mean that connection will succeed
     *       (through if it's false, there's no point in trying to connect)
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
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

}