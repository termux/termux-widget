package com.termux.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

/**
 * Widget colors resolved from {@link WidgetSkinPrefs}, with separate colors for light and dark
 * system mode.
 *
 * One UI Home 7.0+ blurs the wallpaper behind the {@link android.R.id#background} root view and
 * tints it with the view background color if its alpha is between 1 and 254 and the provider info
 * declares {@code app:widgetStyle} and {@code app:widgetSize}, so background alpha is kept in that
 * range. Other launchers just draw the translucent color.
 */
public final class WidgetSkin {

    /** Lowest and highest background alpha for which One UI Home still applies its blur. */
    private static final int MIN_BACKGROUND_ALPHA = 1;
    private static final int MAX_BACKGROUND_ALPHA = 254;

    @ColorInt public final int backgroundLight;
    @ColorInt public final int backgroundDark;
    @ColorInt public final int textPrimaryLight;
    @ColorInt public final int textPrimaryDark;
    @ColorInt public final int textSecondaryLight;
    @ColorInt public final int textSecondaryDark;

    private WidgetSkin(int backgroundLight, int backgroundDark,
                       int textPrimaryLight, int textPrimaryDark,
                       int textSecondaryLight, int textSecondaryDark) {
        this.backgroundLight = backgroundLight;
        this.backgroundDark = backgroundDark;
        this.textPrimaryLight = textPrimaryLight;
        this.textPrimaryDark = textPrimaryDark;
        this.textSecondaryLight = textSecondaryLight;
        this.textSecondaryDark = textSecondaryDark;
    }

    @NonNull
    public static WidgetSkin get(@NonNull Context context, int appWidgetId) {
        int bgLight = context.getColor(R.color.widget_bg_light);
        int bgDark = context.getColor(R.color.widget_bg_dark);
        int onLight = context.getColor(R.color.widget_text_primary_light);
        int onDark = context.getColor(R.color.widget_text_primary_dark);
        int onLightSecondary = context.getColor(R.color.widget_text_secondary_light);
        int onDarkSecondary = context.getColor(R.color.widget_text_secondary_dark);

        int alpha = Math.round(WidgetSkinPrefs.getAlphaPercent(context, appWidgetId) * 255f / 100f);
        alpha = Math.max(MIN_BACKGROUND_ALPHA, Math.min(MAX_BACKGROUND_ALPHA, alpha));

        switch (WidgetSkinPrefs.getColorMode(context, appWidgetId)) {
            case WidgetSkinPrefs.COLOR_WHITE:
                bgDark = bgLight;
                onDark = onLight;
                onDarkSecondary = onLightSecondary;
                break;
            case WidgetSkinPrefs.COLOR_BLACK:
                bgLight = bgDark = context.getColor(R.color.widget_bg_black);
                onLight = onDark;
                onLightSecondary = onDarkSecondary;
                break;
            case WidgetSkinPrefs.COLOR_WALLPAPER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Resolved now, so a new wallpaper is picked up on the next widget update.
                    bgLight = context.getColor(android.R.color.system_accent1_100);
                    bgDark = context.getColor(android.R.color.system_accent1_800);
                }
                break;
            default:
                break;
        }

        // Explicit text colors stay the same across all backgrounds and system modes.
        switch (WidgetSkinPrefs.getTextMode(context, appWidgetId)) {
            case WidgetSkinPrefs.TEXT_WHITE:
                onLight = onDark = context.getColor(R.color.widget_text_white);
                onLightSecondary = onDarkSecondary = context.getColor(R.color.widget_text_white_secondary);
                break;
            case WidgetSkinPrefs.TEXT_BLACK:
                onLight = onDark = context.getColor(R.color.widget_text_black);
                onLightSecondary = onDarkSecondary = context.getColor(R.color.widget_text_black_secondary);
                break;
            default:
                break;
        }

        return new WidgetSkin(withAlpha(bgLight, alpha), withAlpha(bgDark, alpha),
                onLight, onDark, onLightSecondary, onDarkSecondary);
    }

    /**
     * Set a color with a {@link RemoteViews} int method. On Android 12+ the launcher picks the
     * light or dark color when applying the views, so it follows dark mode changes without an
     * update. On older versions the color for the current mode is used.
     */
    public static void setColor(@NonNull Context context, @NonNull RemoteViews remoteViews, @IdRes int viewId,
                                @NonNull String methodName, @ColorInt int lightColor, @ColorInt int darkColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            remoteViews.setColorInt(viewId, methodName, lightColor, darkColor);
        } else {
            int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            remoteViews.setInt(viewId, methodName, nightMode == Configuration.UI_MODE_NIGHT_YES ? darkColor : lightColor);
        }
    }

    @ColorInt
    private static int withAlpha(@ColorInt int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

}
