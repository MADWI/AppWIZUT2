package pl.edu.zut.mad.appwizut2.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by damian on 23.02.16.
 */
public class WebPlanParser {

    String dataToParse;

    public WebPlanParser(String dataToParse){
        this.dataToParse = dataToParse;
    }

    public void parseData() throws XmlPullParserException, IOException {
        long begin = System.nanoTime();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(dataToParse));
        int eventType = xpp.getEventType();
        if (eventType == XmlPullParser.START_DOCUMENT)
            xpp.next();
        String currentTag = "";
        String currentText = "";
        String currentDate = "";
        HashMap<String,ArrayList<ArrayList<String>>> finalData = new HashMap<>();
        ArrayList<String> currentData = new ArrayList<>();
        while (xpp.getAttributeCount() <= 0){
            try {
                xpp.nextTag();
            }catch (XmlPullParserException e){
                xpp.next();
                xpp.nextTag();
            }

        }
        currentTag = xpp.getName();


        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG){
                currentText = " ";
            }else if (eventType == XmlPullParser.TEXT){
                currentText =  xpp.getText();


            }else if (eventType == XmlPullParser.END_TAG){
                currentTag = xpp.getName();
                if (currentTag.equals("th")){

                    currentDate = currentText;

                }else if (currentTag.equals("td")){
                    currentData.add(currentText);
                }else if (currentTag.equals("tr")){

                    if (currentData.size() > 0) {
                        // 6 - adress (not needed)
                        currentData.remove(6);
                        currentData.remove(0);
                        // last - exam form (also not needed)
                        currentData.remove(currentData.size() - 1);
                        ArrayList<String> cpy = new ArrayList<>(currentData);
                        ArrayList<ArrayList<String>> classesArray = finalData.get(currentDate);
                        if (classesArray == null){
                            classesArray = new ArrayList<>();
                        }
                        classesArray.add(cpy);
                        finalData.put(currentDate,classesArray);
                        currentData.clear();

                    }
                }

            }
            eventType = xpp.next();
        }
        long end = System.nanoTime();

        double result = (end - begin) / 1000000000.0;
        System.out.println(result);
    }
}
