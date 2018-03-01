package com.example.android.x_packrat;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.x_packrat.data.BelongingsContract;

/**
 * The activity that launches when a user clicks on a recycler view list item's "logs" button
 * in the MainActivity. Displays a list of all of the dates and times of usage for a single
 * belonging.
 */
public class UsageLogsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, UsageLogAdapter.UsageLogAdapterOnClickHandler {

    private final String TAG = UsageLogsActivity.class.getSimpleName();

    // Used to identify the loader responsible for loading our list of usage logs from the database
    private static final int ID_USAGE_LOGS_LOADER = 80;

    // Stores a reference to the adapter that is attached to the recycler view
    private UsageLogAdapter mUsageLogAdapter;

    // Holds reference to recycler view that displays the list of usage logs
    private RecyclerView mRecyclerView;

    // Stores the recycler views current scroll position
    private int mPosition = RecyclerView.NO_POSITION;

    // Stores a reference for the loading circle(used to indicate that data is being loaded)
    private ProgressBar mLoadingIndicator;

    // Stores reference to message to display when the recycler view is empty
    private TextView mEmptyView;

    // References text view used for displaying the belonging's name
    private TextView mNameHeader;

    // References the image view used to display the belonging's image
    private ImageView mImageHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_logs);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_usage_logs);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mEmptyView = (TextView) findViewById(R.id.empty_view);

        mNameHeader = (TextView) findViewById(R.id.usage_log_belonging_header_text);

        mImageHeader = (ImageView) findViewById(R.id.usage_log_belonging_header_image);

        Intent intent = getIntent();

        mNameHeader.setText(intent.getStringExtra("belonging_name"));

        byte[] imgByte = intent.getByteArrayExtra("belonging_image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        mImageHeader.setImageBitmap(bitmap);

        // Positions and measures item views within a RecyclerView to form a linear list
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // Associates our above layoutManager with our recycler view
        mRecyclerView.setLayoutManager(layoutManager);

        // Indicates that changes in the content of each view will not change the layout size
        mRecyclerView.setHasFixedSize(true);

        // Links our belongings data with our views that will display it
        mUsageLogAdapter = new UsageLogAdapter(this, this);

        // Attaches our adapter(link to our data source) to our recycler view to allow for items to
        // be displayed
        mRecyclerView.setAdapter(mUsageLogAdapter);

        // Adds divider line between recycler view items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        showLoading();

        // Changes the title in the action bar to reflect that the user is viewing usage logs
        setTitle(getString(R.string.usage_log_activity_title_text));

        // Enables up navigation arrow in tool bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initializes and starts a new loader if a loader with the given ID does not exist
        getSupportLoaderManager().initLoader(ID_USAGE_LOGS_LOADER, null, this);
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {

            case ID_USAGE_LOGS_LOADER:
                /*
                 * The columns of data that we are interested in within our
                 * UsageLogActivity's list of usage logs
                 */
                String[] projection = {
                        BelongingsContract.UsageLogEntry._ID,
                        BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID,
                        BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE,
                        BelongingsContract.UsageLogEntry.COLUMN_USAGE_DESCRIPTION
                };

                // Sort belongings in descending order by usage date
                String sortOrder = BelongingsContract.UsageLogEntry.
                        COLUMN_USAGE_DATE + " DESC";

                // The rows of the database table that we are interested in
                String selection = BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID + "=?";
                String[] selectionArgs = new String[]{
                        String.valueOf(getIntent().getLongExtra("belonging_id", 0))};

                return new CursorLoader(this,
                        BelongingsContract.UsageLogEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
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

        mUsageLogAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

        // Checks if there is data to show. If not, displays message, else displays the data
        if (data.getCount() != 0) {
            showUsageLogDataView();
        } else {
            showEmptyView();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * Removes any references the program has to the loader's data.
     *
     * @param loader The loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clears the adapter displaying the usage log data since the loader's data is now invalid
        mUsageLogAdapter.swapCursor(null);
    }

    /**
     * Used for responding to clicks from our list
     *
     * @param clickedItemId The database id for the clicked item
     */
    @Override
    public void onClick(View v, long clickedItemId) {
        Uri clickedBelongingUri = ContentUris.withAppendedId(BelongingsContract.UsageLogEntry.
                CONTENT_URI, clickedItemId);

        Intent editUsageLogIntent = new Intent(this, UsageLogEditorActivity.class);
        editUsageLogIntent.setData(clickedBelongingUri);

        //Intent intent = getIntent();
        //editUsageLogIntent.putExtra("belonging_name",intent.getStringExtra("belonging_name"));
        //editUsageLogIntent.putExtra("belonging_image",intent.getByteArrayExtra("belonging_image"));

        startActivity(editUsageLogIntent);
    }

    /**
     * Makes the view for the usage log data visible and hides the loading indicator.
     */
    private void showUsageLogDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    /**
     * Makes the loading indicator visible and hides the usage log View
     */
    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the loading indicator and hides the usage log recycler view. Makes
     * the view for the empty recycler view state visible.
     */
    private void showEmptyView() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    /**
     * Inflates and sets up the menu for this Activity
     *
     * @param menu The options menu in which items are placed
     * @return True for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.usage_logs, menu);
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu
     *
     * @param item The menu item that was selected by the user
     * @return True to indicate that menu click is handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_add_usage_log:
                Intent intent = new Intent(this, UsageLogEditorActivity.class);
                intent.putExtra("belonging_id", getIntent().getLongExtra("belonging_id",0));
                startActivity(intent);
                return true;
            case android.R.id.home:
                // If the belonging hasn't changed, continue with navigating up to MainActivity
                NavUtils.navigateUpFromSameTask(UsageLogsActivity.this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
