package de.nico.asura;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.nico.asura.activities.AuthWebView1;
import de.nico.asura.activities.Preferences;
import de.nico.asura.activities.WebView1;
import de.nico.asura.tools.JSONParser;
import de.nico.asura.tools.Utils;

public final class Main extends Activity {

    // JSON Node Names
    private static final String TAG_TYPE = "plans";
    private static final String TAG_NAME = "name";
    private static final String TAG_FILENAME = "filename";
    private static final String TAG_URL = "url";
    private static String offline;
    private static String noPDF;
    private static String localLoc;
    private static String jsonURL;

    // PDF Download
    private static DownloadManager downloadManager;
    private static long downloadID;
    private static File file;

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            final DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadID);
            final Cursor cursor = downloadManager.query(query);

            if (cursor.moveToFirst()) {
                final int columnIndex = cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS);
                final int status = cursor.getInt(columnIndex);

                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        final Uri path = Uri.fromFile(file);
                        final Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                        pdfIntent.setDataAndType(path, "application/pdf");
                        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        try {
                            startActivity(pdfIntent);
                        } catch (ActivityNotFoundException e) {
                            Utils.makeLongToast(Main.this, noPDF);
                            Log.e("ActivityNotFoundExcept", e.toString());
                        }
                        break;
                    case DownloadManager.STATUS_FAILED:
                        Utils.makeLongToast(Main.this,
                                getString(R.string.down_error));
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        Utils.makeLongToast(Main.this,
                                getString(R.string.down_paused));
                        break;
                    case DownloadManager.STATUS_PENDING:
                        Utils.makeLongToast(Main.this,
                                getString(R.string.down_pending));
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        Utils.makeLongToast(Main.this,
                                getString(R.string.down_running));
                        break;
                }
            }
        }
    };

    // Data from JSON file
    private final ArrayList<HashMap<String, String>> downloadList = new ArrayList<>();

    private static void checkDir() {
        final File dir = new File(Environment.getExternalStorageDirectory() + "/"
                + localLoc + "/");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_prefs:
                startActivity(new Intent(this, Preferences.class));
                return true;

            case R.id.action_WebView1:
                if (Utils.isNoNetworkAvailable(this)) {
                    Utils.makeLongToast(this, offline);
                } else {
                    startActivity(new Intent(this, WebView1.class));
                }
                return true;

            case R.id.action_Link1:
                if (Utils.isNoNetworkAvailable(this)) {
                    Utils.makeLongToast(this, offline);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.menu_Link_1_url))));
                }
                return true;

            case R.id.action_AuthWebView1:
                if (Utils.isNoNetworkAvailable(this)) {
                    Utils.makeLongToast(this, offline);
                } else {
                    startActivity(new Intent(this, AuthWebView1.class));
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void update() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        offline = getString(R.string.status_offline);
        noPDF = getString(R.string.except_nopdf);
        localLoc = getString(R.string.gen_loc);
        jsonURL = getString(R.string.gen_json);

        checkDir();

        // Parse the JSON file of the plans from the URL
        if (Utils.isNoNetworkAvailable(this)) {
            final HashMap<String, String> map = new HashMap<>();
            map.put(TAG_NAME, offline);
            downloadList.add(map);

            setList(false);
        } else {
            new JSONParse().execute();
        }
    }

    private void setList(boolean downloadable) {
        final ListView list = (ListView) findViewById(R.id.listView_main);
        final ListAdapter adapter = new SimpleAdapter(this, downloadList,
                android.R.layout.simple_list_item_1, new String[]{TAG_NAME},
                new int[]{android.R.id.text1});
        list.setAdapter(adapter);

        // Do nothing when there is no Internet
        if (!downloadable) {
            return;
        }
        // React when user click on item in the list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos,
                                    long id) {

                final Uri downloadUri = Uri.parse(downloadList.get(pos).get(TAG_URL));
                final String title = downloadList.get(pos).get(TAG_NAME);
                file = new File(Environment.getExternalStorageDirectory() + "/"
                        + localLoc + "/"
                        + downloadList.get(pos).get(TAG_FILENAME) + ".pdf");
                final Uri dst = Uri.fromFile(file);

                if (file.exists()) {
                    final Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                    pdfIntent.setDataAndType(dst, "application/pdf");
                    pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        startActivity(pdfIntent);
                    } catch (ActivityNotFoundException e) {
                        Utils.makeLongToast(Main.this, noPDF);
                        Log.e("ActivityNotFoundExcept", e.toString());
                    }
                    return;
                }
                // Download PDF
                final Request request = new Request(downloadUri);
                request.setTitle(title).setDestinationUri(dst);
                downloadID = downloadManager.enqueue(request);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }

    private class JSONParse extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show ProgressDialog while loading data from URL
            pDialog = new ProgressDialog(Main.this);
            pDialog.setMessage(getString(R.string.load_data));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            return JSONParser.getJSONFromUrl(jsonURL);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // Close ProgressDialog
            if (pDialog != null)
                pDialog.dismiss();
            
            if (json == null) {
                final HashMap<String, String> map = new HashMap<>();
                final String error = getString(R.string.except_json);
                map.put(TAG_NAME, error);
                downloadList.add(map);

                setList(false);
                return;
            }

            try {

                // Get JSON Array from URL
                final JSONArray j_plans = json.getJSONArray(TAG_TYPE);

                for (int i = 0; i < j_plans.length(); i++) {
                    final JSONObject c = j_plans.getJSONObject(i);

                    // Storing JSON item in a Variable
                    final String ver = c.getString(TAG_FILENAME);
                    final String name = c.getString(TAG_NAME);
                    final String api = c.getString(TAG_URL);

                    // Adding value HashMap key => value
                    final HashMap<String, String> map = new HashMap<>();
                    map.put(TAG_FILENAME, ver);
                    map.put(TAG_NAME, name);
                    map.put(TAG_URL, api);
                    downloadList.add(map);

                    setList(true);
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
    }
}
