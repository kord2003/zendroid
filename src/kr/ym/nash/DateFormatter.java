package kr.ym.nash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    /*
     * Constructs a String for displaying full date and time: 2013-02-22 14:53:24
     */
    public static String fullDateAndTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return formatter.format(date);
    }

    /*
     * Constructs a String for displaying full date and time: 2013-02-22 14:53:24.666
     */
    public static String fullDateAndTimeMs(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        return formatter.format(date);
    }

    /*
     * Constructs a Date object from a String with format: 2013-02-22 14:53:24
     */
    public static Date dateFromFullDateString(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /*
     * Constructs a String for displaying full date and time for file: 2013-02-22_14-53-24
     */
    public static String fullDateForFile(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        return formatter.format(date);
    }

    /*
     * Constructs a Date object from a String with format: 2013-02-22_14-53-24
     */
    public static Date dateFromDateForFileString(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /*
     * Constructs a String for displaying full date: 2013-01-01
     */
    public static String fullDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return formatter.format(date);
    }


    /*
     * Constructs a Date object from a String with format: 2013-01-01
     */
    public static Date dateFromFullDate(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /*
     * Constructs a Date object from a String with format: 2013-02-22T14:53:24Z
     */
    public static Date dateFromCustomDateString(String string) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd", Locale.ENGLISH);
        return formatter.format(date);
    }

    public static String getDayOfWeek(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEEE", Locale.ENGLISH);
        return formatter.format(date);
    }
}
