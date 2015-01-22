package de.nico.asura.tools;

/* 
 * Author: Nico Alt
 * See the file "LICENSE.txt" for the full license governing this code.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;
import de.nico.asura.R;

public class Utils {

	public static final void makeShortToast(Context c, String msg) {
		Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
	}

	public static final void makeLongToast(Context c, String msg) {
		Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
	}

	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static final boolean addAsContact(Context c) {
		// Create Intent
		Intent i = new Intent(Intents.Insert.ACTION);

		// Set Data for Contact
		i.setType(RawContacts.CONTENT_TYPE)
				.putExtra(Insert.NAME, c.getString(R.string.contacts_name))
				.putExtra(Insert.EMAIL, c.getString(R.string.contacts_mail))
				.putExtra(Insert.EMAIL_TYPE, Email.TYPE_WORK)
				.putExtra(Insert.PHONE, c.getString(R.string.contacts_number_1))
				.putExtra(Insert.PHONE_TYPE, Phone.TYPE_WORK)
				.putExtra(Insert.SECONDARY_PHONE,
						c.getString(R.string.contacts_number_2))
				.putExtra(Insert.SECONDARY_PHONE_TYPE, Phone.TYPE_FAX_WORK)
				.putExtra(Insert.POSTAL, c.getString(R.string.contacts_address))
				.putExtra(Insert.POSTAL_TYPE, Phone.TYPE_WORK);
		try {
			c.startActivity(i);
			return true;
		} catch (ActivityNotFoundException e) {
			Utils.makeLongToast(c, c.getString(R.string.except_contacts));
			Log.e("ActivityNotFoundException", e.toString());
			return false;
		}
	}

	public static final boolean dial(Context c) {
		// Create Intent
		Intent i = new Intent(Intent.ACTION_DIAL);

		// Set telephone number as data for Intent
		i.setData(Uri.parse("tel:" + c.getString(R.string.intent_number)));

		// Create chooser for Intent
		Intent icc = Intent.createChooser(i, c.getString(R.string.intent_call));

		try {
			c.startActivity(icc);
			return true;
		} catch (ActivityNotFoundException e) {
			Utils.makeLongToast(c, c.getString(R.string.except_dial));
			Log.e("ActivityNotFoundException", e.toString());
			return false;
		}
	}

	public static final boolean sendMail(Context c) {
		// Get E-Mail address from config.xml
		String mail = c.getString(R.string.intent_mail);

		// Convert address to Uri
		Uri u = Uri.fromParts("mailto", mail, null);

		// Create Intent
		Intent i = new Intent(Intent.ACTION_SENDTO, u);

		// Create chooser for Intent
		Intent icc = Intent.createChooser(i,
				c.getString(R.string.intent_mail_tit));

		try {
			c.startActivity(icc);
			return true;
		} catch (ActivityNotFoundException e) {
			Utils.makeLongToast(c, c.getString(R.string.except_mail));
			Log.e("ActivityNotFoundException", e.toString());
			return false;
		}
	}

	public static final boolean openInMap(Context c) {
		// Get address from config.xml
		Uri address = Uri.parse(c.getString(R.string.intent_address));

		// Create Intent
		Intent i = new Intent(Intent.ACTION_VIEW, address);

		try {
			c.startActivity(i);
			return true;
		} catch (ActivityNotFoundException e) {
			Utils.makeLongToast(c, c.getString(R.string.except_map));
			Log.e("ActivityNotFoundException", e.toString());
			return false;
		}

	}

}