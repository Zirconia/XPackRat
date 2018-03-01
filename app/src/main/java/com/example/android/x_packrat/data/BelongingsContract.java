package com.example.android.x_packrat.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database "possessions.db"
 */
public class BelongingsContract {

    public static final String LOG_TAG = BelongingsContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.example.android.x_packrat";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URIs
     */
    public static final String PATH_BELONGINGS = "belongings";
    public static final String PATH_USAGE_LOG = "usage_log";
    public static final String PATH_SOLD = "sold";
    public static final String PATH_DISCARDED = "discarded";
    public static final String PATH_DONATED = "donated";

    /*
     * Inner class that defines the table contents of the "belongings" table
     */
    public static final class BelongingEntry implements BaseColumns {

        public static final String TABLE_NAME = "belongings";

        /*
         *Names of all of the columns in the "belongings" table
         */
        public static final String COLUMN_BELONGING_IMAGE = "belonging_image";
        public static final String COLUMN_BELONGING_NAME = "belonging_name";
        public static final String COLUMN_LAST_USED_DATE = "last_used_date";
        /*Note: their is no time column because both the date and time are stored
        * under the date column as one integer representing date and time*/

        // The base CONTENT_URI used to query the "belongings" table from the content provider
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
    }

    /*
     * Inner class that defines the contents of a "usage_log" table
     */
    public static final class UsageLogEntry implements BaseColumns {

        public static final String TABLE_NAME = "usage_log";

        /*
         *Names of all of the columns in a "usage_log" table
         */
        public static final String COLUMN_USAGE_DATE = "usage_date";
        public static final String COLUMN_BELONGING_ID = "belonging_id";
        public static final String COLUMN_USAGE_DESCRIPTION = "usage_description";
        /*Note: their is no time column because both the date and time are stored
        * under the date column as one integer representing date and time*/

        // The base CONTENT_URI used to query the "usage_log" table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_USAGE_LOG)
                .build();

        /**
         * The MIME type of the content URI for a list of usage logs
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_USAGE_LOG;

        /**
         * The MIME type of the content URI for a single belonging
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_USAGE_LOG;
    }

    /*
     * Inner class that defines the contents of a table for belongings the user has sold
     */
    public static final class SoldEntry implements BaseColumns {

        public static final String TABLE_NAME = "sold";

        /*
         *Names of all of the columns in a "sold" table
         */
        public static final String COLUMN_BELONGING_IMAGE = "belonging_image";
        public static final String COLUMN_BELONGING_NAME = "belonging_name";
        public static final String COLUMN_SOLD_TO = "sold_to";

        // The base CONTENT_URI used to query the "sold" table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SOLD)
                .build();

        /**
         * The MIME type of the content URI for a list of sold belongings
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_SOLD;

        /**
         * The MIME type of the content URI for a single sold belonging
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_SOLD;
    }

    /*
     * Inner class that defines the contents of a table for belongings the user has discarded
     */
    public static final class DiscardedEntry implements BaseColumns {

        public static final String TABLE_NAME = "discarded";

        /*
         *Names of all of the columns in a "discarded" table
         */
        public static final String COLUMN_BELONGING_IMAGE = "belonging_image";
        public static final String COLUMN_BELONGING_NAME = "belonging_name";

        // The base CONTENT_URI used to query the "discarded" table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DISCARDED)
                .build();

        /**
         * The MIME type of the content URI for a list of discarded belongings
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_DISCARDED;

        /**
         * The MIME type of the content URI for a single discarded belonging
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_DISCARDED;
    }

    /*
     * Inner class that defines the contents of a table for belongings the user has donated
     */
    public static final class DonatedEntry implements BaseColumns {

        public static final String TABLE_NAME = "donated";

        /*
         *Names of all of the columns in a "donated" table
         */
        public static final String COLUMN_BELONGING_IMAGE = "belonging_image";
        public static final String COLUMN_BELONGING_NAME = "belonging_name";
        public static final String COLUMN_DONATED_TO = "donated_to";

        // The base CONTENT_URI used to query the "donated" table from the content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DONATED)
                .build();

        /**
         * The MIME type of the content URI for a list of donated belongings
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_DONATED;

        /**
         * The MIME type of the content URI for a single donated belonging
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_DONATED;
    }
}
