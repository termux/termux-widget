package com.termux.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.termux.shared.activity.media.AppCompatActivityUtils;
import com.termux.shared.data.DataUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.TermuxUtils;
import com.termux.widget.utils.ShortcutUtils;

import java.io.File;
import java.util.Arrays;

public class TermuxCreateShortcutActivity extends AppCompatActivity {

    private ListView mListView;
    private File mCurrentDirectory;
    private File[] mCurrentFiles;

    private static final String LOG_TAG = "TermuxCreateShortcutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termux_create_shortcut);
        mListView = findViewById(R.id.list);

        AppCompatActivityUtils.setToolbar(this, com.termux.shared.R.id.toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String errmsg = TermuxUtils.isTermuxAppAccessible(this);
        if (errmsg != null) {
            Logger.logErrorAndShowToast(this, LOG_TAG, errmsg);
            finish();
            return;
        }

        updateListview(TermuxConstants.TERMUX_SHORTCUT_SCRIPTS_DIR);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            final Context context = TermuxCreateShortcutActivity.this;
            File clickedFile = mCurrentFiles[position];
            if (clickedFile.isDirectory()) {
                updateListview(clickedFile);
            } else {
                createShortcut(context, clickedFile);
                finish();
            }
        });
    }

    private void updateListview(File directory) {
        mCurrentDirectory = directory;
        mCurrentFiles = directory.listFiles(ShortcutUtils.SHORTCUT_FILES_FILTER);

        if (mCurrentFiles == null) mCurrentFiles = new File[0];

        Arrays.sort(mCurrentFiles, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        final boolean isTopDir = directory.equals(TermuxConstants.TERMUX_SHORTCUT_SCRIPTS_DIR);
        AppCompatActivityUtils.setShowBackButtonInActionBar(this, !isTopDir);

        if (isTopDir && mCurrentFiles.length == 0) {
            // Create if necessary so user can more easily add.
            TermuxConstants.TERMUX_SHORTCUT_SCRIPTS_DIR.mkdirs();
            new AlertDialog.Builder(this)
                    .setMessage(R.string.msg_no_shortcut_scripts)
                    .setOnDismissListener(dialog -> finish()).show();
            return;
        }

        final String[] values = new String[mCurrentFiles.length];
        for (int i = 0; i < values.length; i++)
            values[i] = mCurrentFiles[i].getName() +
                    (mCurrentFiles[i].isDirectory() ? "/" : "");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            updateListview(DataUtils.getDefaultIfNull(mCurrentDirectory.getParentFile(), mCurrentDirectory));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void createShortcut(Context context, File clickedFile) {
        boolean isPinnedShortcutSupported = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = ShortcutUtils.getShortcutManager(context, LOG_TAG, true);
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported())
                isPinnedShortcutSupported = true;
        }

        ShortcutFile shortcutFile = new ShortcutFile(clickedFile);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPinnedShortcutSupported)
                createPinnedShortcut(context, shortcutFile);
            else
                createStaticShortcut(context, shortcutFile);
        } catch (Exception e) {
            String message = context.getString(
                    isPinnedShortcutSupported ? R.string.error_create_pinned_shortcut_failed : R.string.error_create_static_shortcut_failed,
                    shortcutFile.getUnExpandedPath());
            Logger.logErrorAndShowToast(context, LOG_TAG, message + ": " + e.getMessage());
            Logger.logStackTraceWithMessage(LOG_TAG, message, e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createPinnedShortcut(Context context, ShortcutFile shortcutFile) {
        ShortcutManager shortcutManager = ShortcutUtils.getShortcutManager(context, LOG_TAG, true);
        if (shortcutManager == null) return;

        Logger.showToast(context, context.getString(R.string.msg_request_create_pinned_shortcut,
                shortcutFile.getUnExpandedPath()), true);
        shortcutManager.requestPinShortcut(shortcutFile.getShortcutInfo(context, true), null);
    }

    private void createStaticShortcut(Context context, ShortcutFile shortcutFile) {
        Logger.showToast(context, context.getString(R.string.msg_request_create_static_shortcut,
                shortcutFile.getUnExpandedPath()), true);
        setResult(RESULT_OK, shortcutFile.getStaticShortcutIntent(context));
    }

}
