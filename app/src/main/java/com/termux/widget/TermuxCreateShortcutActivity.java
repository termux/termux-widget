package com.termux.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;

public class TermuxCreateShortcutActivity extends Activity {

	ListView mListView ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shortcuts_listview);
		mListView = (ListView) findViewById(R.id.list);
	}

	@Override
	protected void onResume() {
		super.onResume();

		final File[] files = TermuxWidgetService.SHORTCUTS_DIR.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().startsWith(".");
			}
		});

		if (files == null || files.length == 0) {
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

		final String[] values = new String[files.length];
		for (int i = 0; i < values.length; i++) values[i] = files[i].getName();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
		mListView.setAdapter(adapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				final Context context = TermuxCreateShortcutActivity.this;
				File clickedFile = files[position];

				Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);

				Uri scriptUri = new Uri.Builder().scheme("file").path(clickedFile.getAbsolutePath()).build();
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

}
