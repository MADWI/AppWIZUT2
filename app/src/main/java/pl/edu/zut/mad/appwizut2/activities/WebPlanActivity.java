package pl.edu.zut.mad.appwizut2.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleEdzLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Created by damian on 23.02.16.
 */
public class WebPlanActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_plan_activity);
        final WebView web = (WebView)findViewById(R.id.web_view_plan);
        web.getSettings().setJavaScriptEnabled(true);
        final View pleaseWaitView = findViewById(R.id.please_wait);

        // Inject "/res/raw/grab_schedule.js" on all pages
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                // Add to page script from "/appwizut-injected-script.js" (replaced below)
                web.loadUrl("javascript:javascript:(function(){document.body.appendChild(document.createElement('script')).src='/appwizut-injected-script.js'})()");
            }

            @Override
            @SuppressWarnings("deprecation") // Newer version is not supported on older Android versions
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                // Load contents of "/appwizut-injected-script.js" from resource
                if (url.endsWith("/appwizut-injected-script.js")) {
                    return new WebResourceResponse(
                            "text/javascript",
                            "utf-8",
                            getResources().openRawResource(R.raw.grab_schedule)
                    );
                }
                return null;
            }

            // Handle response from javascript
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("js-grabbed-table:")) {
                    String tableJson;
                    try {
                        tableJson = URLDecoder.decode(url.substring(17), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    tableGrabbedByJavascript(tableJson);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show or hide "please wait" text
                pleaseWaitView.setVisibility(url.contains("Logowanie2.aspx") ? View.GONE : View.VISIBLE);
            }
        });

        web.loadUrl("https://edziekanat.zut.edu.pl/WU/PodzGodzin.aspx");
    }

    public void tableGrabbedByJavascript(final String contents) {
        DataLoadingManager
                .getInstance(WebPlanActivity.this)
                .getLoader(ScheduleEdzLoader.class)
                .setSourceTableJson(contents);
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putString(Constants.PREF_TIMETABLE_DATA_SOURCE, "edziekanat")
                .apply();
        finish();
    }
}
