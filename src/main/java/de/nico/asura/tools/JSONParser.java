package de.nico.asura.tools;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

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

    /**
     * Parses a {@link org.json.JSONObject} from an {@link java.net.URL}.
     *
     * @param urlString A http://... address
     * @return {@link org.json.JSONObject} from urlString, null if urlString does not contain a {@link org.json.JSONObject}.
     */
    @Nullable
    public static JSONObject getJSONFromUrl(String urlString) {
        // Parse String to URL
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("MalformedURLException", e.toString());
        }

        // Open URL
        HttpsURLConnection urlConnection = null;
        InputStream is;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            is = urlConnection.getInputStream();
        } catch (IOException e) {
            Log.e("IOException", e.toString());
            return null;
        } catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
            return null;
        } finally {
            try {
                urlConnection.disconnect();
            } catch (NullPointerException e) {
                Log.e("NullPointerException", e.toString());
            }
        }

        // Read URL
        String json;
        try {
            final InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            final BufferedReader reader = new BufferedReader(isr, 8);
            final StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();
        } catch (IOException e) {
            Log.e("IOException", e.toString());
            return null;
        }

        // Return JSONObject if it's one, otherwise null
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
            return null;
        }
    }
}