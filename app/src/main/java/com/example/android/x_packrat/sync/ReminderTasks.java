package com.example.android.x_packrat.sync;

import android.content.Context;
import android.content.Intent;

import com.example.android.x_packrat.MainActivity;
import com.example.android.x_packrat.utilities.NotificationUtils;

/**
 * Contains methods that kick off the creation of or destruction of notifications
 */
public class ReminderTasks {

    /*
     * Indicates the task that we wish to execute
     */
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static final String ACTION_CHECK_BELONGINGS = "check-belongings";
    static final String ACTION_USAGE_REMINDER = "usage-reminder";

    /**
     * Called in the background by UsageReminderFirebaseJobService to start the process for creating
     * a notification or for clearing one.
     *
     * @param context The context from which this function was called
     * @param action  Indicates what action should be taken(show notification or clear it)
     */
    public static void executeTask(Context context, String action) {
        if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            NotificationUtils.clearAllNotifications(context);
        } else if (ACTION_USAGE_REMINDER.equals(action)) {
            issueUsageReminder(context);
        } else if (ACTION_CHECK_BELONGINGS.equals(action)) {
            launchMainActivity(context);
        }
    }

    /**
     * Calls utility method to create a reminder notification.
     *
     * @param context The context from which this function was called
     */
    private static void issueUsageReminder(Context context) {
        NotificationUtils.remindOfLongUnusedBelonging(context);
    }

    /**
     * @param context The context from which this function was called
     */
    private static void launchMainActivity(Context context) {
        Intent checkBelongingsIntent = new Intent(context, MainActivity.class);
        context.startActivity(checkBelongingsIntent);
    }
}
