package pl.edu.zut.mad.appwizut2.utils;

import org.json.JSONObject;

/**
 * JSON-related Utilities
 */
public class JsonUtils {

    /**
     * Returns string from JSON or null (unlike built-in implementation that returns "null")
     *
     * http://stackoverflow.com/a/23377941
     */
    public static String optString(JSONObject jsonObject, String key) {
        if (jsonObject.isNull(key)) {
            return null;
        } else {
            return jsonObject.optString(key, null);
        }
    }
}
