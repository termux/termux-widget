package com.termux.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

/**
 * An activity to launch a shortcut. We want to a launch a service directly, but a shortcut
 * cannot be used to launch a service, only activities, so have to go through this activity.
 */
public class TermuxLaunchShortcutActivity extends Activity {

	static final String TOKEN_NAME = "com.termux.shortcut.token";

	public static String getGeneratedToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("token", Context.MODE_PRIVATE);
		String token = prefs.getString("token", null);
		if (token == null) {
			token = UUID.randomUUID().toString();
			prefs.edit().putString("token", token).commit();
		}
		return token;
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		String token = intent.getStringExtra(TOKEN_NAME);
		if (token == null || !token.equals(getGeneratedToken(this))) {
			Log.w("termux", "Strange token: " + token);
			Toast.makeText(this, R.string.bad_token_message, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		File clickedFile = new File(intent.getData().getPath());
		if (!clickedFile.canExecute()) {
			// Cover for the user if he forgot to mark the file executable:
			clickedFile.setExecutable(true);
		}

		Intent executeIntent = new Intent(TermuxWidgetProvider.ACTION_EXECUTE, getIntent().getData());
		executeIntent.setClassName("com.termux", TermuxWidgetProvider.TERMUX_SERVICE);
		startService(executeIntent);
		finish();
	}

}
