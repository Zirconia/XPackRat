package com.example.android.x_packrat.utilities;

import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Contains methods for getting information about the user's local date and time and for formatting
 * date and time values.
 */
public class XPackRatDateUtils {

    /**
     * Formats a locale and 3 ints into a date string in the following format: Feb 16 2018.
     *
     * @param locale The user's locale
     * @param year   The year
     * @param month  The month
     * @param day    The day
     * @return The formatted date
     */
    public static String formatDate(Locale locale, int year, int month, int day) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day);
        Date date = cal.getTime();

        // Checks whether or not the day is a single or double digit number and selects the
        // appropriate pattern(lets us avoid having dates like Feb 07 2018)
        String pattern;
        if (day / 10 == 0) {
            pattern = "MMM d yyyy";
        } else {
            pattern = "MMM dd yyyy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

        return sdf.format(date);
    }

    /**
     * Formats a locale and 2 ints into a time string in the following format: 7:15pm.
     *
     * @param locale The user's locale
     * @param minute The minute
     * @param hour   The hour
     * @return The formatted time
     */
    public static String formatTime(Locale locale, int minute, int hour) {

        // Used to check whether or not the fetched time should be treated as AM or PM.
        // This is necessary since the fetched time is for a 24hour clock, which does not
        // have AM or PM and we want to convert to a time for a 12hour clock.
        int moddedHour = hour % 12;

        return String.format(locale, "%2d:%02d %s", moddedHour == 0 ? 12 : moddedHour,
                minute, hour < 12 ? "am" : "pm");
    }

    /**
     * Gets the user's current locale. Uses deprecated code if device is using API < 24.
     *
     * @param context The context from which this method was called
     * @return The user's locale
     */
    public static Locale getUserLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        return locale;
    }

    /**
     * Gets the user's current time and date in milliseconds.
     *
     * @return The user's current time and date in milliseconds
     */
    public static long getCurrDatetime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Converts an interval of days to milliseconds.
     *
     * @param interval Number of days
     * @return An interval of days represented in milliseconds
     */
    public static long convertDaysToMilliseconds(String interval) {
        switch (interval) {
            case "weekly":
                return TimeUnit.DAYS.toMillis(7);
            case "biweekly":
                return TimeUnit.DAYS.toMillis(14);
            case "monthly":
                return TimeUnit.DAYS.toMillis(30);
            case "bimonthly":
                return TimeUnit.DAYS.toMillis(60);
            default:
                return 0;
        }
    }
}
