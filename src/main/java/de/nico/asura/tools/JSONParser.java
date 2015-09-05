package de.nico.asura.tools;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public final class JSONParser {

    // Log tag for this class
    private static final String TAG = "JSONParser";
    // String where index is cached
    private static final String TAG_JSON = "json";
    // Shows time when index was synced the last time
    private static final String TAG_TIME = "lastTimeSynced";
    // How long the index should be cached in milliseconds
    private static final long CACHE_TIME = 600000;
    // Default SharedPreferences
    private static SharedPreferences cache;

    /**
     * Parses a {@link org.json.JSONObject} from an {@link java.net.URL}.
     *
     * @param urlString A http://... address
     * @return {@link org.json.JSONObject} from urlString, null if urlString does not contain a {@link org.json.JSONObject}.
     */
    @Nullable
    public static JSONObject getJSONFromUrl(Context c, String urlString, boolean force, boolean online) {
        cache = PreferenceManager.getDefaultSharedPreferences(c);
        final String JSON = readStringFromCache(TAG_JSON);
        if (JSON == null || !shouldCache(cache) || force) {
            if (online) {
                final InputStream inputStream = getInputStreamFromURL(urlString);
                if (inputStream != null) {
                    final String newJSON = readStringFromInputStream(inputStream);
                    final JSONObject jsonObject = getJSONObjectFromString(newJSON);
                    if (jsonObject != null) {
                        writeToCache(newJSON, TAG_JSON);
                        writeToCache(System.currentTimeMillis(), TAG_TIME);
                        return jsonObject;
                    }
                }
            }
        }
        return getJSONObjectFromString(JSON);
    }

    /**
     * Reads String from InputStream
     */
    private static String readStringFromInputStream(InputStream is) {
        try {
            final InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            final BufferedReader reader = new BufferedReader(isr, 8);
            final StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private static InputStream getInputStreamFromURL(String urlString) {
        // Parse String to URL
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

        // Open URL
        HttpsURLConnection urlConnection;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            return urlConnection.getInputStream();
        } catch (IOException | NullPointerException | ClassCastException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Tries to do a new {@link org.json.JSONObject} from a String.
     */
    private static JSONObject getJSONObjectFromString(String s) {
        if (s != null) {
            try {
                return new JSONObject(s);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Writes a long to the cache.
     */
    private static void writeToCache(long s, String tag) {
        final SharedPreferences.Editor editor = cache.edit();
        editor.putLong(tag, s);
        editor.commit();
    }

    /**
     * Writes a string to the cache.
     */
    private static void writeToCache(String s, String tag) {
        final SharedPreferences.Editor editor = cache.edit();
        editor.putString(tag, s);
        editor.commit();
    }

    /**
     * Reads a string from the cache.
     */
    private static String readStringFromCache(String tag) {
        return cache.getString(tag, null);
    }

    /**
     * Indicates if the index should be cached, based on the last time downloaded.
     */
    private static boolean shouldCache(SharedPreferences cache) {
        final long lastTimeSynced = cache.getLong(TAG_TIME, 0);
        final long currentTime = System.currentTimeMillis();
        return (currentTime - lastTimeSynced) < CACHE_TIME;
    }
}