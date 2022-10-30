package com.termux.widget;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.crash.TermuxCrashUtils;
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences;
import com.termux.shared.termux.theme.TermuxThemeUtils;

public class TermuxWidgetApplication extends Application {

    public static final String LOG_TAG = "TermuxWidgetApplication";

    public void onCreate() {
        super.onCreate();

        Log.i(LOG_TAG, "AppInit");

        Context context = getApplicationContext();

        // Set crash handler for the app
        TermuxCrashUtils.setCrashHandler(context);

        // Set log config for the app
        setLogConfig(context, true);

        // Set NightMode.APP_NIGHT_MODE
        TermuxThemeUtils.setAppNightMode(context);
    }

    public static void setLogConfig(Context context, boolean commitToFile) {
        Logger.setDefaultLogTag(TermuxConstants.TERMUX_WIDGET_APP_NAME.replaceAll("[: ]", ""));

        // Load the log level from shared preferences and set it to the {@link Logger.CURRENT_LOG_LEVEL}
        TermuxWidgetAppSharedPreferences preferences = TermuxWidgetAppSharedPreferences.build(context);
        if (preferences == null) return;
        preferences.setLogLevel(null, preferences.getLogLevel(true), commitToFile);
    }

}
