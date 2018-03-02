package com.example.android.x_packrat.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * The content provider for this app. The middleman between the ContentResolver and the actual
 * database.
 */
public class BelongingsProvider extends ContentProvider {

    public static final String LOG_TAG = BelongingsProvider.class.getSimpleName();

    // Reference to the class that manages the creation of the database and its tables
    private static BelongingsDbHelper mDbHelper;

    /*
     * Uri matcher codes
     */
    // For the content URI for the "belongings" table
    private static final int CODE_BELONGINGS = 800;

    // For the content URI for a single belonging in the "belongings" table
    private static final int CODE_BELONGINGS_WITH_ID = 801;

    private static final int CODE_BELONGINGS_WITH_SEARCH_FILTER = 810;

    // For the content URI for the usage_log table
    private static final int CODE_LOG_USAGE_TABLE = 802;

    // For the content URI for a single log in the "usage_log" table
    private static final int CODE_LOG_USAGE_TABLE_WITH_ID = 803;

    // For the content URI for the usage_log table
    private static final int CODE_SOLD = 804;

    // For the content URI for a single log in the "usage_log" table
    private static final int CODE_SOLD_WITH_ID = 805;

    private static final int CODE_SOLD_WITH_SEARCH_FILTER = 811;

    // For the content URI for the usage_log table
    private static final int CODE_DISCARDED = 806;

    // For the content URI for a single log in the "usage_log" table
    private static final int CODE_DISCARDED_WITH_ID = 807;

    private static final int CODE_DISCARDED_WITH_SEARCH_FILTER = 812;

    // For the content URI for the usage_log table
    private static final int CODE_DONATED = 808;

    // For a single log in the "usage_log" table
    private static final int CODE_DONATED_WITH_ID = 809;

    private static final int CODE_DONATED_WITH_SEARCH_FILTER = 813;

    // The matcher used to match int codes to URIs
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mDbHelper = new BelongingsDbHelper(getContext());
        return true;
    }

    /**
     * Queries the appropriate "possessions.db" table based on the params.
     *
     * @param uri           The full URI to query
     * @param projection    The desired table columns to query for
     * @param selection     An optional restriction to query for a specific row(s)
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @param sortOrder     Indicates how to order the data in the returned cursor
     * @return The appropriate information from the database table
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                                                        String[] selectionArgs, String sortOrder) {

        // Reference to a read-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor queryResults;

        // Checks if the given URI is registered in the URI matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                // Queries for the entire "belongings" table
                queryResults = database.query(BelongingsContract.BelongingEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_BELONGINGS_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Queries for a single row of the "belongings" table that has an id from
                // selectionArgs
                queryResults = database.query(BelongingsContract.BelongingEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_BELONGINGS_WITH_SEARCH_FILTER:
                selection = BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + uri.getLastPathSegment() + "%"};

                queryResults = database.query(BelongingsContract.BelongingEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_LOG_USAGE_TABLE:
                // Queries for "usage_log" table rows that are associated with the belonging whose
                // "Usage Logs" button the user has clicked
                queryResults = database.query(BelongingsContract.UsageLogEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_LOG_USAGE_TABLE_WITH_ID:
                selection = BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Queries for "usage_log" table rows that are associated with the current belonging
                queryResults = database.query(BelongingsContract.UsageLogEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_SOLD:
                // Queries for the entire "sold" table
                queryResults = database.query(BelongingsContract.SoldEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_SOLD_WITH_ID:
                selection = BelongingsContract.SoldEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Queries for single "sold" table row
                queryResults = database.query(BelongingsContract.SoldEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_SOLD_WITH_SEARCH_FILTER:
                selection = BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + uri.getLastPathSegment() + "%"};

                queryResults = database.query(BelongingsContract.SoldEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_DISCARDED:
                // Queries for the entire "discarded" table
                queryResults = database.query(BelongingsContract.DiscardedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_DISCARDED_WITH_ID:
                selection = BelongingsContract.DiscardedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Queries for single "discarded" table row
                queryResults = database.query(BelongingsContract.DiscardedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_DISCARDED_WITH_SEARCH_FILTER:
                selection = BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + uri.getLastPathSegment() + "%"};

                queryResults = database.query(BelongingsContract.DiscardedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_DONATED:
                // Queries for the entire "donated" table
                queryResults = database.query(BelongingsContract.DonatedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_DONATED_WITH_ID:
                selection = BelongingsContract.DonatedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Queries for single "donated" table row
                queryResults = database.query(BelongingsContract.DonatedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
            case CODE_DONATED_WITH_SEARCH_FILTER:
                selection = BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + uri.getLastPathSegment() + "%"};

                queryResults = database.query(BelongingsContract.DonatedEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Registers the uri param to be watched for changes that occur at it
        Context context = getContext();
        if(context != null)
            queryResults.setNotificationUri(context.getContentResolver(), uri);

        return queryResults;
    }

    /**
     * Calls appropriate method to insert new data into the provider with the given content values.
     *
     * @param uri           The full URI to use for the insertion
     * @param contentValues The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return insertBelonging(uri, contentValues);
            case CODE_LOG_USAGE_TABLE:
                return insertUsageDate(uri, contentValues);
            case CODE_SOLD:
                return insertSoldBelonging(uri, contentValues);
            case CODE_DISCARDED:
                return insertDiscardedBelonging(uri, contentValues);
            case CODE_DONATED:
                return insertDonatedBelonging(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Inserts a row into the "sold" table with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri    The full URI to use for the insertion
     * @param values The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    private Uri insertSoldBelonging(Uri uri, ContentValues values) {

        // Checks that the user has entered a name for the belonging
        String name = values.getAsString(BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Your belonging must have a name");
        }

        // Checks that the user has selected an image for the belonging
        byte[] belonging_image = values.getAsByteArray(
                BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE);
        if (belonging_image == null) {
            throw new IllegalArgumentException("Your belonging must have an image");
        }

        // Reference to write-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The ID for the newly inserted row
        long newRowId = database.insert(BelongingsContract.SoldEntry.TABLE_NAME,
                null, values);

        // Checks if the insertion failed by checking if the new row's ID is -1
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informs content resolver that there was a change at the uri param
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Inserts a row into the "discarded" table with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri    The full URI to use for the insertion
     * @param values The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    private Uri insertDiscardedBelonging(Uri uri, ContentValues values) {

        // Checks that the user has entered a name for the discarded belonging
        String name = values.getAsString(BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Your belonging must have a name");
        }

        // Checks that the user has selected an image for the discarded belonging
        byte[] belonging_image = values.getAsByteArray(
                BelongingsContract.DiscardedEntry.COLUMN_BELONGING_IMAGE);
        if (belonging_image == null) {
            throw new IllegalArgumentException("Your belonging must have an image");
        }

        // Reference to write-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The ID for the newly inserted row
        long newRowId = database.insert(BelongingsContract.DiscardedEntry.TABLE_NAME,
                null, values);

        // Checks if the insertion failed by checking if the new row's ID is -1
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informs content resolver that there was a change at the uri param
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Inserts a row into the "donated" table with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri    The full URI to use for the insertion
     * @param values The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    private Uri insertDonatedBelonging(Uri uri, ContentValues values) {

        // Checks that the user has entered a name for the donated belonging
        String name = values.getAsString(BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Your belonging must have a name");
        }

        // Checks that the user has selected an image for the donated belonging
        byte[] belonging_image = values.getAsByteArray(
                BelongingsContract.DonatedEntry.COLUMN_BELONGING_IMAGE);
        if (belonging_image == null) {
            throw new IllegalArgumentException("Your belonging must have an image");
        }

        // Reference to write-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The ID for the newly inserted row
        long newRowId = database.insert(BelongingsContract.DonatedEntry.TABLE_NAME,
                null, values);

        // Checks if the insertion failed by checking if the new row's ID is -1
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informs content resolver that there was a change at the uri param
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Inserts a row into the "belongings" table with the given content values.
     * Does validation checking to ensure user cannot insert invalid data.
     *
     * @param uri    The full URI to use for the insertion
     * @param values The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    private Uri insertBelonging(Uri uri, ContentValues values) {

        // Checks that the user has entered a name for the belonging
        String name = values.getAsString(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Your belonging must have a name");
        }

        // Checks that the user has selected an image for the belonging
        byte[] belonging_image = values.getAsByteArray(
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE);
        if (belonging_image == null) {
            throw new IllegalArgumentException("Your belonging must have an image");
        }

        // Reference to write-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The ID for the newly inserted row
        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, values);

        // Checks if the insertion failed by checking if the new row's ID is -1
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informs content resolver that there was a change at the uri param
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Inserts a row into the "usage_log" table with the given content values.
     *
     * @param uri    The full URI to use for the insertion
     * @param values The data to insert into the database
     * @return The content URI for the newly inserted database row
     */
    private Uri insertUsageDate(Uri uri, ContentValues values) {

        // Reference to write-only "possessions.db"
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The ID for the newly inserted row
        long newRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, values);

        // Checks if the insertion failed by checking if the new row's ID is -1
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informs content resolver that there was a change at the uri param
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    /**
     * Calls the appropriate method to update the data at the given uri, selection and selection
     * arguments, with the new content values.
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param contentValues The data to insert into the database
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                                                                     String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return updateBelongings(uri, contentValues, selection, selectionArgs);
            case CODE_BELONGINGS_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateBelongings(uri, contentValues, selection, selectionArgs);
            case CODE_LOG_USAGE_TABLE_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.UsageLogEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateUsageLogs(uri, contentValues, selection, selectionArgs);
            case CODE_SOLD:
                return updateSoldBelonging(uri, contentValues, selection, selectionArgs);
            case CODE_SOLD_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.SoldEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateSoldBelonging(uri, contentValues, selection, selectionArgs);
            case CODE_DISCARDED:
                return updateDiscardedBelonging(uri, contentValues, selection, selectionArgs);
            case CODE_DISCARDED_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.DiscardedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateDiscardedBelonging(uri, contentValues, selection, selectionArgs);
            case CODE_DONATED:
                return updateDonatedBelonging(uri, contentValues, selection, selectionArgs);
            case CODE_DONATED_WITH_ID:
                // Extracts out the ID from the URI
                selection = BelongingsContract.DonatedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateDonatedBelonging(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Updates the "donated" table with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    private int updateDonatedBelonging(Uri uri, ContentValues values, String selection,
                                                                      String[] selectionArgs) {

        // Checks that the user selected an image for the donated belonging
        if (values.containsKey(BelongingsContract.DonatedEntry.COLUMN_BELONGING_IMAGE)) {
            byte[] belongingImage = values.getAsByteArray
                    (BelongingsContract.DonatedEntry.COLUMN_BELONGING_IMAGE);
            if (belongingImage == null) {
                throw new IllegalArgumentException("Your belonging requires an image");
            }
        }

        // Checks that the user selected a name for the donated belonging
        if (values.containsKey(BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME)) {
            String name =
                    values.getAsString(BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Your belonging requires a name");
            }
        }

        // Avoids updating the "donated" table if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were updated
        int numRowsUpdated = database.update(
                BelongingsContract.DonatedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notifies registered listeners that the database has changed
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Updates "discarded" table with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    private int updateDiscardedBelonging(Uri uri, ContentValues values, String selection,
                                 String[] selectionArgs) {

        // Checks that the user selected an image for the discarded belonging
        if (values.containsKey(BelongingsContract.DiscardedEntry.COLUMN_BELONGING_IMAGE)) {
            byte[] belongingImage = values.getAsByteArray
                    (BelongingsContract.DiscardedEntry.COLUMN_BELONGING_IMAGE);
            if (belongingImage == null) {
                throw new IllegalArgumentException("Your belonging requires an image");
            }
        }

        // Checks that the user selected a name for the discarded belonging
        if (values.containsKey(BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME)) {
            String name =
                    values.getAsString(BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Your belonging requires a name");
            }
        }

        // Avoids updating the "discarded" table if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were updated
        int numRowsUpdated = database.update(
                BelongingsContract.DiscardedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notifies registered listeners that the database has changed
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Updates "sold" table with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    private int updateSoldBelonging(Uri uri, ContentValues values, String selection,
                                 String[] selectionArgs) {

        // Checks that the user selected an image for the sold belonging
        if (values.containsKey(BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE)) {
            byte[] belongingImage = values.getAsByteArray
                    (BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE);
            if (belongingImage == null) {
                throw new IllegalArgumentException("Your belonging requires an image");
            }
        }

        // Checks that the user selected a name for the sold belonging
        if (values.containsKey(BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME)) {
            String name =
                    values.getAsString(BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Your belonging requires a name");
            }
        }

        // Avoids updating the "sold" table if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were updated
        int numRowsUpdated = database.update(
                BelongingsContract.SoldEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notifies registered listeners that the database has changed
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Updates "belongings" table with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    private int updateBelongings(Uri uri, ContentValues values, String selection,
                                 String[] selectionArgs) {

        // Checks that the user selected an image for the belonging
        if (values.containsKey(BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE)) {
            byte[] belongingImage = values.getAsByteArray
                    (BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE);
            if (belongingImage == null) {
                throw new IllegalArgumentException("Your belonging requires an image");
            }
        }

        // Checks that the user selected a name for the belonging
        if (values.containsKey(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME)) {
            String name =
                    values.getAsString(BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Your belonging requires a name");
            }
        }

        // Avoids updating the "belonings" table if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were updated
        int numRowsUpdated = database.update(
                BelongingsContract.BelongingEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notifies registered listeners that the database has changed
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Updates "usage_log" table with the given content values. Applies the changes to the
     * row(s) specified in the selection and selection arguments
     *
     * @param uri           The full URI of the row(s) we wish to update
     * @param values        The data to update the database with
     * @param selection     A restriction to specify what specific rows to update
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were updated
     */
    private int updateUsageLogs(Uri uri, ContentValues values, String selection,
                                 String[] selectionArgs) {

        // Avoids updating the "usage_log" table if there is nothing to update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were updated
        int numRowsUpdated = database.update(
                BelongingsContract.UsageLogEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notifies registered listeners that the database has changed
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    /**
     * Deletes the data at the uri param, selection and selection arguments.
     *
     * @param uri           The full URI of the row(s) we wish to delete
     * @param selection     A restriction to specify what specific rows to delete
     * @param selectionArgs Used in conjunction with the selection statement to define restrictions
     * @return The number of rows that were deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // The number of rows that were deleted
        int numRowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                // Deletes all rows that match the selection and selection args
                numRowsDeleted = database.delete(BelongingsContract.BelongingEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_BELONGINGS_WITH_ID:
                // Deletes a single row given by the ID in the URI
                selection = BelongingsContract.BelongingEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(BelongingsContract.BelongingEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_LOG_USAGE_TABLE_WITH_ID:
                // Deletes a single row given by the ID in the URI
                selection = BelongingsContract.UsageLogEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(BelongingsContract.UsageLogEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_SOLD_WITH_ID:
                // Deletes a single row given by the ID in the URI
                selection = BelongingsContract.SoldEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(BelongingsContract.SoldEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_DISCARDED_WITH_ID:
                // Deletes a single row given by the ID in the URI
                selection = BelongingsContract.DiscardedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(BelongingsContract.DiscardedEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case CODE_DONATED_WITH_ID:
                // Deletes a single row given by the ID in the URI
                selection = BelongingsContract.DonatedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(BelongingsContract.DonatedEntry.TABLE_NAME,
                        selection, selectionArgs);
                if (numRowsDeleted > 0) {
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
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_BELONGINGS:
                return BelongingsContract.BelongingEntry.CONTENT_LIST_TYPE;
            case CODE_BELONGINGS_WITH_ID:
                return BelongingsContract.BelongingEntry.CONTENT_ITEM_TYPE;
            case CODE_LOG_USAGE_TABLE:
                return BelongingsContract.UsageLogEntry.CONTENT_LIST_TYPE;
            case CODE_LOG_USAGE_TABLE_WITH_ID:
                return BelongingsContract.UsageLogEntry.CONTENT_ITEM_TYPE;
            case CODE_SOLD:
                return BelongingsContract.SoldEntry.CONTENT_LIST_TYPE;
            case CODE_SOLD_WITH_ID:
                return BelongingsContract.SoldEntry.CONTENT_ITEM_TYPE;
            case CODE_DISCARDED:
                return BelongingsContract.DiscardedEntry.CONTENT_LIST_TYPE;
            case CODE_DISCARDED_WITH_ID:
                return BelongingsContract.DiscardedEntry.CONTENT_ITEM_TYPE;
            case CODE_DONATED:
                return BelongingsContract.DonatedEntry.CONTENT_LIST_TYPE;
            case CODE_DONATED_WITH_ID:
                return BelongingsContract.DonatedEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Creates the UriMatcher that will match each URI to int ID constants
     *
     * @return The newly created UriMatcher
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BelongingsContract.CONTENT_AUTHORITY;

        /*
         * Registers all possible URIs with the matcher and associates them with their appropriate
         * int ID codes.
         */
        matcher.addURI(authority, BelongingsContract.PATH_BELONGINGS, CODE_BELONGINGS);

        matcher.addURI(authority, BelongingsContract.PATH_BELONGINGS + "/#",
                CODE_BELONGINGS_WITH_ID);

        matcher.addURI(authority, BelongingsContract.PATH_BELONGINGS + "/*",
                CODE_BELONGINGS_WITH_SEARCH_FILTER);

        matcher.addURI(authority, BelongingsContract.PATH_USAGE_LOG, CODE_LOG_USAGE_TABLE);

        matcher.addURI(authority, BelongingsContract.PATH_USAGE_LOG + "/#",
                CODE_LOG_USAGE_TABLE_WITH_ID);

        matcher.addURI(authority, BelongingsContract.PATH_SOLD, CODE_SOLD);

        matcher.addURI(authority, BelongingsContract.PATH_SOLD + "/#", CODE_SOLD_WITH_ID);

        matcher.addURI(authority, BelongingsContract.PATH_SOLD + "/*", CODE_SOLD_WITH_SEARCH_FILTER);

        matcher.addURI(authority, BelongingsContract.PATH_DISCARDED, CODE_DISCARDED);

        matcher.addURI(authority, BelongingsContract.PATH_DISCARDED + "/#",
                CODE_DISCARDED_WITH_ID);

        matcher.addURI(authority, BelongingsContract.PATH_DISCARDED + "/*",
                CODE_DISCARDED_WITH_SEARCH_FILTER);

        matcher.addURI(authority, BelongingsContract.PATH_DONATED, CODE_DONATED);

        matcher.addURI(authority, BelongingsContract.PATH_DONATED + "/#", CODE_DONATED_WITH_ID);

        matcher.addURI(authority, BelongingsContract.PATH_DONATED + "/*",
                CODE_DONATED_WITH_SEARCH_FILTER);

        return matcher;
    }

    /**
     * Getter method that returns a reference to a sqlite open helper.
     *
     * @return reference to sqlite open helper
     */
    public static BelongingsDbHelper getDbHelper(){
        return mDbHelper;
    }
}
