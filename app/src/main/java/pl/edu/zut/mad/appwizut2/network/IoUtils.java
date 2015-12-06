package pl.edu.zut.mad.appwizut2.network;

import java.io.Closeable;
import java.io.IOException;

public class IoUtils {
    /**
     * Close something without throwing exception, can be passed null
     */
    // Based on Apache Commons
    public static void closeQuietly(Closeable input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) {
        }
    }
}
