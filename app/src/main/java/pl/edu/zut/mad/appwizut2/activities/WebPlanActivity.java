package pl.edu.zut.mad.appwizut2.activities;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.utils.WebPlanParser;

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
        web.addJavascriptInterface(this,"HTMLContent");
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("PodzGodzDruk")){
                    web.loadUrl("javascript:window.HTMLContent.foundData('<table>'+document.getElementsByTagName('table')[0].innerHTML+'</table>');");

                }
            }
        });
        web.loadUrl("https://edziekanat.zut.edu.pl/WU/PodzGodzin.aspx");
    }



    @SuppressWarnings("unused")
    @android.webkit.JavascriptInterface
    public void foundData(String contents){

        String data = contents.replace("&nbsp;","").trim();
        WebPlanParser parser = new WebPlanParser(data);
        try {
            parser.parseData();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
