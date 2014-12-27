package de.nico.asura.activities;

/* 
 * Author: Nico Alt
 * See the file "LICENSE.txt" for the full license governing this code.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.webkit.WebView;
import de.nico.asura.R;

public class WebView1 extends Activity {

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		// Check whether JavaScript should be enabled
		Resources res = getResources();
		boolean js = res.getBoolean(R.bool.menu_Web_1_js);

		WebView WebView = (WebView) findViewById(R.id.webView_main);
		WebView.loadUrl(getString(R.string.menu_Web_1_url));
		WebView.getSettings().setJavaScriptEnabled(js);

		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}