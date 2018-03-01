package com.example.android.x_packrat.data;


import android.content.ContentValues;

import static com.example.android.x_packrat.data.BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE;
import static com.example.android.x_packrat.data.BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME;
import static com.example.android.x_packrat.data.BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE;
import static com.example.android.x_packrat.data.BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID;
import static com.example.android.x_packrat.data.BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE;

public class TestUtilities {

    // October 1st, 2016 at midnight, GMT time
    static final long DATE_NORMALIZED = 1475280000000L;

    static ContentValues createTestBelongingContentValues() {
        String belongingName = "testName";
        byte[] belongingImage = {};

        ContentValues testBelongingValues = new ContentValues();

        // All of the columns in the belongings table that cannot have a null entry
        testBelongingValues.put(COLUMN_LAST_USED_DATE, DATE_NORMALIZED);
        testBelongingValues.put(COLUMN_BELONGING_IMAGE, belongingName);
        testBelongingValues.put(COLUMN_BELONGING_NAME, belongingImage);


        return testBelongingValues;
    }

    static ContentValues createTestLogContentValues() {
        long belongingId = 250;

        ContentValues testBelongingValues = new ContentValues();

        // All of the columns in the usage_log table that cannot have a null entry
        testBelongingValues.put(COLUMN_USAGE_DATE, DATE_NORMALIZED);
        testBelongingValues.put(COLUMN_BELONGING_ID, belongingId);;

        return testBelongingValues;
    }

    static ContentValues createBadTestBelongingContentValues() {
        String belongingName = "testName";
        byte[] belongingImage = {};

        ContentValues testBelongingValues = new ContentValues();

        // We leave off the column for the name of the belonging so that the belongings
        // table creation specifications in the BelongingsDbHelper class will not be satisfied,
        // thus hopefully causing an error
        testBelongingValues.put(COLUMN_LAST_USED_DATE, DATE_NORMALIZED);
        testBelongingValues.put(COLUMN_BELONGING_IMAGE, belongingName);

        return testBelongingValues;
    }

    static ContentValues createBadTestLogContentValues() {
        long belongingId = 250;

        ContentValues testBelongingValues = new ContentValues();

        // Left off the COLUMN_USAGE_DATE column in order to cause an error
        // when attempting to insert into the usage_log table with these values
        testBelongingValues.put(COLUMN_BELONGING_ID, belongingId);;

        return testBelongingValues;
    }

    static String[] createBelongingsTableProjection(){
        String[] projection = {BelongingsContract.BelongingEntry._ID,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
                BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE,};

        return projection;
    }

    static String[] createLogTableProjection(){
        String[] projection = {BelongingsContract.UsageLogEntry._ID,
                BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID,
                BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE};

        return projection;
    }
}
