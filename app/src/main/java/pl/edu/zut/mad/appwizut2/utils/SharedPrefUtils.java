package pl.edu.zut.mad.appwizut2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Klasa wspomagajaca prace z SharedPreferences
 * 
 * @author Sebastian Swierczek
 * */
public class SharedPrefUtils {

    /**
     * Metoda zwraca obiekt SharedPreferences, ktory wskazuje na plik
     * SharedPreferences
     * 
     * 
     * @param ctx
     *            kontekst aplikacji
     * 
     * @return wskazany obiekt SharedPreferences
     */
    public static SharedPreferences getSharedPreferences(Context ctx) {
	return ctx.getSharedPreferences(Constans.PREFERENCES_NAME,
		Activity.MODE_PRIVATE);
    }

    /**
     * Metoda czyszczaca dane z SharedPreferences
     * 
     * @param ctx
     *            kontekst aplikacji
     */
    public static void clearPreferences(Context ctx) {
	SharedPreferences preferences = ctx.getSharedPreferences(
		Constans.PREFERENCES_NAME, Activity.MODE_PRIVATE);
	SharedPreferences.Editor editor = preferences.edit();
	editor.clear();
	editor.commit();
    }

    /**
     * Metoda do zapisu informacji w SharedPreferences
     * 
     * @param preferences
     *            obiekt SharedPreferences w ktorym zapisane sa dane
     * @param key
     *            nazwa zapisywanego elementu w SharedPreferences
     * @param value
     *            wartosc zapisywanego elementu w SharedPreferences
     */
    public static void saveString(SharedPreferences preferences, String key,
	    String value) {
	SharedPreferences.Editor editor = preferences.edit();
	editor.putString(key, value);
	editor.commit();
    }

    /**
     * Metoda wczytujaca z SharedPreferences
     * 
     * @param preferences
     *            wczytywany obiekt
     * 
     * @param key
     *            wczytywana nazwa
     * 
     * @return wartosc wczytanego elementu
     */
    public static String loadString(SharedPreferences preferences, String key) {
	return preferences.getString(key, "");
    }

}
