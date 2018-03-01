package com.example.android.x_packrat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import static com.example.android.x_packrat.data.BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME;
import static com.example.android.x_packrat.data.BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import com.example.android.x_packrat.data.BelongingsContract.BelongingEntry;

/**
 * Instrumented test class to check for proper performance of local "possessions.db" database
 */
@RunWith(AndroidJUnit4.class)
public class TestXPackRatDatabase {

    private final Context context = InstrumentationRegistry.getTargetContext();

    private static final String packageName = "com.example.android.x_packrat";
    private static final String dataPackageName = packageName + ".data";

    private Class belongingsEntryClass;
    private Class belongingsDbHelperClass;
    private static final String BelongingsContractName = ".BelongingsContract";
    private static final String BelongingsEntryName = BelongingsContractName + "$BelongingEntry";
    private static final String BelongingsDbHelperName = ".BelongingsDbHelper";

    private static final String DATABASE_NAME = "possessions.db";

    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    @Before
    public void before() {
        try {
            belongingsEntryClass = Class.forName(dataPackageName + BelongingsEntryName);
            belongingsDbHelperClass = Class.forName(dataPackageName + BelongingsDbHelperName);

            Constructor belongingsDbHelperCtor = belongingsDbHelperClass.getConstructor(Context.class);

            dbHelper = (SQLiteOpenHelper) belongingsDbHelperCtor.newInstance(context);

            context.deleteDatabase(DATABASE_NAME);

            Method getWritableDatabase = SQLiteOpenHelper.class.getDeclaredMethod("getWritableDatabase");
            database = (SQLiteDatabase) getWritableDatabase.invoke(dbHelper);

        } catch (ClassNotFoundException e) {
            fail(e.getMessage());
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        } catch (NoSuchMethodException e) {
            fail(e.getMessage());
        } catch (InstantiationException e) {
            fail(e.getMessage());
        } catch (InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that the database is created properly with all of the expected tables.
     * Note: Aside from the tables created in the BelongingsDbHelper constructor,
     * there should also be 2 tables that are auto-generated(android_metadata,
     * sqlite_sequence). The android_metadata table holds meta information about
     * the app and the sqlite_sequence table keeps track of the largest ROWID and is
     * created and initialized automatically whenever a normal table that contains an
     * AUTOINCREMENT column is created.
     */
    @Test
    public void testCreateDb() {
        // Stores names of all expected tables
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add("belongings");
        tableNameHashSet.add("usage_log");

        // Checks that the database is open
        String databaseIsNotOpen = "The database should be open and isn't";
        assertEquals(databaseIsNotOpen,
                true,
                database.isOpen());

        // Queries database for all tables
        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);

        // An error message for the case where there are no tables in the database
        String errorInCreatingDatabase =
                "Error: This means that the database has not been created correctly";

        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());

        // Should remove all tables from the hash set, while iterating through each table in the cursor.
        // If all of the tables are not removed, we know there is an error.
        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        assertTrue("Error: Your database was created without the expected tables.",
                tableNameHashSet.isEmpty());

        tableNameCursor.close();
    }

    /**
     * Tests that rows are inserted into the belongings table properly and that
     * each new row has an ID greater than the last because they should be being auto
     * incremented. Checks that bad rows are not inserted.
     */
    @Test
    public void testInsertIntoBelongingsTable() {

        String errorInsertingIntoDatabase =
                "Error: Database insertion failed";

        // Checks that belonging is inserted correctly
        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createTestBelongingContentValues());

        assertFalse(errorInsertingIntoDatabase,newRowId == -1);

        // Checks that subsequently inserted belongings have greater IDs than previous inserted
        // belongings
        long nextRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createTestBelongingContentValues());

        assertTrue(errorInsertingIntoDatabase,nextRowId > newRowId);

        // Checks that insert of bad contentValues will fail
        long badRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createBadTestBelongingContentValues());

        assertTrue(errorInsertingIntoDatabase,badRowId == -1);
    }

    /**
     * Tests that rows are inserted into the usage_log table properly and that
     * each new row has an ID greater than the last because they should be being auto
     * incremented. Checks that bad rows are not inserted.
     */
    @Test
    public void testInsertIntoLogsTable() {

        String errorInsertingIntoDatabase =
                "Error: Database insertion failed";

        // Checks that usage log is inserted correctly
        long newRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createTestLogContentValues());

        assertFalse(errorInsertingIntoDatabase,newRowId == -1);

        // Checks that subsequently inserted logs have greater IDs than previous inserted
        // belongings
        long nextRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createTestLogContentValues());

        assertTrue(errorInsertingIntoDatabase,nextRowId > newRowId);

        // Checks that insert of bad contentValues will fail
        long badRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createBadTestLogContentValues());

        assertTrue(errorInsertingIntoDatabase,badRowId == -1);
    }

    @Test
    public void testDeleteFromBelongingsTable(){

        String errorDeletingFromDatabase =
                "Error: Database deletion failed";

        // Inserts a row into the belongings table to be deleted
        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createTestBelongingContentValues());

        String selection = BelongingsContract.BelongingEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        // Deletes the row that was inserted above
        int numRowsDeleted = database.delete(BelongingEntry.TABLE_NAME,
                selection, selectionArgs);

        assertTrue(errorDeletingFromDatabase,numRowsDeleted == 1);

        // Tries to delete the row again. Delete should return 0 here as there shouldn't be any rows
        // with the id "newRowId" in the database anymore
        int rowsDeleted = database.delete(BelongingEntry.TABLE_NAME,
                selection, selectionArgs);

        assertTrue(errorDeletingFromDatabase,rowsDeleted == 0);
    }

    @Test
    public void testDeleteFromLogsTable(){

        String errorDeletingFromDatabase =
                "Error: Database deletion failed";

        // Inserts a row into the belongings table to be deleted
        long newRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createTestLogContentValues());

        String selection = BelongingsContract.UsageLogEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        // Deletes the row that was inserted above
        int numRowsDeleted = database.delete(BelongingsContract.UsageLogEntry.TABLE_NAME,
                selection, selectionArgs);

        assertTrue(errorDeletingFromDatabase,numRowsDeleted == 1);

        // Tries to delete the row again. Delete should return 0 here as there shouldn't be any rows
        // with the id "newRowId" in the database anymore
        int rowsDeleted = database.delete(BelongingsContract.UsageLogEntry.TABLE_NAME,
                selection, selectionArgs);

        assertTrue(errorDeletingFromDatabase,rowsDeleted == 0);
    }

    @Test
    public void testUpdateBelongingsTable(){

        String errorUpdatingDatabase =
                "Error: Failed to update database.";

        // Inserts a row into the belongings table to be updated
        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createTestBelongingContentValues());

        String selection = BelongingsContract.BelongingEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        // Creates content values with a new name to overwrite the old one
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BELONGING_NAME, "newName");

        int numRowsUpdated = database.update(BelongingsContract.BelongingEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs);

        assertTrue(errorUpdatingDatabase,numRowsUpdated == 1);
    }

    @Test
    public void testUpdateLogsTable(){

        String errorUpdatingDatabase =
                "Error: Failed to update database.";

        // Inserts a row into the belongings table to be updated
        long newRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createTestLogContentValues());

        String selection = BelongingsContract.UsageLogEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        // Creates content values with a new name to overwrite the old one
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BELONGING_ID, 5000);

        int numRowsUpdated = database.update(BelongingsContract.UsageLogEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs);

        assertTrue(errorUpdatingDatabase,numRowsUpdated == 1);
    }

    @Test
    public void testQueryBelongingsTable(){

        String errorQueryingDatabase =
                "Error: Failed to query database.";

        // Inserts a row into the belongings table to be queried for
        long newRowId = database.insert(BelongingsContract.BelongingEntry.TABLE_NAME,
                null, TestUtilities.createTestBelongingContentValues());

        Uri queryUri = Uri.parse(BelongingEntry.CONTENT_URI + "/" + newRowId);
        String selection = BelongingsContract.BelongingEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        Cursor queryResults = database.query(BelongingEntry.TABLE_NAME,
                TestUtilities.createBelongingsTableProjection(), selection, selectionArgs,
                null,null, null);

        int resultCount = queryResults.getCount();

        assertTrue(errorQueryingDatabase,resultCount == 1);

        // Deletes the row that was inserted above
        int numRowsDeleted = database.delete(BelongingsContract.BelongingEntry.TABLE_NAME,
                selection, selectionArgs);

        // Queries for the row again. We should get back 0 results since we have deleted the row
        // above
        Cursor queryResult = database.query(BelongingEntry.TABLE_NAME,
                TestUtilities.createBelongingsTableProjection(), selection, selectionArgs,
                null,null, null);

        resultCount = queryResult.getCount();

        assertTrue(errorQueryingDatabase,resultCount == 0);

        queryResults.close();
        queryResult.close();
    }

    @Test
    public void testQueryLogsTable(){

        String errorQueryingDatabase =
                "Error: Failed to query database.";

        // Inserts a row into the belongings table to be queried for
        long newRowId = database.insert(BelongingsContract.UsageLogEntry.TABLE_NAME,
                null, TestUtilities.createTestLogContentValues());

        Uri queryUri = Uri.parse(BelongingsContract.UsageLogEntry.CONTENT_URI + "/" + newRowId);
        String selection = BelongingsContract.UsageLogEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(newRowId)};

        Cursor queryResults = database.query(BelongingsContract.UsageLogEntry.TABLE_NAME,
                TestUtilities.createLogTableProjection(), selection, selectionArgs,
                null,null, null);

        int resultCount = queryResults.getCount();

        assert(resultCount == 1);

        // Deletes the row that was inserted above
        int numRowsDeleted = database.delete(BelongingsContract.UsageLogEntry.TABLE_NAME,
                selection, selectionArgs);

        // Queries for the row again. We should get back 0 results since we have deleted the row
        // above
        Cursor queryResult = database.query(BelongingsContract.UsageLogEntry.TABLE_NAME,
                TestUtilities.createLogTableProjection(), selection, selectionArgs,
                null,null, null);

        resultCount = queryResult.getCount();

        assertTrue(errorQueryingDatabase,resultCount == 0);

        queryResults.close();
        queryResult.close();
    }

    @After
    public void closeDb() throws IOException {
        database.close();
    }
}
