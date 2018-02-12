package com.example.android.x_packrat;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.utilities.XPackRatDateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * An adapter class that works in conjunction with the recycler view to display the list of the
 * user's usage dates and times for a belonging. Its data source is a Cursor of usage log data from
 * the local "possessions.db" database.
 */
public class UsageLogAdapter extends RecyclerView.Adapter<UsageLogAdapter.
        UsageLogAdapterViewHolder> {
    // Indicates that we want to use the layout "R.layout.usage_log_item" to display each
    // item in the recycler view
    private static final int VIEW_TYPE_LOGS = 5;

    // The context we use for utility methods, app resources and layout inflaters
    private final Context mContext;

    // Click handler for items clicked within the adapter
    final private UsageLogAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface UsageLogAdapterOnClickHandler {
        void onClick(View v, long clickedItemId);
    }

    // Holds the data that is fetched from the database
    private Cursor mCursor;

    /**
     * Creates a BelongingsAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public UsageLogAdapter(@NonNull Context context, UsageLogAdapter.UsageLogAdapterOnClickHandler
            clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within
     * @param viewType  Used to determine what layout to use for displaying recycler view items
     * @return A new BelongingsAdapterViewHolder that holds a View for a list item
     */
    @Override
    public UsageLogAdapter.UsageLogAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                                        int viewType) {

        int layoutId;

        // Decides which layout file to inflate for displaying a list item
        switch (viewType) {
            case VIEW_TYPE_LOGS: {
                layoutId = R.layout.usage_log_item;
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new UsageLogAdapter.UsageLogAdapterViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display the data at the specified
     * position. Updates the contents of the ViewHolder to display the belonging
     * details for this particular position, using the "position" argument.
     *
     * @param usageLogAdapterViewHolder The ViewHolder that is updated to represent the
     *                                  contents of the item at the given position in the data set
     * @param position                  The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(UsageLogAdapter.UsageLogAdapterViewHolder usageLogAdapterViewHolder,
                                 int position) {
        mCursor.moveToPosition(position);

        // Reads the date from the cursor
        long dateInMillis = mCursor.getLong(mCursor.getColumnIndexOrThrow(
                BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);

        // Parses out information contained in the datetime that was fetched from the database
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int hour = hourOfDay % 12;
        int minute = calendar.get(Calendar.MINUTE);

        // Gets the user's current locale. Uses deprecated code if device is using API < 24.
        Locale locale = XPackRatDateUtils.getUserLocale(mContext);

        // Sets the date that the belonging was last used to be displayed in a text view
        usageLogAdapterViewHolder.usageDateView.setText(XPackRatDateUtils.formatDate(
                locale, year, month, day));

        // Sets the time that the belonging was last used to be displayed in a text view
        usageLogAdapterViewHolder.usageTimeView.setText(XPackRatDateUtils.formatTime(
                locale, minute, hourOfDay));
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of available usage logs
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (determines what layout to use when displaying an item)
     */
    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_LOGS;
    }

    /**
     * Called by MainActivity after a load has finished, as well as when the Loader responsible for
     * loading the belongings data is reset. When this method is called, we assume we have a
     * completely new set of data, so we call notifyDataSetChanged to tell the RecyclerView to
     * update.
     *
     * @param newCursor The new cursor to use as BelongingsAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * Caches the child views for an item.
     */
    class UsageLogAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        // All child views of a single item
        final TextView usageDateView;
        final TextView usageTimeView;

        /**
         * @param view The view to cache for later reuse
         */
        UsageLogAdapterViewHolder(View view) {
            super(view);

            usageDateView = (TextView) view.findViewById(R.id.date_used);
            usageTimeView = (TextView) view.findViewById(R.id.time_used);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the id of the item that has
         * been selected, and then call the onClick handler registered with this adapter,
         * passing that id and the clicked view.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long clickedItemId = mCursor.getLong(mCursor.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry._ID));
            mClickHandler.onClick(v, clickedItemId);
        }
    }
}
