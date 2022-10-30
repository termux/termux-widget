package com.termux.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TermuxWidgetControlExecutorReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "TermuxWidgetControlExecutorReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Set log level for the receiver
        TermuxWidgetApplication.setLogConfig(context, false);

        TermuxWidgetProvider.handleTermuxShortcutExecutionIntent(context, intent, LOG_TAG);
    }

}
