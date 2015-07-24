package de.nico.asura.activities;

/* 
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import de.nico.asura.R;
import de.nico.asura.tools.Utils;

public final class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        checkPrefs();

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

    private void checkPrefs() {
        // Dial the telephone number
        findPreference("pref_contact_tel").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return Utils.dial(Preferences.this);
                    }
                });

        // Send an E-Mail
        findPreference("pref_contact_mail").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return Utils.sendMail(Preferences.this);

                    }

                });

        // Open a map
        findPreference("pref_contact_address").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return Utils.openInMap(Preferences.this);

                    }

                });

        // Creates a new Intent to insert a contact
        findPreference("pref_contact_addcontact").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return Utils.addAsContact(Preferences.this);
                    }
                });
    }
}