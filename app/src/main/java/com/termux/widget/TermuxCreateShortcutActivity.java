package com.termux.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class TermuxCreateShortcutActivity extends Activity {

    private ListView mListView;
    private File mCurrentDirectory;
    private File[] mCurrentFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shortcuts_listview);
        mListView = (ListView) findViewById(R.id.list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateListview(TermuxWidgetService.SHORTCUTS_DIR);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final Context context = TermuxCreateShortcutActivity.this;
                File clickedFile = mCurrentFiles[position];
                if (clickedFile.isDirectory()) {
                    updateListview(clickedFile);
                    return;
                }

                Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);

                Uri scriptUri = new Uri.Builder().scheme("com.termux.file").path(clickedFile.getAbsolutePath()).build();
                Intent executeIntent = new Intent(context, TermuxLaunchShortcutActivity.class);
                executeIntent.setData(scriptUri);
                executeIntent.putExtra(TermuxLaunchShortcutActivity.TOKEN_NAME, TermuxLaunchShortcutActivity.getGeneratedToken(context));

                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, executeIntent);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, clickedFile.getName());
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void updateListview(File directory) {
        mCurrentDirectory = directory;
        mCurrentFiles = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().startsWith(".");
            }
        });
        if (mCurrentFiles == null) mCurrentFiles = new File[0];

        Arrays.sort(mCurrentFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        final boolean isTopDir = directory.equals(TermuxWidgetService.SHORTCUTS_DIR);
        getActionBar().setDisplayHomeAsUpEnabled(!isTopDir);

        if (isTopDir && mCurrentFiles.length == 0) {
            // Create if necessary so user can more easily add.
            TermuxWidgetService.SHORTCUTS_DIR.mkdirs();
            new AlertDialog.Builder(this).setMessage(R.string.no_shortcut_scripts).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            }).show();
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
            updateListview(mCurrentDirectory.getParentFile());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
