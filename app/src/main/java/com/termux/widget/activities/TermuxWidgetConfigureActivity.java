package com.termux.widget.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.termux.shared.activity.media.AppCompatActivityUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.theme.NightMode;
import com.termux.widget.R;
import com.termux.widget.TermuxWidgetProvider;
import com.termux.widget.WidgetSkinPrefs;

/**
 * Widget settings activity. Opened by the launcher when the widget is reconfigured (long press ->
 * settings) or by clicking the widget title.
 */
public class TermuxWidgetConfigureActivity extends AppCompatActivity {

    public static final String LOG_TAG = "TermuxWidgetConfigureActivity";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private RadioGroup mColorRadioGroup;
    private RadioGroup mTextRadioGroup;
    private TextView mAlphaValueTextView;
    private SeekBar mAlphaSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.logDebug(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_termux_widget_configure);

        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().getName(), true);

        // If user backs out of the activity, then the launcher should treat it as cancelled.
        setResult(RESULT_CANCELED);

        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Logger.logError(LOG_TAG, "Invalid widget id received, finishing");
            finish();
            return;
        }

        Logger.logDebug(LOG_TAG, "Configuring widget id: " + mAppWidgetId);

        setColorViews();
        setTextViews();
        setAlphaViews();

        Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(v -> saveAndFinish());
    }

    private void setColorViews() {
        mColorRadioGroup = findViewById(R.id.radio_group_color);

        // Wallpaper colors are only available on Android 12+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            findViewById(R.id.radio_color_wallpaper).setVisibility(View.GONE);

        switch (WidgetSkinPrefs.getColorMode(this, mAppWidgetId)) {
            case WidgetSkinPrefs.COLOR_WHITE:
                mColorRadioGroup.check(R.id.radio_color_white);
                break;
            case WidgetSkinPrefs.COLOR_BLACK:
                mColorRadioGroup.check(R.id.radio_color_black);
                break;
            case WidgetSkinPrefs.COLOR_WALLPAPER:
                mColorRadioGroup.check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                        R.id.radio_color_wallpaper : R.id.radio_color_auto);
                break;
            default:
                mColorRadioGroup.check(R.id.radio_color_auto);
                break;
        }
    }

    private int getCheckedColorMode() {
        int checkedId = mColorRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_color_white) return WidgetSkinPrefs.COLOR_WHITE;
        if (checkedId == R.id.radio_color_black) return WidgetSkinPrefs.COLOR_BLACK;
        if (checkedId == R.id.radio_color_wallpaper) return WidgetSkinPrefs.COLOR_WALLPAPER;
        return WidgetSkinPrefs.COLOR_AUTO;
    }

    private void setTextViews() {
        mTextRadioGroup = findViewById(R.id.radio_group_text);
        switch (WidgetSkinPrefs.getTextMode(this, mAppWidgetId)) {
            case WidgetSkinPrefs.TEXT_WHITE:
                mTextRadioGroup.check(R.id.radio_text_white);
                break;
            case WidgetSkinPrefs.TEXT_BLACK:
                mTextRadioGroup.check(R.id.radio_text_black);
                break;
            default:
                mTextRadioGroup.check(R.id.radio_text_auto);
                break;
        }
    }

    private int getCheckedTextMode() {
        int checkedId = mTextRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_text_white) return WidgetSkinPrefs.TEXT_WHITE;
        if (checkedId == R.id.radio_text_black) return WidgetSkinPrefs.TEXT_BLACK;
        return WidgetSkinPrefs.TEXT_AUTO;
    }

    private void setAlphaViews() {
        mAlphaValueTextView = findViewById(R.id.text_alpha_value);
        mAlphaSeekBar = findViewById(R.id.seekbar_alpha);

        int percent = WidgetSkinPrefs.getAlphaPercent(this, mAppWidgetId);
        mAlphaSeekBar.setProgress(percent);
        setAlphaValueText(percent);

        mAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setAlphaValueText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setAlphaValueText(int percent) {
        mAlphaValueTextView.setText(getString(R.string.label_widget_background_opacity, percent));
    }

    private void saveAndFinish() {
        int colorMode = getCheckedColorMode();
        int textMode = getCheckedTextMode();
        int percent = mAlphaSeekBar.getProgress();
        Logger.logDebug(LOG_TAG, "Saving background color mode " + colorMode + " and text color mode " + textMode + " and opacity " + percent + "% for widget id: " + mAppWidgetId);

        WidgetSkinPrefs.setColorMode(this, mAppWidgetId, colorMode);
        WidgetSkinPrefs.setTextMode(this, mAppWidgetId, textMode);
        WidgetSkinPrefs.setAlphaPercent(this, mAppWidgetId, percent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        TermuxWidgetProvider.updateAppWidgetRemoteViews(this, appWidgetManager, mAppWidgetId);
        // List item text colors are set by TermuxWidgetService, so reload the items too.
        appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.widget_list);

        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
        finish();
    }

}
