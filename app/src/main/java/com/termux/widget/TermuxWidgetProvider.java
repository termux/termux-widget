package com.termux.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.termux.shared.data.DataUtils;
import com.termux.shared.data.IntentUtils;
import com.termux.shared.file.FileUtils;
import com.termux.shared.file.filesystem.FileType;
import com.termux.shared.logger.Logger;
import com.termux.shared.models.ExecutionCommand;
import com.termux.shared.models.ResultData;
import com.termux.shared.models.errors.Error;
import com.termux.shared.settings.preferences.TermuxWidgetAppSharedPreferences;
import com.termux.shared.shell.ShellUtils;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_SERVICE;
import com.termux.shared.termux.TermuxConstants.TERMUX_WIDGET.TERMUX_WIDGET_PROVIDER;
import com.termux.shared.termux.TermuxUtils;

import java.io.File;

/**
 * Widget providing a list to launch scripts in ~/.shortcuts/.
 * <p>
 * See https://developer.android.com/guide/topics/appwidgets/index.html
 */
public final class TermuxWidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = "TermuxWidgetProvider";

    public void onEnabled(Context context) {
        String errmsg = TermuxUtils.isTermuxAppAccessible(context);
        if (errmsg != null) {
            Logger.logErrorAndShowToast(context, LOG_TAG, errmsg);
        }
    }

    /**
     * "This is called to update the App Widget at intervals defined by the updatePeriodMillis attribute in the
     * AppWidgetProviderInfo (see Adding the AppWidgetProviderInfo Metadata above). This method is also called when the
     * user adds the App Widget, so it should perform the essential setup, such as define event handlers for Views and
     * start a temporary Service, if necessary. However, if you have declared a configuration Activity, this method is
     * not called when the user adds the App Widget, but is called for the subsequent updates. It is the responsibility
     * of the configuration Activity to perform the first update when configuration is done. (See Creating an App Widget
     * Configuration Activity below.)"
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view:
            rv.setEmptyView(R.id.widget_list, R.id.empty_view);

            // Setup intent which points to the TermuxWidgetService which will provide the views for this collection.
            Intent intent = new Intent(context, TermuxWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            rv.setRemoteAdapter(R.id.widget_list, intent);

            // Setup refresh button:
            Intent refreshIntent = new Intent(context, TermuxWidgetProvider.class);
            refreshIntent.setAction(TERMUX_WIDGET_PROVIDER.ACTION_REFRESH_WIDGET);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent toastIntent = new Intent(context, TermuxWidgetProvider.class);
            toastIntent.setAction(TERMUX_WIDGET_PROVIDER.ACTION_WIDGET_ITEM_CLICKED);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_list, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {
            case TERMUX_WIDGET_PROVIDER.ACTION_WIDGET_ITEM_CLICKED:
                String clickedFilePath = intent.getStringExtra(TERMUX_WIDGET_PROVIDER.EXTRA_FILE_CLICKED);
                if (FileUtils.getFileType(clickedFilePath, true) == FileType.DIRECTORY) return;
                sendExecutionIntentToTermuxService(context, clickedFilePath, LOG_TAG);
                break;
            case TERMUX_WIDGET_PROVIDER.ACTION_REFRESH_WIDGET:
                String errmsg = TermuxUtils.isTermuxAppAccessible(context);
                if (errmsg != null) {
                    Logger.logErrorAndShowToast(context, LOG_TAG, errmsg);
                    return;
                }

                int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);

                Toast toast = Toast.makeText(context, R.string.msg_scripts_reloaded, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
        }
    }






    /**
     * Extract termux shortcut file path from an intent and send intent to TermuxService to execute it.
     *
     * @param context The {@link Context} that will be used to send execution intent to the TermuxService.
     * @param intent The {@link Intent} received for the shortcut file.
     */
    public static void handleTermuxShortcutExecutionIntent(Context context, Intent intent, String logTag) {
        if (context == null || intent == null) return;
        logTag = DataUtils.getDefaultIfNull(logTag, LOG_TAG);
        String token = intent.getStringExtra(TermuxConstants.TERMUX_WIDGET.EXTRA_TOKEN_NAME);
        if (token == null || !token.equals(TermuxWidgetAppSharedPreferences.getGeneratedToken(context))) {
            Logger.logWarn(logTag, "Invalid token \"" + token + "\" for intent:\n" + IntentUtils.getIntentString(intent));
            Toast.makeText(context, R.string.msg_bad_token, Toast.LENGTH_LONG).show();
            return;
        }

        sendExecutionIntentToTermuxService(context, intent.getData().getPath(), logTag);
    }

    /**
     * Send execution intent to TermuxService for a shortcut file.
     *
     * @param context The {@link Context} that will be used to send execution intent to the TermuxService.
     * @param shortcutFilePath The path to the shortcut file.
     */
    public static void sendExecutionIntentToTermuxService(final Context context, String shortcutFilePath, String logTag) {
        if (context == null) return;

        logTag = DataUtils.getDefaultIfNull(logTag, LOG_TAG);
        String errmsg;
        Error error;

        ExecutionCommand executionCommand = new ExecutionCommand();
        executionCommand.executable = shortcutFilePath;

        // If Termux app is not installed, enabled or accessible with current context or if
        // TermuxConstants.TERMUX_PREFIX_DIR_PATH does not exist or has required permissions, then
        // just return.
        errmsg = TermuxUtils.isTermuxAppAccessible(context);
        if (errmsg != null) {
            Logger.logErrorAndShowToast(context, logTag, errmsg);
            return;
        }


        // If executable is null or empty, then exit here instead of getting canonical path which would expand to "/"
        if (executionCommand.executable == null || executionCommand.executable.isEmpty()) {
            errmsg  = context.getString(R.string.error_null_or_empty_executable);
            Logger.logErrorAndShowToast(context, logTag, errmsg);
            return;
        }

        // Get canonical path of executable
        executionCommand.executable = FileUtils.getCanonicalPath(executionCommand.executable, null);

        // If executable is not under TermuxConstants#TERMUX_SHORTCUT_SCRIPTS_DIR_PATH
        if (!FileUtils.isPathInDirPath(executionCommand.executable, TermuxConstants.TERMUX_SHORTCUT_SCRIPTS_DIR_PATH, true)) {
            errmsg = context.getString(R.string.error_executable_not_under_shortcuts_directory) +
                    "\n" + context.getString(R.string.msg_executable_absolute_path, executionCommand.executable);
            Logger.logErrorAndShowToast(context, logTag, errmsg);
            return;
        }

        // If executable is not a regular file, or is not readable or executable, then return
        // RESULT_CODE_FAILED to plugin host app
        // Setting of read and execute permissions are only done if executable is under TermuxConstants#TERMUX_SHORTCUT_SCRIPTS_DIR_PATH
        error = FileUtils.validateRegularFileExistenceAndPermissions("executable", executionCommand.executable,
                TermuxConstants.TERMUX_SHORTCUT_SCRIPTS_DIR_PATH,
                FileUtils.APP_EXECUTABLE_FILE_PERMISSIONS,
                true, true,
                false);
        if (error != null) {
            error.appendMessage("\n" + context.getString(R.string.msg_executable_absolute_path, executionCommand.executable));
            executionCommand.setStateFailed(error);
            Logger.logErrorAndShowToast(context, logTag, ResultData.getErrorsListMinimalString(executionCommand.resultData));
            return;
        }


        // If executable is under a directory with the basename matching TermuxConstants#TERMUX_SHORTCUT_TASKS_SCRIPTS_DIR_BASENAME
        File shortcutFile = new File(executionCommand.executable);
        File shortcutParentDirFile = shortcutFile.getParentFile();
        if (shortcutParentDirFile != null && shortcutParentDirFile.getName().equals(TermuxConstants.TERMUX_SHORTCUT_TASKS_SCRIPTS_DIR_BASENAME)) {
            executionCommand.inBackground = true;
            // Show feedback for background task
            Toast toast = Toast.makeText(context, context.getString(R.string.msg_executing_task,
                    ShellUtils.getExecutableBasename(executionCommand.executable)),
                    Toast.LENGTH_SHORT);
            // Put the toast at the top of the screen, to avoid blocking eventual
            // toasts made from the task with termux-toast.
            // See https://github.com/termux/termux-widget/issues/33
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }


        // Create execution intent with the action TERMUX_SERVICE#ACTION_SERVICE_EXECUTE to be sent to the TERMUX_SERVICE
        executionCommand.executableUri = new Uri.Builder().scheme(TERMUX_SERVICE.URI_SCHEME_SERVICE_EXECUTE).path(executionCommand.executable).build();
        Intent executionIntent = new Intent(TERMUX_SERVICE.ACTION_SERVICE_EXECUTE, executionCommand.executableUri);
        executionIntent.setClassName(TermuxConstants.TERMUX_PACKAGE_NAME, TermuxConstants.TERMUX_APP.TERMUX_SERVICE_NAME);
        executionIntent.putExtra(TERMUX_SERVICE.EXTRA_BACKGROUND, executionCommand.inBackground);
        executionIntent.putExtra(TERMUX_SERVICE.EXTRA_PLUGIN_API_HELP, context.getString(R.string.help, TermuxConstants.TERMUX_WIDGET_GITHUB_REPO_URL));

        Logger.logVerboseExtended(logTag, executionCommand.toString());
        Logger.logDebug(logTag, "Sending execution intent to " + executionIntent.getComponent().toString() + " for \"" + executionCommand.executable + "\" with background mode " + executionCommand.inBackground);

        try {
            // Send execution intent to execution service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // https://developer.android.com/about/versions/oreo/background.html
                context.startForegroundService(executionIntent);
            } else {
                context.startService(executionIntent);
            }
        } catch (Exception e) {
            String message = "Failed to send execution intent to " + executionIntent.getComponent().toString();
            Logger.logErrorAndShowToast(context, logTag, message + ": " + e.getMessage());
            Logger.logStackTraceWithMessage(logTag, message, e);
        }
    }

}
