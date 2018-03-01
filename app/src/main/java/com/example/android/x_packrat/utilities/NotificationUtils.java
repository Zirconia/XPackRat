package com.example.android.x_packrat.utilities;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.x_packrat.MainActivity;
import com.example.android.x_packrat.R;
import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.data.BelongingsDbHelper;
import com.example.android.x_packrat.sync.ReminderTasks;
import com.example.android.x_packrat.sync.ReminderUtilities;
import com.example.android.x_packrat.sync.UsageReminderIntentService;

/**
 * Contains methods for constructing notifications and pending intents
 */
public class NotificationUtils {

    // Used to access notifications after they have been displayed(for updating or cancelling)
    private static final int USAGE_REMINDER_NOTIFICATION_ID = 1138;
    // Unique identifier for accessing a pending intent
    private static final int USAGE_REMINDER_PENDING_INTENT_ID = 3417;
    // Unique identifier for the notification channel that will be associated with notifications
    // from this app(used in Android O)
    private static final String USAGE_REMINDER_NOTIFICATION_CHANNEL_ID =
            "reminder_notification_channel";
    // Unique identifier for the pending intent that handles dismissing a notification
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 14;

    /**
     * Creates and displays the notification that informs the user that they have a belonging or
     * belongings that they have not used for a long time.
     *
     * @param context The context from which this method was called
     */
    public static void remindOfLongUnusedBelonging(Context context) {

        Cursor cursor = queryForLongUnused(context);

        // Avoids showing a notification if the user does not have any belongings that they have not
        // used in a long time
        if (!ReminderUtilities.hasLongUnusedBelonging(cursor, context)) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Checks if the user's device is running Android Oreo and creates a notification channel
        // to associate the notification manager with if so
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The channel is given high importance so that notifications will appear up top in the
            // notification bar
            NotificationChannel mChannel = new NotificationChannel(
                    USAGE_REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Constructs a notification which:
        // -has its color set to be the same as this app's main color
        // -has its icon that appears in the notification bar set
        // -has its icon on the actual notification set
        // -has its title set to indicate to the user that they have belongings that they have not
        //  used for awhile
        // -has its content text set to advice user to do something about what is mentioned in the
        // title
        // -has its style set to make the notification text big
        // -is set to vibrate the device upon displaying the notification
        // -has its content intent set to bring the app into the foreground when the notification is
        // pressed
        // -has an optional action added to the notification to dismiss the notification
        // -is set to be canceled when the user clicks it
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                USAGE_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_image_black_36dp)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.usage_reminder_notification_title))
                .setContentText(context.getString(R.string.usage_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.usage_reminder_notification_body)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(ignoreReminderAction(context))
                .setAutoCancel(true);

        // Guarantees heads up popup notification will be displayed in Android version Jelly Bean
        // and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        // Notifies the notification manager that we have built a new notification
        if(notificationManager != null)
            notificationManager.notify(USAGE_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Creates the intent that causes this app's MainActivity to be brought into the foreground
     * when a notification is clicked.
     *
     * @param context The context from which this method was called
     * @return The pending intent that launches the main activity
     */
    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                USAGE_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Gets the Bitmap Representation of the image to use for the notification's large icon
     *
     * @param context The context from which this method was called
     * @return The Bitmap representation of the large image for the notification
     */
    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.ic_launcher_foreground);
    }

    /**
     * Clears all notifications from the notification bar
     *
     * @param context The context from which this method was called
     */
    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.cancelAll();
    }

    /**
     * Gets the Bitmap Representation of the image to use for the notification's large icon
     *
     * @param context The context from which this method was called
     * @return The action associated with dismissing a notification
     */
    private static NotificationCompat.Action ignoreReminderAction(Context context) {
        Intent ignoreReminderIntent = new Intent(context, UsageReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTasks.ACTION_DISMISS_NOTIFICATION);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action(
                R.drawable.ic_add_white_36dp,
                "No, thanks.",
                ignoreReminderPendingIntent);
    }

    /**
     * Helper method that supplies the cursor for the call to ReminderUtilities's
     * hasLongUnusedBelonging method in this class's remindOfLongUnusedBelonging method.
     * The data in the cursor is used to determine whether or not there are any belongings
     * that the user has not used in a long time.
     *
     * @param context The context from which this method was called
     * @return Cursor containing the result of a query of the local database "possessions.db"
     */
    private static Cursor queryForLongUnused(Context context) {
        String[] projection = {
                BelongingsContract.BelongingEntry._ID,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
                BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
                BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE
        };

        // Sort belongings in descending order by last used date
        String sortOrder = BelongingsContract.BelongingEntry.
                COLUMN_LAST_USED_DATE + " DESC";

        BelongingsDbHelper belongingsDbHelper = new BelongingsDbHelper(context);
        SQLiteDatabase database = belongingsDbHelper.getReadableDatabase();

        return context.getContentResolver().query(
                BelongingsContract.BelongingEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );
    }
}
