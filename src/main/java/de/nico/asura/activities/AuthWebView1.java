package de.nico.asura.activities;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import de.nico.asura.R;
import de.nico.asura.tools.Utils;

public final class AuthWebView1 extends Activity {

    private static SharedPreferences prefs;
    private static String firstField;
    private static String secondField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check password
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        firstField = prefs.getString("firstFirst", "0");
        secondField = prefs.getString("firstSecond", "0");

        if (firstField.equals("0") || secondField.equals("0"))
            checkLogin();
        else
            openWebView();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.authwebview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                resetLogin();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openWebView() {
        setContentView(R.layout.webview);
        final WebView webView = (WebView) findViewById(R.id.webView_main);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(getString(R.string.menu_AuthWeb_1_url));
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(getResources().getBoolean(R.bool.menu_AuthWeb_1_js));
    }

    private void checkLogin() {
        // Layout for Dialog
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edit_name = new EditText(this);
        edit_name.setHint(getString(R.string.menu_AuthWeb_1_fiFi));
        layout.addView(edit_name);

        final EditText edit_pass = new EditText(this);
        edit_pass.setHint(getString(R.string.menu_AuthWeb_1_seFi));
        edit_pass.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edit_pass);

        final Builder builder = new Builder(this);
        builder.setTitle(getString(R.string.menu_AuthWeb_1_name))
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                firstField = edit_name.getText().toString();
                                secondField = edit_pass.getText().toString();

                                // Nothing?
                                if (firstField.length() == 0
                                        || secondField.length() == 0) {
                                    Utils.makeShortToast(AuthWebView1.this,
                                            getString(R.string.wrong));
                                    checkLogin();
                                    return;

                                }

                                final Editor editor = prefs.edit();
                                editor.putString("firstFirst", firstField);
                                editor.putString("firstSecond", secondField);
                                editor.apply();

                                openWebView();

                            }

                        })
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                AuthWebView1.this.finish();

                            }

                        }).show();
    }

    private void resetLogin() {
        final Editor editor = prefs.edit();
        editor.putString("name", "0");
        editor.putString("pass", "0");
        editor.apply();
        checkLogin();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler, String host, String realm) {
            handler.proceed(firstField, secondField);
        }
    }
}