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
    private static final String KEY_COLOR_PREFIX = "color_";

    /** Background color follows the system dark mode. */
    public static final int COLOR_AUTO = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;
    /** Background color is taken from the wallpaper based system palette on Android 12+. */
    public static final int COLOR_WALLPAPER = 3;

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

    /** Get background color mode, one of the {@code COLOR_*} constants, for the widget. */
    public static int getColorMode(@NonNull Context context, int appWidgetId) {
        int mode = getSharedPreferences(context).getInt(KEY_COLOR_PREFIX + appWidgetId, COLOR_AUTO);
        return mode >= COLOR_AUTO && mode <= COLOR_WALLPAPER ? mode : COLOR_AUTO;
    }

    /** Set background color mode, one of the {@code COLOR_*} constants, for the widget. */
    public static void setColorMode(@NonNull Context context, int appWidgetId, int mode) {
        getSharedPreferences(context).edit()
                .putInt(KEY_COLOR_PREFIX + appWidgetId, mode)
                .apply();
    }

    /** Remove all skin settings of the widget. */
    public static void remove(@NonNull Context context, int appWidgetId) {
        getSharedPreferences(context).edit()
                .remove(KEY_ALPHA_PREFIX + appWidgetId)
                .remove(KEY_COLOR_PREFIX + appWidgetId)
                .apply();
    }

}
