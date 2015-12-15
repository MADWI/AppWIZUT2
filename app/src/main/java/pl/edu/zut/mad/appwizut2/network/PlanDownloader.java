package pl.edu.zut.mad.appwizut2.network;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Klasa odpowiedzialna za pobieranie listy grup ze strony oraz pobieranie planu
 * zajec dla kazdej grupy
 *
 * @author Sebastian Peryt, Sebastian Swierczek
 *
 */
public class PlanDownloader {

    /**
     * Zmienna do debuggowania.
     */
    private static final String TAG = "WidgetDownload";

    /** Adres strony zawierajacej plany zajec. */
    private static final String siteIn = "http://wi.zut.edu.pl/plan/Wydruki/PlanGrup/";

    /**
     * Funkcja na podstawie zadanego ciagu wejsciowego zwraca tablice z numerami
     * grup.
     *
     * @param rodzajStudiow
     *            rodzaj studiow (string)
     * @param kierunek
     *            kierunek studiow (string)
     * @param stopien
     *            stopien studiow (liczba)
     * @param rok
     *            rok studiow (liczba)
     * @return Tablica stringow ze znalezionymi grupami
     */
    public static String[] getGroups(String rodzajStudiow, String kierunek,
                                     int stopien, int rok) {
        Log.d(TAG, "Stopien: " + Integer.valueOf(stopien));
        Log.d(TAG, "Rok: " + Integer.valueOf(rok));

        HttpConnect con = new HttpConnect(siteIn + rodzajStudiow);
        String site = null;

        // String[] outputTab = new String[]; //- Pamiec jest chyba dynamicznie
        // przydzielana to co jest nie tak?
        try {
            site = con.getPage();
            Log.d(TAG, "Polaczono ze strona");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        if ("" == site) {
            Log.e(TAG, "Error con.getStrona()");
        }

        // wybor kierunku i roku
        Pattern p = null;
        p = getRodzaj(rodzajStudiow, kierunek, stopien, rok);

        // sP = this.getRodzaj(rodzajStudiow, kierunek, stopien, rok);

        /**
         * W momencie gdy zle zostana podane dane: rok; kierunek; rodzaj;
         */
        if (null == p) {
            Log.d(TAG, "błedne dane, zwracam nulla...");
            return null;
        }

        // p = Pattern.compile(sP);

        Matcher m = p.matcher(site);
        int i = 0;
        while (m.find()) {
            i++;
        }
        m.reset();
        String[] outputTab = new String[i];
        i = 0;
        while (m.find()) {
            outputTab[i] = m.group().subSequence(1, m.group().indexOf(".pdf"))
                    .toString();
            Log.d(TAG, outputTab[i]);
            i++;
        }
        return outputTab;
    }

    /**
     * Metoda sprawdzajaca rodzaje studiow i dostepne na nich kierunki
     *
     * @param rodzaj
     *            String Rodzaj studiow z wielkiej litery
     * @param kierunek
     *            String Kierunek studiow z wielkiej litery
     * @param stopien
     *            int Stopien studiow
     * @param rok
     *            int Rok studiow
     * @return pattern jezeli dana istnieje, null jesli nie.
     */
    private static Pattern getRodzaj(String rodzaj, String kierunek,
                                     int stopien, int rok) {
        Pattern p = null;
        if (rodzaj.equals("Stacjonarne")) {
            if (kierunek.equals("Bioinformatyka")) {
                p = Pattern.compile(">BI" + stopien + "-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            } else if (kierunek.equals("Inżynieria cyfryzacji")) {
                p = Pattern.compile(">IC" + stopien + "-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            } else if (kierunek.equals("Informatyka")) {
                p = Pattern.compile(">I" + stopien + "-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            } else if (kierunek.equals("ZIP")) {
                p = Pattern.compile(">ZIP" + stopien + "-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            }
            return p;
        } else if (rodzaj.equals("Niestacjonarne")) {
            if (kierunek.equals("Bioinformatyka")) {
                p = null;
            } else if (kierunek.equals("Inżynieria cyfryzacji")) {
                p = null;
            } else if (kierunek.equals("Informatyka")) {
                p = Pattern.compile(">I" + stopien + "n-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            } else if (kierunek.equals("ZIP")) {
                p = Pattern.compile(">ZIP" + stopien + "n-" + rok
                        + "[0-9]{1,2}\\.pdf<");
            }
            return p;
        }

        return null;
    }

}
