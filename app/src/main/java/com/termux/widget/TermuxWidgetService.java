package com.termux.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TermuxWidgetService extends RemoteViewsService {

    @SuppressLint("SdCardPath")
    public static final File SHORTCUTS_DIR = new File("/data/data/com.termux/files/home/.shortcuts");

    public static final class TermuxWidgetItem {

        /** Label to display in the list. */
        public final String mLabel;
        /** The file which this item represents, sent with the {@link TermuxWidgetProvider#EXTRA_CLICKED_FILE} extra. */
        public final String mFile;

        public TermuxWidgetItem(File file, int depth) {
            this.mLabel = (depth > 0 ? (file.getParentFile().getName() + "/") : "")
                    + file.getName().replace('-', ' ');
            this.mFile = file.getAbsolutePath();
        }

    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(getApplicationContext());
    }

    public static class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private final List<TermuxWidgetItem> mWidgetItems = new ArrayList<>();
        private final Context mContext;

        public ListRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
            // for example downloading or creating content etc, should be deferred to onDataSetChanged()
            // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        }

        @Override
        public void onDestroy() {
            mWidgetItems.clear();
        }

        @Override
        public int getCount() {
            return mWidgetItems.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // Position will always range from 0 to getCount() - 1.
            TermuxWidgetItem widgetItem = mWidgetItems.get(position);

            // Construct remote views item based on the item xml file and set text based on position.
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            rv.setTextViewText(R.id.widget_item, widgetItem.mLabel);

            // Next, we set a fill-intent which will be used to fill-in the pending intent template
            // which is set on the collection view in TermuxAppWidgetProvider.
            Intent fillInIntent = new Intent().putExtra(TermuxWidgetProvider.EXTRA_CLICKED_FILE, widgetItem.mFile);
            rv.setOnClickFillInIntent(R.id.widget_item_layout, fillInIntent);

            // You can do heaving lifting in here, synchronously. For example, if you need to
            // process an image, fetch something from the network, etc., it is ok to do it here,
            // synchronously. A loading view will show up in lieu of the actual contents in the
            // interim.

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            // You can create a custom loading view (for instance when getViewAt() is slow.) If you
            // return null here, you will get the default loading view.
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @SuppressLint("SdCardPath")
        @Override
        public void onDataSetChanged() {
            // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
            // on the collection view corresponding to this factory. You can do heaving lifting in
            // here, synchronously. For example, if you need to process an image, fetch something
            // from the network, etc., it is ok to do it here, synchronously. The widget will remain
            // in its current state while work is being done here, so you don't need to worry about
            // locking up the widget.
            mWidgetItems.clear();
            // Create directory if necessary so user more easily finds where to put shortcuts:
            SHORTCUTS_DIR.mkdirs();

            addFile(SHORTCUTS_DIR, mWidgetItems, 0);
        }
    }

    private static void addFile(File dir, List<TermuxWidgetItem> widgetItems, int depth) {
        if (depth > 5) return;

        File[] files = dir.listFiles(pathname -> !pathname.getName().startsWith("."));

        if (files == null) return;
        Arrays.sort(files, (lhs, rhs) -> {
            if (lhs.isDirectory() != rhs.isDirectory()) {
                return lhs.isDirectory() ? 1 : -1;
            }
            return NaturalOrderComparator.compare(lhs.getName(), rhs.getName());
        });

        for (File file : files) {
            if (file.isDirectory()) {
                addFile(file, widgetItems, depth + 1);
            } else {
                widgetItems.add(new TermuxWidgetItem(file, depth));
            }
        }

    }
}
