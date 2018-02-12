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

public class XPackRatDateUtils {
    public static String formatDate(Locale locale, int year, int month, int day) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day);
        Date date = cal.getTime();
        String pattern;
        if(day / 10 == 0){
            pattern = "MMM d yyyy";
        }
        else{
            pattern = "MMM dd yyyy";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

        return sdf.format(date);
    }

    public static String formatTime(Locale locale, int minute, int hour){

        // Used to check whether or not the fetched time should be treated as AM or PM.
        // This is necessary since the fetched time is for a 24hour clock, which does not
        // have AM or PM and we want to convert to a time for a 12hour clock.
        int moddedHour = hour % 12;

        return String.format(locale,"%2d:%02d %s", moddedHour == 0 ? 12 : moddedHour,
                minute, hour < 12 ? "am" : "pm");
    }

    // Gets the user's current locale. Uses deprecated code if device is using API < 24.
    public static Locale getUserLocale(Context context){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        return locale;
    }

}
