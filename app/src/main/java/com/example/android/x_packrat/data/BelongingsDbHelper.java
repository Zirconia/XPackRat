package com.example.android.x_packrat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Manages a local database that contains data about the user's belongings
 */
public class BelongingsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = BelongingsDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "possessions.db";

    // SQL "belongings" table creation constant
    private static final String SQL_CREATE_BELONGINGS_TABLE = "CREATE TABLE "
            + BelongingsContract.BelongingEntry.TABLE_NAME + " ("
            + BelongingsContract.BelongingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE + " BLOB NOT NULL, "
            + BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME + " TEXT NOT NULL, "
            + BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE + " INTEGER NOT NULL);";

    // SQL "usage_log" table creation constant
    private static final String SQL_CREATE_USAGE_LOG_TABLE = "CREATE TABLE "
            + BelongingsContract.UsageLogEntry.TABLE_NAME + " ("
            + BelongingsContract.UsageLogEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID + " INTEGER NOT NULL, "
            + BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE + " INTEGER NOT NULL, "
            + BelongingsContract.UsageLogEntry.COLUMN_USAGE_DESCRIPTION + " TEXT);";

    // SQL "sold" table creation constant
    private static final String SQL_CREATE_SOLD_TABLE = "CREATE TABLE "
            + BelongingsContract.SoldEntry.TABLE_NAME + " ("
            + BelongingsContract.SoldEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE + " BLOB NOT NULL, "
            + BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME + " TEXT NOT NULL, "
            + BelongingsContract.SoldEntry.COLUMN_SOLD_TO + " TEXT);";

    // SQL "discarded" table creation constant
    private static final String SQL_CREATE_DISCARDED_TABLE = "CREATE TABLE "
            + BelongingsContract.DiscardedEntry.TABLE_NAME + " ("
            + BelongingsContract.DiscardedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.DiscardedEntry.COLUMN_BELONGING_IMAGE + " BLOB NOT NULL, "
            + BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME + " TEXT NOT NULL);";

    // SQL "donated" table creation constant
    private static final String SQL_CREATE_DONATED_TABLE = "CREATE TABLE "
            + BelongingsContract.DonatedEntry.TABLE_NAME + " ("
            + BelongingsContract.DonatedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BelongingsContract.DonatedEntry.COLUMN_BELONGING_IMAGE + " BLOB NOT NULL, "
            + BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME + " TEXT NOT NULL, "
            + BelongingsContract.DonatedEntry.COLUMN_DONATED_TO + " TEXT);";

    // SQL "belongings" table deletion constant
    private static final String SQL_DELETE_BELONGINGS_TABLE =
            "DROP TABLE " + BelongingsContract.BelongingEntry.TABLE_NAME;

    // SQL "usage_log" table deletion constant
    private static final String SQL_DELETE_USAGE_LOG_TABLE =
            "DROP TABLE " + BelongingsContract.UsageLogEntry.TABLE_NAME;

    // SQL "sold" table deletion constant
    private static final String SQL_DELETE_SOLD_TABLE =
            "DROP TABLE " + BelongingsContract.SoldEntry.TABLE_NAME;

    // SQL "discarded" table deletion constant
    private static final String SQL_DELETE_DISCARDED_TABLE =
            "DROP TABLE " + BelongingsContract.DiscardedEntry.TABLE_NAME;

    // SQL "donated" table deletion constant
    private static final String SQL_DELETE_DONATED_TABLE =
            "DROP TABLE " + BelongingsContract.DonatedEntry.TABLE_NAME;



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
        db.execSQL(SQL_CREATE_BELONGINGS_TABLE);
        db.execSQL(SQL_CREATE_USAGE_LOG_TABLE);
        db.execSQL(SQL_CREATE_SOLD_TABLE);
        db.execSQL(SQL_CREATE_DISCARDED_TABLE);
        db.execSQL(SQL_CREATE_DONATED_TABLE);
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
