package com.termux.widget;

import android.app.Application;
import android.content.Context;

import com.termux.shared.logger.Logger;
import com.termux.shared.termux.crash.TermuxCrashUtils;
import com.termux.shared.termux.settings.preferences.TermuxWidgetAppSharedPreferences;
import com.termux.shared.termux.theme.TermuxThemeUtils;

public class TermuxWidgetApplication extends Application {

    public void onCreate() {
        super.onCreate();

        // Set crash handler for the app
        TermuxCrashUtils.setCrashHandler(this);

        // Set log level for the app
        setLogLevel(getApplicationContext(), true);

        Logger.logDebug("Starting Application");

        // Set NightMode.APP_NIGHT_MODE
        TermuxThemeUtils.setAppNightMode(context);
    }

    public static void setLogLevel(Context context, boolean commitToFile) {
        // Load the log level from shared preferences and set it to the {@link Logger.CURRENT_LOG_LEVEL}
        TermuxWidgetAppSharedPreferences preferences = TermuxWidgetAppSharedPreferences.build(context);
        if (preferences == null) return;
        preferences.setLogLevel(null, preferences.getLogLevel(true), commitToFile);
    }

}
