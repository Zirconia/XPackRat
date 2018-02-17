package com.example.android.x_packrat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Manages a local database that contains data about the user's belongings
 */
public class BelongingsDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "possessions.db";

    // SQL "belongings" table creation constant
    public static final String SQL_CREATE_BELONGINGS_TABLE = "CREATE TABLE "
            + BelongingsContract.BelongingEntry.TABLE_NAME + " ("
            + BelongingsContract.BelongingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE + " BLOB NOT NULL, "
            + BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME + " TEXT NOT NULL, "
            + BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE + " INTEGER NOT NULL);";

    // SQL "usage_log" table creation constant
    public static final String SQL_CREATE_USAGE_LOG_TABLE = "CREATE TABLE "
            + BelongingsContract.UsageLogEntry.TABLE_NAME + " ("
            + BelongingsContract.BelongingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID + " INTEGER NOT NULL, "
            + BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE + " INTEGER NOT NULL );";

    // SQL "belongings" table deletion constant
    public static final String SQL_DELETE_BELONGINGS_TABLE =
            "DROP TABLE " + BelongingsContract.BelongingEntry.TABLE_NAME;

    // SQL "usage_log" table deletion constant
    public static final String SQL_DELETE_USAGE_LOG_TABLE =
            "DROP TABLE " + BelongingsContract.UsageLogEntry.TABLE_NAME;

    public BelongingsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables happens.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("SQL_CHECK", SQL_CREATE_BELONGINGS_TABLE);
        db.execSQL(SQL_CREATE_BELONGINGS_TABLE);
        db.execSQL(SQL_CREATE_USAGE_LOG_TABLE);
    }

    /**
     * For now we simply implement this method because it is required
     * in order for the BelongingsDbHelper class to extend SQLiteOpenHelper.
     *
     * @param db         Database that is being upgraded
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
