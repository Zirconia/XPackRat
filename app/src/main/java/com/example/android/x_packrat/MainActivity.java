package com.example.android.x_packrat;

import android.content.ContentUris;
import android.content.ContentValues;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.example.android.x_packrat.data.BelongingsContract;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
        ,BelongingsAdapter.BelongingsAdapterOnClickHandler {

    private final String TAG = MainActivity.class.getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * belongings.
     */
    public static final String[] MAIN_BELONGINGS_PROJECTION = {
            BelongingsContract.BelongingEntry._ID,
            BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
            BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE,
    };

    // Used to identify the loader responsible for loading our list of belongings from the database
    private static final int ID_BELONGINGS_LOADER = 77;

    // Used to identify the loader responsible for updating a belongings last usage date and time
    private static final int ID_LOG_BELONGING_USAGE_LOADER = 78;

    // Stores a reference to the adapter that is attached to the recycler view
    private BelongingsAdapter mBelongingsAdapter;

    // Holds reference to recycler view that displays the list of belongings
    private RecyclerView mRecyclerView;

    // Stores the recycler views current scroll position
    private int mPosition = RecyclerView.NO_POSITION;

    // Will be used to store a reference to the loading image
    private ProgressBar mLoadingIndicator;

    // The current date when the user has pressed the log usage button
    private Date currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_main_belongings);

        //The ProgressBar that will indicate to the user that we are loading data. It will be hidden
        //when no data is loading.
        //mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Positions and measures item views within a RecyclerView to form a linear list
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // Associates our above layoutManager with our recycler view
        mRecyclerView.setLayoutManager(layoutManager);

        // Indicates that changes in the content of each view will not change the layout size
        mRecyclerView.setHasFixedSize(true);

        // Links our belongings data with our views that will display it
        mBelongingsAdapter = new BelongingsAdapter(this, this);

        // Attaches our adapter(link to our data source) to our recycler view to allow for items to
        // be displayed
        mRecyclerView.setAdapter(mBelongingsAdapter);



        showLoading();

        // Initializes and starts a new loader if a loader with the given ID does not exist
        getSupportLoaderManager().initLoader(ID_BELONGINGS_LOADER, null, this);
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return         A new loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {

            case ID_BELONGINGS_LOADER:
                // URI for all rows of belongings in "belongings" table
                Uri belongingsQueryUri = BelongingsContract.BelongingEntry.CONTENT_URI;

                // Sort belongings in descending order by last used date
                String sortOrder = BelongingsContract.BelongingEntry.
                        COLUMN_LAST_USED_DATE + " DESC";

                return new CursorLoader(this,
                        belongingsQueryUri,
                        MAIN_BELONGINGS_PROJECTION,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a loader has finished loading its data.
     *
     * @param loader The loader that has finished
     * @param data   The data generated by the loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mBelongingsAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showBelongingsDataView();
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * Removes any references the program has to the loader's data.
     *
     * @param loader The loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clears the adapter displaying the belongings data since the loader's data is now invalid
        mBelongingsAdapter.swapCursor(null);
    }

    /**
     * Used for responding to clicks from our list
     *
     * @param clickedItemId The database id for the clicked item
     */
    @Override
    public void onClick(View v, long clickedItemId) {
        Uri clickedBelongingUri = ContentUris.withAppendedId(BelongingsContract.BelongingEntry.
                CONTENT_URI, clickedItemId);

        if(v.getId() == R.id.main_log_usage_button) {
            logBelongingUsage(clickedBelongingUri);
        }
        else {
            Intent editBelongingIntent = new Intent(
                    MainActivity.this, EditorActivity.class);

            // Appends the clicked item's id to the content Uri
            editBelongingIntent.setData(clickedBelongingUri);
            startActivity(editBelongingIntent);
        }
    }

    /**
     * Makes the view for the belongings data visible and hides the error message and
     * loading indicator.
     */
    private void showBelongingsDataView() {

        //mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Makes the loading indicator visible and hides the belongings View and error
     * message.
     */
    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        //mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Inflates and sets up the menu for this Activity
     *
     * @param menu The options menu in which items are placed
     *
     * @return     True for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.belongings, menu);
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu
     *
     * @param item The menu item that was selected by the user
     *
     * @return     True to indicate that menu click is handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_add_belonging:
                startActivity(new Intent(this, EditorActivity.class));
                return true;
        }

        /*if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void logBelongingUsage(Uri clickedBelongingUri){
        currentTime = Calendar.getInstance().getTime();
        long dateTime = currentTime.getTime();

        // The values to insert into the database
        ContentValues values = new ContentValues();
        values.put(BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE,dateTime );

        int numRowsUpdated = getContentResolver().update(clickedBelongingUri, values, null,
                null);
    }
}
