package de.nico.asura.activities;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.nico.asura.R;

public final class WebView1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        final WebView WebView = (WebView) findViewById(R.id.webView_main);
        WebSettings settings = WebView.getSettings();
        settings.setDomStorageEnabled(true);

        WebView.loadUrl(getString(R.string.menu_Web_1_url));
        WebView.getSettings().setJavaScriptEnabled(getResources().getBoolean(R.bool.menu_Web_1_js));

        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}