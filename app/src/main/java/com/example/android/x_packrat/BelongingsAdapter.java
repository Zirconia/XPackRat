package com.example.android.x_packrat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.utilities.XPackRatDateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * An adapter class that works in conjunction with the recycler view to display the list of the
 * user's belongings. Its data source is a Cursor of belongings data from a local database.
 */
public class BelongingsAdapter extends RecyclerView.Adapter<BelongingsAdapter.
        BelongingsAdapterViewHolder>{

    private static final int VIEW_TYPE_ALL_BELONGINGS = 0;

    // The context we use for utility methods, app resources and layout inflaters
    private final Context mContext;

    // Click handler for items clicked within the adapter
    final private BelongingsAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface BelongingsAdapterOnClickHandler {
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
    public BelongingsAdapter(@NonNull Context context, BelongingsAdapterOnClickHandler clickHandler)
    {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  Used to determine what layout to use for displaying recycler view items.
     *
     * @return A new BelongingsAdapterViewHolder that holds the View for each list item
     */
    @Override
    public BelongingsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId;

        switch (viewType) {
            case VIEW_TYPE_ALL_BELONGINGS: {
                layoutId = R.layout.belongings_list_item;
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new BelongingsAdapterViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display the data at the specified
     * position. Updates the contents of the ViewHolder to display the belonging
     * details for this particular position, using the "position" argument.
     *
     * @param BelongingsAdapterViewHolder The ViewHolder that is updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(BelongingsAdapterViewHolder BelongingsAdapterViewHolder,
                                 int position) {
        mCursor.moveToPosition(position);

        // Reads the belonging's image from the cursor as a Blob(array of bytes in this case)
        byte[] belongingImageBytes = mCursor.getBlob(mCursor.getColumnIndexOrThrow(
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE));

        // Decodes the array of bytes into a Bitmap image
        Bitmap belongingBitmap = BitmapFactory.decodeByteArray(belongingImageBytes, 0,
                belongingImageBytes.length);

        // Sets the belonging's image to be displayed in an image view for this view holder
        BelongingsAdapterViewHolder.belongingImageView.setImageBitmap(belongingBitmap);

        // Reads the name of the belonging from the cursor
        String belongingName = mCursor.getString(mCursor.getColumnIndexOrThrow(
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME));

        // Sets the name of the belonging to be displayed in a text view for this view holder
        BelongingsAdapterViewHolder.nameView.setText(belongingName);


        // Reads the date from the cursor
        long dateInMillis = mCursor.getLong(mCursor.getColumnIndexOrThrow(
                BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE));

        // Gets human readable string using utility method
        String dateString = XPackRatDateUtils.getFriendlyDateString(mContext, dateInMillis,
                true);

        // Sets the date that the belonging was last used to be displayed in a text view
        BelongingsAdapterViewHolder.lastUsedDateView.setText(dateString);

        // Sets the time that the belonging was last used to be displayed in a text view
        BelongingsAdapterViewHolder.lastUsedTimeView.setText(dateString);
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of available belongings
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
     * @return the view type (determines what layout to use when displaying items)
     */
    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ALL_BELONGINGS;
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
    class BelongingsAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        final ImageView belongingImageView;

        final TextView nameView;
        final TextView lastUsedDateView;
        final TextView lastUsedTimeView;

        final Button logUsageButton;

        /**
         * @param view The view to cache for later reuse
         */
        BelongingsAdapterViewHolder(View view) {
            super(view);

            belongingImageView = (ImageView) view.findViewById(R.id.belonging_image);
            nameView = (TextView) view.findViewById(R.id.belonging_name);
            lastUsedDateView = (TextView) view.findViewById(R.id.last_used_date);
            lastUsedTimeView = (TextView) view.findViewById(R.id.last_used_time);
            logUsageButton = (Button) view.findViewById(R.id.main_log_usage_button);
            logUsageButton.setOnClickListener(this);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the id of the item that has
         * been selected, and then call the onClick handler registered with this adapter,
         * passing that id.
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
