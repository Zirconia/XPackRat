package com.example.android.x_packrat.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the possessions database
 */
public class BelongingsContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.x_packrat";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's
     */
    public static final String PATH_BELONGINGS = "belongings";

    /*
     * Inner class that defines the table contents of the Belongings table
     */
    public static final class BelongingEntry implements BaseColumns {

        // The base CONTENT_URI used to query the Belongings table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BELONGINGS)
                .build();

        /**
         * The MIME type of the content URI for a list of belongings
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_BELONGINGS;

        /**
         * The MIME type of the content URI for a single belonging
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_BELONGINGS;

        public static final String TABLE_NAME = "belongings";

        /*
         *Names of all of the columns in the belongings table
         */
        public static final String COLUMN_BELONGING_IMAGE = "belonging_image";
        public static final String COLUMN_BELONGING_NAME = "belonging_name";
        public static final String COLUMN_LAST_USED_DATE = "last_used_date";
        /*Note: their is no time column because both the date and time are stored
        * under the date column as one integer representing date and time*/
    }

    /*
     * Inner class that defines the contents of a Usage Log table
     */
    public static final class UsageLogEntry implements BaseColumns {

        public static final String TABLE_NAME = "usage_log";

        public static String UNIQUE_TABLE_URI = "usage_log";

        public static String UNIQUE_TABLE_NAME = "";

        /*
         *Names of all of the columns in a Usage Log table
         */
        public static final String COLUMN_USAGE_DATE = "usage_date";
        /*Note: their is no time column because both the date and time are stored
        * under the date column as one integer representing date and time*/
    }
}
