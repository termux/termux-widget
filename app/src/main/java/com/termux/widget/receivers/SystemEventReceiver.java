package com.termux.widget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.shared.data.IntentUtils;
import com.termux.shared.logger.Logger;
import com.termux.widget.TermuxWidgetProvider;

public class SystemEventReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = "SystemEventReceiver";

    @Override
    public void onReceive(@NonNull Context context, @Nullable Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        long upTime = SystemClock.uptimeMillis();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Logger.logInfo(LOG_TAG, "Received intent at " + upTime + "ms since boot:\n" + IntentUtils.getIntentString(intent));
        } else {
            Logger.logDebug(LOG_TAG, "Received intent:\n" + IntentUtils.getIntentString(intent));
        }

        if (action == null) return;

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
            case Intent.ACTION_MY_PACKAGE_UNSUSPENDED:
                sendIntentToRefreshAllWidgets(context);
                break;
            default:
                Logger.logError(LOG_TAG, "Invalid action \"" + action + "\" passed to " + LOG_TAG);
        }
    }


    public synchronized void sendIntentToRefreshAllWidgets(@NonNull Context context) {
        TermuxWidgetProvider.sendIntentToRefreshAllWidgets(context, LOG_TAG);
    }

}
