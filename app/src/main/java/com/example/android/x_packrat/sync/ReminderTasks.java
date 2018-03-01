package com.example.android.x_packrat.sync;

import android.content.Context;

import com.example.android.x_packrat.utilities.NotificationUtils;

/**
 * Contains methods that kick off the creation of or destruction of notifications
 */
public class ReminderTasks {

    /*
     * Indicates the task that we wish to execute
     */
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
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
}
