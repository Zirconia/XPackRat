package com.example.android.x_packrat;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.x_packrat.data.BelongingsContract;

/**
 * The fragment on which the recycler view list of the user's sold belongings is displayed
 */
public class SoldFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
        , SoldAdapter.SoldAdapterOnClickHandler, SearchView.OnQueryTextListener {

    // The activity that this fragment is attached to
    MainActivity mActivity;

    // Stores any text that the user enters into the search view in the action bar
    public static String sSearchConstraint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
    }

    /*
     * The columns of data that we are interested in within our MainActivity's list of
     * belongings.
     */
    public static final String[] MAIN_BELONGINGS_PROJECTION = {
            BelongingsContract.BelongingEntry._ID,
            BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE,
            BelongingsContract.SoldEntry.COLUMN_SOLD_TO
    };

    // Used to identify the loader responsible for loading our list of belongings from the database
    private static final int ID_SOLD_BELONGINGS_LOADER = 201;

    // Stores a reference to the adapter that is attached to the recycler view
    private SoldAdapter mSoldAdapter;

    // Holds reference to recycler view that displays the list of belongings
    private RecyclerView mRecyclerView;

    // Stores the recycler views current scroll position
    private int mPosition = RecyclerView.NO_POSITION;

    // Stores a reference for the loading circle(used to indicate that data is being loaded)
    private ProgressBar mLoadingIndicator;

    // Stores reference to message to display when the recycler view is empty
    private TextView mEmptyView;

    public SoldFragment(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sold_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_sold_belongings);

        mLoadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);

        mEmptyView = (TextView) view.findViewById(R.id.empty_view);

        // Positions and measures item views within a RecyclerView to form a linear list
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);

        // Associates our above layoutManager with our recycler view
        mRecyclerView.setLayoutManager(layoutManager);

        // Indicates that changes in the content of each view will not change the layout size
        mRecyclerView.setHasFixedSize(true);

        // Links our belongings data with our views that will display it
        mSoldAdapter = new SoldAdapter(mActivity, this);

        // Attaches our adapter(link to our data source) to our recycler view to allow for items to
        // be displayed
        mRecyclerView.setAdapter(mSoldAdapter);

        // Adds divider line between recycler view items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        showLoading();

        // Initializes and starts a new loader if a loader with the given ID does not exist
        getLoaderManager().initLoader(ID_SOLD_BELONGINGS_LOADER, null, this);
    }

    /**
     * Called when a new Loader needs to be created
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new loader instance
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {

            case ID_SOLD_BELONGINGS_LOADER:

                Uri belongingsQueryUri;

                // Sets the query uri to be the one for retrieving all rows of the "sold"
                // table if the user has not entered any text in the action bar search view
                if (!TextUtils.isEmpty(sSearchConstraint)) {
                    belongingsQueryUri = BelongingsContract.SoldEntry.CONTENT_URI.buildUpon().
                            appendPath(sSearchConstraint).build();
                } else {
                    belongingsQueryUri = BelongingsContract.SoldEntry.CONTENT_URI;
                }

                // Sort belongings in descending order by name
                String sortOrder = BelongingsContract.SoldEntry.
                        COLUMN_BELONGING_NAME + " DESC";

                return new CursorLoader(mActivity,
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
        mSoldAdapter.swapCursor(data);

        // Scrolls recycler view to position of the first item in the list
        // if there is no position set
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

        // Checks if there is data to show. If not, displays message, else displays the data
        if (data.getCount() != 0) {
            showBelongingsDataView();
        } else if (TextUtils.isEmpty(sSearchConstraint)) {
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

        // Clears the adapter displaying the belongings data since the loader's data is now invalid
        mSoldAdapter.swapCursor(null);
    }

    /**
     * Used for responding to clicks from our list
     *
     * @param v             The clicked item
     * @param clickedItemId The database id for the clicked item
     * @param belongingName The name of the clicked item
     */
    @Override
    public void onClick(View v, long clickedItemId, String belongingName, byte[] belongingImage) {
        Uri clickedBelongingUri;

        clickedBelongingUri = ContentUris.withAppendedId(BelongingsContract.SoldEntry.
                CONTENT_URI, clickedItemId);
        Intent editBelongingIntent = new Intent(mActivity, EditSoldActivity.class);
        editBelongingIntent.setData(clickedBelongingUri);

        startActivity(editBelongingIntent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sold, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_add_sold_belonging:
                startActivity(new Intent(mActivity, EditSoldActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(mActivity, SettingsActivity.class));
                return true;
            case R.id.action_charts:
                startActivity(new Intent(mActivity, ChartsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes the view for the belongings data visible and hides the
     * loading indicator.
     */
    private void showBelongingsDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    /**
     * Makes the loading indicator visible and hides the belongings View
     */
    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the loading indicator and hides the belongings recycler view. Makes
     * the view for the empty recycler view state visible.
     */
    private void showEmptyView() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        sSearchConstraint = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(ID_SOLD_BELONGINGS_LOADER, null, this);
        return true;
    }
}
