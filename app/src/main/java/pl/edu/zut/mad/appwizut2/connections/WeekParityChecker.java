package pl.edu.zut.mad.appwizut2.connections;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import pl.edu.zut.mad.appwizut2.models.DayParity;
import pl.edu.zut.mad.appwizut2.utils.Constans;
import pl.edu.zut.mad.appwizut2.utils.OfflineHandler;
import pl.edu.zut.mad.appwizut2.utils.SharedPrefUtils;


/**
 * Klasa sprawdzajaca nieparzystosc/parzystosc dni tygodnia
 * 
 * @author Grzegorz Fabisiak, Dawid Glinski
 * 
 */
public class WeekParityChecker {
	

	/**
	 * Zmienna zawierajaca adres strony z danymi o parzystosci tygodnia w formacie
	 * JSON
	 */
	private static String ZUT_WI_JSON = "http://wi.zut.edu.pl/components/com_kalendarztygodni/zapis.json";

	/**
	 * Zmienna do debuggowania.
	 */
	private static final String TAG = "WeekParityChecker";

    private Context context;
    public static final int MY_PERMISSIONS_WRITE_EXTERNAL = 0;
	private boolean changesMade = false;
	private ArrayList<DayParity> currentData;

	/** Domyslny konstruktor klasy. */
	public WeekParityChecker(Context context) {
		this.context = context;
	}



	/**
	 * Metoda zwraca tablice stringow, ktora mowi czy dzien obecny i nastepny jest
	 * nieparzysty/parzysty.
	 * 
	 * @return Tablica stringow mowiaca o nieparzystosci/parzystosci dnia obecnego
	 *         i nastepnego.
	 */
	private String[] getParity() {
		String pageSource = WeekParityChecker.getURLSource(ZUT_WI_JSON);

		String[] currentWeek = new String[2];

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;

		int day = c.get(Calendar.DAY_OF_MONTH);

		c.add(Calendar.DAY_OF_YEAR, 1);
		int dayNext = c.get(Calendar.DAY_OF_MONTH);

		String today = "_" + Integer.toString(year) + "_" + Integer.toString(month)
				+ "_" + Integer.toString(day);
		String tomorrow = "_" + Integer.toString(year) + "_"
				+ Integer.toString(month) + "_" + Integer.toString(dayNext);

		Log.d(TAG, today + " " + tomorrow);

		try {
			JSONObject pageSrcObject = new JSONObject(pageSource);

			if (pageSrcObject.has(today)) {
				currentWeek[0] = pageSrcObject.getString(today);
			} else
				currentWeek[0] = "?";

			if (pageSrcObject.has(tomorrow))
				currentWeek[1] = pageSrcObject.getString(tomorrow);
			else
				currentWeek[1] = "?";

		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 2; i++) {
			if (currentWeek[i].equals("x")) {
				Log.e(TAG, currentWeek[i]);
				currentWeek[i] = "---";
			} else if (currentWeek[i].equals("p"))
				currentWeek[i] = "parzysty";
			else if (currentWeek[i].equals("n"))
				currentWeek[i] = "nieparzysty";
			else
				currentWeek[i] = "?";
		}

		return currentWeek;
	}

	/**
	 * Metoda zwraca HashMap ze wszystkimi informacjami o
	 * nieparzystosci/parzystosci danego dnia tygodnia
	 * 
	 * @return HashMap z informacjami o wszystkich dniach (ich
	 *         nieparzystosci/parzystosci)
	 */
	private ArrayList<DayParity> getAllParity() {

		String pageSource = getURLSource(ZUT_WI_JSON);
		ArrayList<DayParity> daysParityList = new ArrayList<>();

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month - 1, day);
		String[] weekdays = new DateFormatSymbols().getWeekdays();

		try {
			JSONObject pageSrcObject = new JSONObject(pageSource);
			JSONArray dates = pageSrcObject.names();

			for (int i = 0; i < dates.length(); i++) {
				if (pageSrcObject.has(dates.get(i).toString())) {

					String tempDate = dates.get(i).toString();
					int charAfterYear = tempDate.indexOf("_", 1);
					int charAfterMonth = tempDate.indexOf("_", charAfterYear + 1);

					int yearJSON = Integer.parseInt(tempDate.substring(1, charAfterYear));
					int monthJSON = Integer.parseInt(tempDate.substring(
							charAfterYear + 1, charAfterMonth));
					int dayJSON = Integer.parseInt(tempDate.substring(charAfterMonth + 1,
							tempDate.length()));
					GregorianCalendar dateJSON = new GregorianCalendar(yearJSON,
							monthJSON - 1, dayJSON);

					if (dateJSON.after(today) || dateJSON.equals(today)) {

						String monthString = "";
						String dayString = "";

						if (monthJSON < 10)
							monthString = "0" + Integer.toString(monthJSON);
						else
							monthString = Integer.toString(monthJSON);

						if (dayJSON < 10)
							dayString = "0" + Integer.toString(dayJSON);
						else
							dayString = Integer.toString(dayJSON);

						String date = Integer.toString(yearJSON) + "." + monthString + "."
								+ dayString;

						String dayType = pageSrcObject.getString(tempDate);
						if (dayType.equals("x"))
							dayType = "---";
						else if (dayType.equals("p"))
							dayType = "parzysty";
						else if (dayType.equals("n"))
							dayType = "nieparzysty";
						else
							dayType = "?";

						String dayOfTheWeek = weekdays[dateJSON.get(Calendar.DAY_OF_WEEK)];
						daysParityList.add(new DayParity(date, dayType, dayOfTheWeek,
								0, dateJSON));
					}

				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		Collections.sort(daysParityList, new CustomComparator());

		return daysParityList;
	}

    // odwolanie do danych po skonczeniu pobierania, dla wszystkich dni
    public interface DataCallback {
        void foundData(ArrayList<DayParity> data);

    }

    //odwolanie do danych po skonczeniu pobierania, dla dzis i jutra
    public interface  DataTwoDaysCallback {
        void twoDaysData(String[] data);
    }


    /**
     *
     * @param callback zwraca pobrane dane (online jeśli nie mamy jeszcze danych offline)
     * @author Damian Malarczyk
     */
    public void getCurrentData(final DataCallback callback){

        if (currentData == null){
            currentData = readOfflineParity();
            if (currentData == null && HttpConnect.isOnline(context)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentData = getAllParity();
                        changesMade = true;
                        currentData = trimDataTillToday(currentData);
                        saveIfChangesMade();
                        callback.foundData(currentData);
                    }
                }).start();
            }else {
               callback.foundData(currentData);
            }

        }else {
            callback.foundData(currentData);
        }
    }





	/**
	 * Zapis danych dla trybu offline
	 * Metoda publiczna, aby umożliwić oddzielne korzystanie z metody @getAllParity
	 * bez używania metody @downloadAndSaveNewestData
	 *
	 * @author Damian Malarczyk
	 */
	private void  saveIfChangesMade(){
        if (changesMade) {
            OfflineHandler.folderSetup(context);
            File dataFile = context.getFilesDir();

            try {
                File daysParityFile = new File(dataFile, Constans.OFFLINE_DATA_FOLDER + "/DaysParity");
                FileOutputStream daysOutput = new FileOutputStream(daysParityFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(daysOutput);
                objectOutputStream.writeObject(currentData);
                objectOutputStream.close();
                daysOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

	}

	/**
	 * Odczyt posiadanych danych offline odnośnie parzystości
	 * Za każdym wczytanie dane są kontrolowane
	 * jeśli posiadamy informacje o dniach które już przeminęły to są one usuwane
	 * @author Damian Malarczyk
	 */
	private ArrayList<DayParity> readOfflineParity(){
		File dataFile = context.getFilesDir();
		try {
			File daysParityFile = new File(dataFile, Constans.OFFLINE_DATA_FOLDER + "/DaysParity");
			FileInputStream daysOutput = new FileInputStream(daysParityFile);

			ObjectInputStream objectOutputStream = new ObjectInputStream(daysOutput);

			ArrayList<DayParity> daysParityList = (ArrayList<DayParity>) objectOutputStream.readObject();
			objectOutputStream.close();
			daysOutput.close();

            return(trimDataTillToday(daysParityList));

		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

    /**
     *
     * @param data
     * @return zwraca przyciętne dni parzystości, tylko od dnia dzisiejszego naprzód
     * @author Damian Malarczyk
     */
    private ArrayList<DayParity> trimDataTillToday(ArrayList<DayParity> data){
        Date now = new Date();
        if (data != null && data.size() > 0) {
            DayParity nextParity = data.get(0);

            boolean foundEqualOrAfter = false;
            while (nextParity != null && !foundEqualOrAfter) {
                if (laterOrEqualThanDate(now, nextParity.getDate())) {
                    return data;
                } else {
                    if (data.size() > 1) {
                        data.remove(nextParity);
                        nextParity = data.get(0);
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }




	/**
	 * Metoda pomocnicza
	 * Sprawdza czy w danych offline znajduje się odpowiednia ilość danych
	 * oraz to czy kolejne dni są dniami w których odbywają się zajęcia
	 * @author Damian Malarczyk
	 */
	private void  readNextDaysParity(final DataTwoDaysCallback callback){

		getCurrentData(new DataCallback() {
            @Override
            public void foundData(ArrayList<DayParity> data) {
                String[] weekParity = new String[2];
                DateFormat format = new SimpleDateFormat("yyyy.MM.dd");

                Calendar calendar = Calendar.getInstance();
                String today = format.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_YEAR,1);
                String tomorrow = format.format(calendar.getTime());


                if (data != null && data.size() >= 2) {
                    weekParity[0] = "-";
                    weekParity[1] = "-";
                    DayParity first = data.get(0);
                    DayParity second = data.get(1);


                    if (first.getDate().equals(today)){
                        weekParity[0] = first.getParity();
                        if (second.getDate().equals(tomorrow)){
                            weekParity[1] = second.getParity();

                        }

                    }else if (first.getDate().equals(tomorrow)){
                        weekParity[1] = first.getParity();

                    }
                    callback.twoDaysData(weekParity);
                }else {
                    callback.twoDaysData(null);
                }

            }


        });



	}

	/**
	 * Informacja odnośnie parzystości dnia obecnego i kolejnego
	 * zwraca '-' gdy nie ma danych o danym dniu
	 * Zwraca NULL gdy w danych offline nie ma odpowiedniej ilości danych
	 * w tym przypadku niezbędne jest ponowne pobranie danych online (metoda @getParity)
	 * @return parzystość dwóch następnych dni
	 * @author Damian Malarczyk
	 */
	public  void getNextDaysParity(final DataTwoDaysCallback callback){
		final SharedPreferences preferences = SharedPrefUtils.getSharedPreferences(context);
		String lastCheckDay = preferences.getString(Constans.LAST_DAY_PARITY_UPDATE, "brak");

		Date today = new Date();

		DateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		final String todayString = format.format(today);



		if (!todayString.equals(lastCheckDay)){
            readNextDaysParity(new DataTwoDaysCallback() {
                @Override
                public void twoDaysData(String[] data) {
                    if (data != null && data.length == 2) {
                        SharedPreferences.Editor prefEditor = preferences.edit();
                        prefEditor.putString(Constans.WEEK_PARITY, data[0]);
                        prefEditor.putString(Constans.WEEK_PARITY_NEXT, data[1]);
                        prefEditor.putString(Constans.LAST_DAY_PARITY_UPDATE, todayString);
                        prefEditor.commit();

                    }
                    callback.twoDaysData(data);
                }
            });


		}else {

			String[] weekParity = new String[2];
			weekParity[0] = preferences.getString(Constans.WEEK_PARITY,"-");
			weekParity[1] = preferences.getString(Constans.WEEK_PARITY_NEXT,"-");
            callback.twoDaysData(weekParity);

		}


	}


	/**
	 * Metoda pomocnicza
	 * Sprwadza czy podany argument date jest równym lub dalszym dniem
	 *@author Damian Malarczyk
	 */
	private static boolean laterOrEqualThanDate(Date date,String parity){
		DateFormat format = new SimpleDateFormat("yyyy.MM.dd");

		try {
			Date theDate = format.parse(format.format(date));
			Date fromParity = format.parse(parity);
			if (fromParity.after(theDate) || fromParity.equals(theDate)){
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	public class CustomComparator implements Comparator<DayParity> {
		@Override
		public int compare(DayParity o1, DayParity o2) {
			return o1.getDate().compareTo(o2.getDate());
		}
	}

	/**
	 * Metoda zwraca zrodlo strony jako String.
	 * 
	 * @param url
	 *          zmienna zawierajaca adres strony do pobrania.
	 * @return zrodlo strony jako zmienna typu String.
	 */
	private static String getURLSource(String url) {
		String do_obrobki = "";

		HttpConnect strona = new HttpConnect(url);
		do_obrobki = strona.getPage();

		return do_obrobki;
	}
}
