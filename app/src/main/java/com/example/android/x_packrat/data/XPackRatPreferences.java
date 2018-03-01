package com.example.android.x_packrat.data;


import android.content.Context;

import com.example.android.x_packrat.sync.ReminderUtilities;

/**
 * Contains methods used for the updating of shared preferences
 */
public class XPackRatPreferences {

    /*
     * Defines textual meanings of periods of days with their corresponding int values
     */
    public static final int DAYS_IN_A_WEEK = 7;
    public static final int DAYS_IN_TWO_WEEKS = 14;
    public static final int DAYS_IN_A_MONTH = 30;
    public static final int DAYS_IN_TWO_MONTHS = 60;

    /**
     * Calls appropriate utility functions to update how frequently the user receives
     * reminder notifications based on their selection in Settings.
     *
     * @param context    The context from which this function was called
     * @param newPrefVal The new value for the reminder frequency option in the Settings fragment
     */
    public static void updateReminderFrequency(Context context, String newPrefVal) {
        int intervalInDays = frequencyToDays(newPrefVal);
        ReminderUtilities.updateReminderIntervals(intervalInDays);
        ReminderUtilities.scheduleUsageReminder(context);
    }

    /**
     * Converts the user's selected string preference to an integer that represents the number of
     * days that is typically associated with it.
     *
     * @param pref The new value that the user has set in Settings
     * @return The integer value that is associated with the given string
     */
    private static int frequencyToDays(String pref) {
        switch (pref) {
            case "weekly":
                return DAYS_IN_A_WEEK;
            case "biweekly":
                return DAYS_IN_TWO_WEEKS;
            case "monthly":
                return DAYS_IN_A_MONTH;
            case "bimonthly":
                return DAYS_IN_TWO_MONTHS;
            default:
                return -1;
        }
    }
}
