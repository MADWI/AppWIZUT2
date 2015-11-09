package pl.edu.zut.mad.appwizut2.connections;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.zut.mad.appwizut2.models.MessagePlanChanges;


/**
 * Klasa do parsowania zmian w planie ze strony WI ZUT.
 * 
 * @author Sebastian Swierczek
 * @version 1.2.1
 */
public class GetPlanChanges {

	/**
	 * Zmienna do debuggowania.
	 */
	private static final String TAG = "GetPlanChanges";

	/** Zmienna przechowujaca zawartosc strony. */
	private static String strona = "";

	/**
	 * Zmienna zawierajaca adres odnoszacy sie do zmian w planie w formacie JSON.
	 * */
	private static final String adres = "http://wi.zut.edu.pl/plan-zajec/zmiany-w-planie?format=json";

	/** Zmienna zawierajaca szablon potrzebny przy parsowaniu */
	private static final Pattern patternQuot = Pattern.compile("(&quot;)");

	/** Obiekt klasy HttpConnect sluzacy do polaczenia ze strona. */
	private final HttpConnect con;

	/**
	 * Konstruktor klasy wykorzystujacy obiekt HttpConnect do polaczenia ze strona
	 */
	public GetPlanChanges() {
		con = new HttpConnect(10000, adres);
	}

	/**
	 * Metoda do pobierania ostatnich zmian w planie ze strony.
	 * 
	 * @return ostatnie zmiany w planie jako okiekt klasy MessagePlanChanges.
	 */
	public MessagePlanChanges getLastMessage() {
		Log.i(TAG, "getLastMessage");
		strona = con.getPage();
		MessagePlanChanges tempMsg = new MessagePlanChanges();

		if (!strona.equals("")) {
			/* Arrays of JSON and Messages elements */
			JSONArray entry = new JSONArray();
			JSONObject jsonObject = new JSONObject();
			/* parse response from server */
			if (!strona.equals("")) {
				try {
					/* initialize JSON object with server response */
					jsonObject = (JSONObject) new JSONTokener(strona).nextValue();
					/* get JSONArray from response */
					entry = jsonObject.getJSONArray("entry");

					/* parse elements of JSONArray */

					if (entry.getJSONObject(0).has("title"))
						tempMsg.setTitle(entry.getJSONObject(0).getString("title"));
					if (entry.getJSONObject(0).has("created"))
						tempMsg.setDate(entry.getJSONObject(0).getString("created"));
					if (entry.getJSONObject(0).has("text"))
						tempMsg.setBody(entry.getJSONObject(0).getString("text"));

					// replace &quot and set to body String
					String temp = tempMsg.getBody();
					Matcher matcherString = patternQuot.matcher(temp);
					tempMsg.setBody(matcherString.replaceAll("\""));

				} catch (JSONException e) {
					Log.e(TAG, "JSONException " + e.toString());
					e.printStackTrace();
					return null;
				}
			}
			return tempMsg;
		} else
			return null;
	}

	/**
	 * Metoda do zwracania wszystkich zmian w planie ze strony WI ZUT jako
	 * ArrayList obiektow MessagePlanChanges.
	 * 
	 * @return ArrayList ze zmianami w planie.
	 */
	public ArrayList<MessagePlanChanges> getServerMessages() {
		Log.i(TAG, "getServerMessages");
		strona = con.getPage();
		/* parse response from server */
		if (!strona.equals("")) {
			ArrayList<MessagePlanChanges> DataArray = new ArrayList<MessagePlanChanges>();
			MessagePlanChanges tempMsg = new MessagePlanChanges();

			/* Arrays of JSON and Messages elements */
			JSONArray entry = new JSONArray();
			JSONObject jsonObject = new JSONObject();

			try {
				/* initialize JSON object with server response */
				jsonObject = (JSONObject) new JSONTokener(strona).nextValue();
				/* get JSONArray from response */
				entry = jsonObject.getJSONArray("entry");

				/* parse elements of JSONArray */

				for (int i = 0; i < entry.length(); i++) {
					tempMsg = new MessagePlanChanges();
					/*
					 * checking availability of elements and set fields of Message objects
					 */
					if (entry.getJSONObject(i).has("title"))
						tempMsg.setTitle(entry.getJSONObject(i).getString("title"));
					if (entry.getJSONObject(i).has("created"))
						tempMsg.setDate(entry.getJSONObject(i).getString("created"));
					if (entry.getJSONObject(i).has("content"))
						tempMsg.setBody(entry.getJSONObject(i).getString("content"));

					// replace &quot and set to body String
					String temp = tempMsg.getBody();
					Matcher matcherString = patternQuot.matcher(temp);
					tempMsg.setBody(matcherString.replaceAll("\""));

					DataArray.add(tempMsg);
				}
				//** offline data implementation */

			} catch (JSONException e) {
				Log.i(TAG, "JSONException" + e.toString());
				e.printStackTrace();
				return null;
			}
			return DataArray;
		}

		else
			return null;
	}
}