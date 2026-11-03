package com.termux.widget.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

        setAlphaViews();

        Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(v -> saveAndFinish());
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
        int percent = mAlphaSeekBar.getProgress();
        Logger.logDebug(LOG_TAG, "Saving background opacity " + percent + "% for widget id: " + mAppWidgetId);

        WidgetSkinPrefs.setAlphaPercent(this, mAppWidgetId, percent);
        TermuxWidgetProvider.updateAppWidgetRemoteViews(this, AppWidgetManager.getInstance(this), mAppWidgetId);

        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
        finish();
    }

}
