package de.nico.asura;

/* 
 * Author: Nico Alt
 * See the file "LICENSE.txt" for the full license governing this code.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import de.nico.asura.activities.AuthWebView1;
import de.nico.asura.activities.Preferences;
import de.nico.asura.activities.WebView1;
import de.nico.asura.tools.JSONParser;
import de.nico.asura.tools.Utils;

public class Main extends Activity {

	// Data from JSON file
	private ArrayList<HashMap<String, String>> downloadList = new ArrayList<HashMap<String, String>>();

	// JSON Node Names
	private static final String TAG_TYPE = "plans";
	private static final String TAG_NAME = "name";
	private static final String TAG_FILENAME = "filename";
	private static final String TAG_URL = "url";

	private static String offline;
	private static String noPDF;
	private static String localLoc;
	private static String jsonURL;

	// For PDF Download
	private static DownloadManager downloadManager;
	private static long downloadID;
	private static File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {

		case R.id.action_prefs:
			startActivity(new Intent(this, Preferences.class));
			return true;

		case R.id.action_WebView1:
			if (!(Utils.isNetworkAvailable(this)))
				Utils.makeLongToast(this, offline);
			else
				startActivity(new Intent(this, WebView1.class));
			return true;

		case R.id.action_Link1:
			if (!(Utils.isNetworkAvailable(this)))
				Utils.makeLongToast(this, offline);
			else
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.menu_Link_1_url))));
			return true;

		case R.id.action_AuthWebView1:
			if (!(Utils.isNetworkAvailable(this)))
				Utils.makeLongToast(this, offline);
			else
				startActivity(new Intent(this, AuthWebView1.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}

	}

	private static void checkDir() {
		File dir = new File(Environment.getExternalStorageDirectory() + "/"
				+ localLoc + "/");
		if (!dir.exists())
			dir.mkdir();
	}

	private void update() {
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

		offline = getString(R.string.status_offline);
		noPDF = getString(R.string.except_nopdf);
		localLoc = getString(R.string.gen_loc);
		jsonURL = getString(R.string.gen_json);

		checkDir();

		// Parse the JSON file of the plans from the URL
		if (!(Utils.isNetworkAvailable(this))) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TAG_NAME, offline);
			downloadList.add(map);

			setList();

		} else
			new JSONParse().execute();
	}

	private void setList() {
		ListView list = (ListView) findViewById(R.id.listView_main);
		ListAdapter adapter = new SimpleAdapter(this, downloadList,
				android.R.layout.simple_list_item_1, new String[] { TAG_NAME },
				new int[] { android.R.id.text1 });
		list.setAdapter(adapter);

		// Do nothing when there is no Internet
		if (!(Utils.isNetworkAvailable(this))) {
			return;
		}
		// React when user click on item in the list
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {

				Uri downloadUri = Uri.parse(downloadList.get(pos).get(TAG_URL));
				String title = downloadList.get(pos).get(TAG_NAME);
				file = new File(Environment.getExternalStorageDirectory() + "/"
						+ localLoc + "/"
						+ downloadList.get(pos).get(TAG_FILENAME) + ".pdf");
				Uri dest = Uri.fromFile(file);

				if (file.exists()) {
					Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
					pdfIntent.setDataAndType(dest, "application/pdf");
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					try {
						startActivity(pdfIntent);
					} catch (ActivityNotFoundException e) {
						Utils.makeLongToast(Main.this, noPDF);
						Log.e("ActivityNotFoundException", e.toString());
					}
					return;
				}

				// Download PDF
				Request request = new Request(downloadUri);
				request.setTitle(title).setDestinationUri(dest);
				downloadID = downloadManager.enqueue(request);
			}

		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(downloadReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(downloadReceiver);
	}

	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadID);
			Cursor cursor = downloadManager.query(query);

			if (cursor.moveToFirst()) {
				int columnIndex = cursor
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);

				// Success?
				if (status == DownloadManager.STATUS_SUCCESSFUL) {

					Uri path = Uri.fromFile(file);
					Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
					pdfIntent.setDataAndType(path, "application/pdf");
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					try {
						startActivity(pdfIntent);
					} catch (ActivityNotFoundException e) {
						Utils.makeLongToast(Main.this, noPDF);
						Log.e("ActivityNotFoundException", e.toString());
					}

				} else if (status == DownloadManager.STATUS_FAILED) {
					Utils.makeLongToast(Main.this,
							getString(R.string.down_error));

				} else if (status == DownloadManager.STATUS_PAUSED) {
					Utils.makeLongToast(Main.this,
							getString(R.string.down_paused));

				} else if (status == DownloadManager.STATUS_PENDING) {
					Utils.makeLongToast(Main.this,
							getString(R.string.down_pending));

				} else if (status == DownloadManager.STATUS_RUNNING) {
					Utils.makeLongToast(Main.this,
							getString(R.string.down_running));

				}

			}
		}

	};

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

			JSONObject json = JSONParser.getJSONFromUrl(jsonURL);

			return json;

		}

		@Override
		protected void onPostExecute(JSONObject json) {

			// Close ProgressDialog
			pDialog.dismiss();

			try {

				// Get JSON Array from URL
				JSONArray j_plans = json.getJSONArray(TAG_TYPE);

				for (int i = 0; i < j_plans.length(); i++) {
					JSONObject c = j_plans.getJSONObject(i);

					// Storing JSON item in a Variable
					String ver = c.getString(TAG_FILENAME);
					String name = c.getString(TAG_NAME);
					String api = c.getString(TAG_URL);

					// Adding value HashMap key => value
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(TAG_FILENAME, ver);
					map.put(TAG_NAME, name);
					map.put(TAG_URL, api);
					downloadList.add(map);

					setList();

				}

			}

			catch (JSONException e) {
				Log.e("JSONException", e.toString());

			}

		}

	}
}