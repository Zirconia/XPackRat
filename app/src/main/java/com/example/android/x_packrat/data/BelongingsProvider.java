package com.example.android.x_packrat.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * The content provider for this app. The middleman between the ContentResolver and the actual
 * database.
 *
 */
public class BelongingsProvider extends ContentProvider {

    // Reference to the manager of the local database
    public static BelongingsDbHelper mDbHelper;

    public static final String LOG_TAG = BelongingsProvider.class.getSimpleName();

    // URI matcher code for the content URI for the belongings table
    private static final int CODE_BELONGINGS = 100;

    // URI matcher code for the content URI for a single belonging in the belongings table
    private static final int CODE_BELONGINGS_WITH_ID = 101;

    private static final int CODE_LOG_USAGE_TABLE = 102;

    // The matcher used to match int codes to URIs
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mDbHelper = new BelongingsDbHelper(getContext());
        return true;
    }

    /**
     * @param uri           The full URI to query
     * @param projection    The desired table columns to query for
     * @param selection     An optional restriction to query for a specific row(s)
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return              The appropriate information from the database table
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor queryResults;

        // Checks if the given URI is registered in the URI matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                // Queries for the entire "belongings" table
                queryResults = database.query(BelongingsContract.BelongingEntry.TABLE_NAME,
                        projection,selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_BELONGINGS_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Queries for a single row of the "belongings" table that has an id from
                // selectionArgs
                queryResults = database.query(BelongingsContract.BelongingEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_LOG_USAGE_TABLE:
                // Queries for an entire log usage table for a single belonging
                String tableNameString = BelongingsContract.UsageLogEntry.UNIQUE_TABLE_NAME;
                queryResults = database.query(tableNameString,
                        projection,selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        queryResults.setNotificationUri(getContext().getContentResolver(), uri);

        return queryResults;
    }

    /**
     * Inserts new data into the provider with the given ContentValues.
     *
     * @param uri           The full URI to use for the insertion
     * @param contentValues The data to insert into the database
     * @return              The content URI for the newly inserted database row
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return insertBelonging(uri, contentValues);
            case CODE_LOG_USAGE_TABLE:
                return insertUsageDate(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Inserts a belonging into the database with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri           The full URI to use for the insertion
     * @param values        The data to insert into the database
     * @return              The content URI for the newly inserted database row
     */
    private Uri insertBelonging(Uri uri, ContentValues values) {

        // Checks that the user has entered a name
        String name = values.getAsString(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Your belonging must have a name");
        }

        // Checks that the user has selected an image
        byte[] belonging_image = values.getAsByteArray(
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE);
        if(belonging_image == null){
            throw new IllegalArgumentException("Your belonging must have an image");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null,values);

        // If the ID is -1, then the insertion failed. Logs an error and returns null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Inserts a belonging into the database with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri           The full URI to use for the insertion
     * @param values        The data to insert into the database
     * @return              The content URI for the newly inserted database row
     */
    private Uri insertUsageDate(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long newRowId = database.insert(BelongingsContract.UsageLogEntry.UNIQUE_TABLE_NAME,
                null,values);

        // If the ID is -1, then the insertion failed. Logs an error and returns null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param contentValues The data to insert into the database
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return              The number of rows that were updated
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return updateBelongings(uri, contentValues, selection, selectionArgs);
            case CODE_BELONGINGS_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBelongings(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Updates belongings in the database with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return              The number of rows that were updated
     */
    private int updateBelongings(Uri uri, ContentValues values, String selection,
                                 String[] selectionArgs) {

        // Checks that the image is not null
        if(values.containsKey(BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE)){
            byte[] belongingImage = values.getAsByteArray
                    (BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE);
            if(belongingImage == null){
                throw new IllegalArgumentException("Your belonging requires an image");
            }
        }

        // Checks that the name value is not null
        if(values.containsKey(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME)){
            String name =
                    values.getAsString(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Your belonging requires a name");
            }
        }

        // Avoids updating the database if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numRowsUpdated = database.update(
                BelongingsContract.BelongingEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs );

        // Notifies registered listeners that the database has changed
        if(numRowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Deletes the data at the given selection and selection arguments.
     * @param uri           The full URI of the row(s) we wish to delete
     * @param selection     A restriction to specify what specific rows to delete
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return              The number of rows that were deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int numRowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                // Delete all rows that match the selection and selection args
                numRowsDeleted = database.delete(BelongingsContract.BelongingEntry.TABLE_NAME,
                        selection, selectionArgs);
                if(numRowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_BELONGINGS_WITH_ID:
                // Delete a single row given by the ID in the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                numRowsDeleted =  database.delete(BelongingsContract.BelongingEntry.TABLE_NAME,
                        selection, selectionArgs);
                if(numRowsDeleted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        return numRowsDeleted;
    }

    /**
     * Returns the MIME type for the type of data handled by the supplied content Uri
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return BelongingsContract.BelongingEntry.CONTENT_LIST_TYPE;
            case CODE_BELONGINGS_WITH_ID:
                return BelongingsContract.BelongingEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Creates the UriMatcher that will match each URI to int ID constants
     *
     * @return A UriMatcher that correctly matches all URIs with integer constants
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BelongingsContract.CONTENT_AUTHORITY;

        // Registers query URI for all belongings to matcher
        matcher.addURI(authority, BelongingsContract.PATH_BELONGINGS, CODE_BELONGINGS);

        // Registers query URI for a single belonging based on ID to matcher
        matcher.addURI(authority, BelongingsContract.PATH_BELONGINGS + "/#",
                CODE_BELONGINGS_WITH_ID);

        matcher.addURI(authority, BelongingsContract.UsageLogEntry.TABLE_NAME + "/*",
                CODE_LOG_USAGE_TABLE);

        return matcher;
    }
}
