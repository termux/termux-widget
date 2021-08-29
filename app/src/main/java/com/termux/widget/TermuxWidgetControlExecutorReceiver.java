package com.termux.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;

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

        File shortcutFile = new File(intent.getData().getPath());
        TermuxWidgetProvider.ensureFileReadableAndExecutable(shortcutFile);

        // Do not use the intent data passed in, since that may be an old one with a file:// uri
        // which is not allowed starting with Android 7.
        Uri scriptUri = new Uri.Builder().scheme("com.termux.file").path(shortcutFile.getAbsolutePath()).build();

        Intent executeIntent = new Intent(TermuxWidgetProvider.ACTION_EXECUTE, scriptUri);
        executeIntent.setClassName("com.termux", TermuxWidgetProvider.TERMUX_SERVICE);
        if (shortcutFile.getParentFile().getName().equals("tasks")) {
            executeIntent.putExtra("com.termux.execute.background", true);
            // Show feedback for executed background task.
            String message = "Task executed: " + shortcutFile.getName();
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        TermuxWidgetProvider.startTermuxService(context, executeIntent);
    }
}
