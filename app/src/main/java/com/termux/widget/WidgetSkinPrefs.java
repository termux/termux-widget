package com.termux.widget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Per widget skin settings, stored by appWidgetId in a separate {@link SharedPreferences} file so
 * that termux-shared preferences are not touched.
 */
public final class WidgetSkinPrefs {

    private static final String PREFERENCES_NAME = "widget_skin";

    private static final String KEY_ALPHA_PREFIX = "alpha_";

    /** Default background opacity in percent. */
    public static final int DEFAULT_ALPHA_PERCENT = 100;

    private WidgetSkinPrefs() {}

    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /** Get background opacity in percent (0-100) for the widget. */
    public static int getAlphaPercent(@NonNull Context context, int appWidgetId) {
        int percent = getSharedPreferences(context).getInt(KEY_ALPHA_PREFIX + appWidgetId, DEFAULT_ALPHA_PERCENT);
        return Math.max(0, Math.min(100, percent));
    }

    /** Set background opacity in percent (0-100) for the widget. */
    public static void setAlphaPercent(@NonNull Context context, int appWidgetId, int percent) {
        getSharedPreferences(context).edit()
                .putInt(KEY_ALPHA_PREFIX + appWidgetId, Math.max(0, Math.min(100, percent)))
                .apply();
    }

    /** Remove all skin settings of the widget. */
    public static void remove(@NonNull Context context, int appWidgetId) {
        getSharedPreferences(context).edit()
                .remove(KEY_ALPHA_PREFIX + appWidgetId)
                .apply();
    }

}
