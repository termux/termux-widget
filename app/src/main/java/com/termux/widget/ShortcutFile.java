package com.termux.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.common.base.Joiner;
import com.termux.shared.file.FileUtils;
import com.termux.shared.file.TermuxFileUtils;
import com.termux.shared.file.filesystem.FileType;
import com.termux.shared.logger.Logger;
import com.termux.shared.settings.preferences.TermuxWidgetAppSharedPreferences;
import com.termux.shared.shell.ShellUtils;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_SERVICE;
import com.termux.shared.termux.TermuxConstants.TERMUX_WIDGET.TERMUX_WIDGET_PROVIDER;
import com.termux.shared.termux.TermuxConstants.TERMUX_WIDGET;
import com.termux.shared.termux.TermuxConstants;
import com.termux.widget.utils.ShortcutUtils;

import java.io.File;

public final class ShortcutFile {

    private static final String LOG_TAG = "ShortcutFile";

    public final File file;
    public final String label;

    public ShortcutFile(File file, int depth) {
        this.file = file;
        this.label = (depth > 0 ? (file.getParentFile().getName() + "/") : "")
                + file.getName();
    }

    public Intent getExecutionIntent(Context context) {
        String path = file.getAbsolutePath();
        Uri scriptUri = new Uri.Builder().scheme(TERMUX_SERVICE.URI_SCHEME_SERVICE_EXECUTE).path(path).build();
        Intent executionIntent = new Intent(context, TermuxLaunchShortcutActivity.class);
        executionIntent.setAction(TERMUX_SERVICE.ACTION_SERVICE_EXECUTE); // Mandatory for pinned shortcuts
        executionIntent.setData(scriptUri);
        executionIntent.putExtra(TERMUX_WIDGET.EXTRA_TOKEN_NAME, TermuxWidgetAppSharedPreferences.getGeneratedToken(context));
        return executionIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public ShortcutInfo getShortcutInfo(Context context) {
        String path = file.getAbsolutePath();

        ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, path);
        builder.setIntent(this.getExecutionIntent(context));
        builder.setShortLabel(this.label);

        // Set icon if existent.
        File shortcutIconFile = TermuxCreateShortcutActivity.getShortcutIconFile(context, ShellUtils.getExecutableBasename(path));
        if (shortcutIconFile != null)
            builder.setIcon(Icon.createWithBitmap(((BitmapDrawable) Drawable.createFromPath(shortcutIconFile.getAbsolutePath())).getBitmap()));
        else
            builder.setIcon(Icon.createWithResource(context, R.drawable.ic_launcher));

        return builder.build();
    }

    public RemoteViews getListWidgetView(Context context) {
        // Position will always range from 0 to getCount() - 1.
        // Construct remote views item based on the item xml file and set text based on position.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item, this.label);

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in TermuxAppWidgetProvider.
        Intent fillInIntent = new Intent().putExtra(TERMUX_WIDGET_PROVIDER.EXTRA_FILE_CLICKED, this.file.getAbsolutePath());
        rv.setOnClickFillInIntent(R.id.widget_item_layout, fillInIntent);

        return rv;
    }

    @Nullable
    private File getIconFile(Context context) {
        String errmsg;
        String shortcutIconFilePath = FileUtils.getCanonicalPath(
                TermuxConstants.TERMUX_SHORTCUT_SCRIPT_ICONS_DIR_PATH +
                "/" + ShellUtils.getExecutableBasename(file.getAbsolutePath()) + ".png", null);

        FileType fileType = FileUtils.getFileType(shortcutIconFilePath, true);
        //  Ensure file or symlink points to a regular file that exists
        if (fileType != FileType.REGULAR) {
            if (fileType != FileType.NO_EXIST) {
                errmsg = context.getString(R.string.error_icon_not_a_regular_file, fileType.getName()) +
                        "\n" + context.getString(R.string.msg_icon_absolute_path, shortcutIconFilePath);
                Logger.logErrorAndShowToast(context, LOG_TAG, errmsg);
            }
            return null;
        }

        // Do not allow shortcut icons files not under SHORTCUT_ICONS_FILES_ALLOWED_PATHS_LIST
        if (!FileUtils.isPathInDirPaths(shortcutIconFilePath, ShortcutUtils.SHORTCUT_ICONS_FILES_ALLOWED_PATHS_LIST, true)) {
            errmsg = context.getString(R.string.error_icon_not_under_shortcut_icons_directories,
                    Joiner.on(", ").skipNulls().join(TermuxFileUtils.getUnExpandedTermuxPaths(ShortcutUtils.SHORTCUT_ICONS_FILES_ALLOWED_PATHS_LIST))) +
                    "\n" + context.getString(R.string.msg_icon_absolute_path, shortcutIconFilePath);
            Logger.logErrorAndShowToast(context, LOG_TAG, errmsg);
            return null;
        }

        Logger.logInfo(LOG_TAG, "Using file at \"" + shortcutIconFilePath + "\" as shortcut icon file");
        Logger.showToast(context, context.getString(R.string.msg_shortcut_icon_file_used, shortcutIconFilePath), true);

        return new File(shortcutIconFilePath);
    }

}
