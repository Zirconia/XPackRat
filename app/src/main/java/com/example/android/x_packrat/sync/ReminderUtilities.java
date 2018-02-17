package com.example.android.x_packrat.sync;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.data.XPackRatPreferences;
import com.example.android.x_packrat.utilities.XPackRatDateUtils;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Contains fields used for scheduling the job that handles showing the user recurring
 * notifications. Contains methods for scheduling notification job.
 */
public class ReminderUtilities {

    // Fields used when scheduling a job in order to indicate how frequently that job will run.
    // We use them to indicate how often our job service checks whether or not the user has
    // belongings stored in the database that they have not used in awhile.
    private static int reminderIntervalDays = 7;
    private static int reminderIntervalSeconds = (int) (TimeUnit.DAYS.toSeconds(reminderIntervalDays));
    private static int syncFlextimeSeconds = reminderIntervalSeconds;

    private static final String REMINDER_JOB_TAG = "usage_reminder_tag";

    // Indicates whether or not a recurring job to notify the user of long unused belongings is
    // scheduled.
    private static boolean sInitialized;

    /**
     * Schedules a recurring job to trigger UsageReminderFirebaseJobService to start the process for
     * creating a reminder notification.
     *
     * @param context The context from which this method was called
     */
    synchronized public static void scheduleUsageReminder(@NonNull final Context context) {

        // Avoids scheduling the job if the job to show reminder notifications has already been
        // scheduled
        if (sInitialized) return;

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        // Builds a job which:
        // - has UsageReminderFirebaseJobService as it's service
        // - has the tag REMINDER_JOB_TAG
        // - has the lifetime of the job as forever(keeps job from dying after device reboot)
        // - has the job recur
        // - sets it to occur with a frequency based on the user's preference specified in the
        //   Settings(once a week by default)
        // - replaces the current job if a job with the same tag is already running
        // - constrains the job to run on any network
        // - retries the job in linear time in the case of an execution failure
        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(UsageReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                // The first argument for Trigger class's executionWindow method is the start
                // of the time frame when the job should be performed. The second argument is the
                // latest point in time at which the user should receive a notification.
                .setTrigger(Trigger.executionWindow(
                        reminderIntervalSeconds,
                        syncFlextimeSeconds))
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .build();

        // Schedules the job
        dispatcher.mustSchedule(constraintReminderJob);

        // Indicates that the job has been scheduled
        sInitialized = true;

        // Stores indicator in persistent local storage to be retrieved upon device reboots or app
        // restarts
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("job_initialized", sInitialized);
        editor.apply();
    }

    /**
     * Updates the fields that are used to determine the frequency at which the user receives
     * notifications.(fields are used when scheduling a job and the job fires off code that creates
     * the reminder notifications)
     *
     * @param numOfDays How frequently the user wishes to receive notifications(in days)
     */
    public static void updateReminderIntervals(int numOfDays) {
        reminderIntervalDays = numOfDays;
        reminderIntervalSeconds = (int) (TimeUnit.DAYS.toSeconds(reminderIntervalDays));
        syncFlextimeSeconds = reminderIntervalSeconds;

        // The values of the fields above have changed so we set this to false to allow another
        // job with the same tag as the old one to be scheduled( the new job replaces the old one)
        sInitialized = false;
    }

    /**
     * Helper method to determine whether or not to show the user a usage reminder notification.
     * Invoked when the reminder notification job is executing, before a notification is displayed.
     *
     * @param data    The data retrieved from querying the local database("possessions.db")
     * @param context The context from which this method was called
     * @return True if their is a belonging or belongings in the database that has not been used
     * in a long while. This "long while" is determined by the notification frequency that
     * the user has set. For example, if the user has a belonging that has not been used for
     * a week or longer and they currently have Reminder Frequency set to "weekly" when the
     * firebase job is executing, then it will be determined that they have belongings that
     * they have not used in awhile. If all of their belongings have their most recent usage
     * logged as one day ago, however, it will be determined that the user does NOT have any
     * belongings that they have not used in a long while.
     */
    public static boolean hasLongUnusedBelonging(Cursor data, Context context) {

        // Moves cursor to last row in the data since the belonging in the last row
        // has the oldest last used date and time
        if (data.moveToLast()) {
            long longestUnusedDatetime = data.getLong(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE));

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String currReminderInterval = preferences.getString("frequency", "weekly");

            long currDateTime = XPackRatDateUtils.getCurrDatetime();
            long reminderInterval = XPackRatDateUtils.convertDaysToMilliseconds(currReminderInterval);

            if (Math.abs(longestUnusedDatetime - currDateTime) >= reminderInterval) {
                return true;
            }
        }

        return false;
    }
}
