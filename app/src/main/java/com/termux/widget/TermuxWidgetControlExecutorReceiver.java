package com.termux.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static com.termux.widget.TermuxLaunchShortcutActivity.TOKEN_NAME;
import static com.termux.widget.TermuxLaunchShortcutActivity.getGeneratedToken;

public class TermuxWidgetControlExecutorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String token = intent.getStringExtra(TOKEN_NAME);
        if (token == null || !token.equals(getGeneratedToken(context))) {
            Log.w("termux", "Strange token: " + token);
            Toast.makeText(context, R.string.bad_token_message, Toast.LENGTH_LONG).show();
            return;
        }
        TermuxWidgetProvider.handleTermuxShortcutExecuteIntent(context, intent);
    }
}
