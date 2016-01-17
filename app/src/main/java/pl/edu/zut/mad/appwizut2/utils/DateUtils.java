package pl.edu.zut.mad.appwizut2.utils;

public class DateUtils {
    /**
     * Get date of easter for specified day
     *
     * @return "month day" (two numbers separated by space)
     *
     * usage of algorithm to find Easter Sunday date found by Carl Friedrich Gauss
     * usage example from http://stackoverflow.com/questions/26022233/calculate-the-date-of-easter-sunday
     */
    public static String getEasterSundayDate(int year) {
        int a = year % 19,
                b = year / 100,
                c = year % 100,
                d = b / 4,
                e = b % 4,
                g = (8 * b + 13) / 25,
                h = (19 * a + b - d - g + 15) % 30,
                j = c / 4,
                k = c % 4,
                m = (a + 11 * h) / 319,
                r = (2 * e + 2 * j - k - h + m + 32) % 7,
                n = (h - m + r + 90) / 25,
                p = (h - m + r + n + 19) % 32;

        //n - month, p - day
        return String.valueOf(n) + " " + String.valueOf(p);
    }
}
