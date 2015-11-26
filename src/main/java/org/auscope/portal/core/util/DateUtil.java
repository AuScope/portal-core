package org.auscope.portal.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;

/**
 * Utility methods for date and time operations.
 * 
 * @author Richard Goh
 */
public class DateUtil {
    public static enum TimeField {
        DAY, HOUR, MINUTE, SECOND, MILLISECOND;
    }

    /**
     * Converts Date object to a date string of a given format.
     * 
     * @param date
     *            a Date object to be converted to string
     * @param pattern
     *            the pattern describing the date and time format
     * @return the formatted date-time string.
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * Calculates the absolute difference between two Date without regard for time offsets. The author of this method is Charles and it is taken from the
     * following site: http://technojeeves.com/joomla/index.php/free/71-difference-between-two-dates-in-java
     *
     * @param d1
     *            Date one
     * @param d2
     *            Date two
     * @param field
     *            The field we're interested in out of day, hour, minute, second, millisecond
     * @return The value of the required field.
     */
    public static long getTimeDifference(Date d1, Date d2, TimeField field) {
        return getTimeDifference(d1, d2)[field.ordinal()];
    }

    /**
     * Calculates the absolute difference between two Date without regard for time offsets. The author of this method is Charles and it is taken from the
     * following site: http://technojeeves.com/joomla/index.php/free/71-difference-between-two-dates-in-java
     * 
     * @param d1
     *            Date one
     * @param d2
     *            Date two
     * @return The fields day, hour, minute, second and millisecond.
     */
    public static long[] getTimeDifference(Date d1, Date d2) {
        long[] result = new long[5];
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(d1);

        long t1 = cal.getTimeInMillis();
        cal.setTime(d2);

        long diff = Math.abs(cal.getTimeInMillis() - t1);
        final int ONE_DAY = 1000 * 60 * 60 * 24;
        final int ONE_HOUR = ONE_DAY / 24;
        final int ONE_MINUTE = ONE_HOUR / 60;
        final int ONE_SECOND = ONE_MINUTE / 60;

        long d = diff / ONE_DAY;
        diff %= ONE_DAY;

        long h = diff / ONE_HOUR;
        diff %= ONE_HOUR;

        long m = diff / ONE_MINUTE;
        diff %= ONE_MINUTE;

        long s = diff / ONE_SECOND;
        long ms = diff % ONE_SECOND;

        result[0] = d;
        result[1] = h;
        result[2] = m;
        result[3] = s;
        result[4] = ms;

        return result;
    }

    /**
     * Gets a DateTime object using a formatted String.
     * 
     * @param dateString
     *            in format: 28/02/2013
     * @param endOfDay
     *            false means the time will be 00:00:00:000 true means the time will be 23:59:59:999
     * @return
     */
    public static DateTime stringToDateTime(String dateString, boolean endOfDay) {
        String[] date = dateString.split("/");
        return new DateTime(
                Integer.parseInt(date[2]), // year
                Integer.parseInt(date[1]), // monthOfYear
                Integer.parseInt(date[0]), // dayOfMonth
                endOfDay ? 23 : 0, // hourOfDay
                endOfDay ? 59 : 0, // minuteOfHour
                endOfDay ? 59 : 0, // secondOfMinute
                endOfDay ? 999 : 0); // millisOfSecond
    }

    /**
     * Gets a DateTime object using a year only. All other fields calculated based on whether it is the start or end of the year.
     * 
     * @param yearString
     *            A String containing a year in format: 2013
     * @param endOfYear
     *            false means the date will be Jan 1st and the time will be 00:00:00:000 true means the date will be Dec 31st and the time will be 23:59:59:999
     * @return
     */
    public static DateTime stringYearToDate(String yearString, boolean endOfYear) {
        return new DateTime(
                Integer.parseInt(yearString), // year
                endOfYear ? 12 : 1, // monthOfYear
                endOfYear ? 31 : 1, // dayOfMonth
                endOfYear ? 23 : 0, // hourOfDay
                endOfYear ? 59 : 0, // minuteOfHour
                endOfYear ? 59 : 0, // secondOfMinute
                endOfYear ? 999 : 0); // millisOfSecond
    }

}