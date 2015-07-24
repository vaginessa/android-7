package de.nico.asura.tools;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public final class JSONParser {

    private static InputStream is;
    private static JSONObject jObj;
    private static String json;

    public static JSONObject getJSONFromUrl(String url) {

        // Make HTTP request
        try {
            final DefaultHttpClient httpClient = new DefaultHttpClient();
            final HttpPost httpPost = new HttpPost(url);
            final HttpResponse httpResponse = httpClient.execute(httpPost);
            final HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncodingException", e.toString());
        } catch (ClientProtocolException e) {
            Log.e("ClientProtocolException", e.toString());
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }

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
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }
}