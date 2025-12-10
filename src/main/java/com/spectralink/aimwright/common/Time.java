package com.spectralink.aimwright.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Date and time utility methods for the Aimwright framework.
 */
public class Time {

    /**
     * Returns the current time in milliseconds since epoch.
     */
    public static long getCurrentMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the start of today in milliseconds since epoch.
     */
    public static long getTodayBeginningMillis() {
        LocalDateTime unzonedTodayStart = LocalDate.now().atStartOfDay();
        ZonedDateTime startOfToday = unzonedTodayStart.atZone(TimeZone.getDefault().toZoneId());
        return startOfToday.toInstant().toEpochMilli();
    }

    /**
     * Returns the start of a past day in milliseconds since epoch.
     *
     * @param daySpan Number of days in the past (1 = yesterday)
     */
    public static long getPastDayBeginningMillis(int daySpan) {
        LocalDateTime unzonedTodayStart = LocalDate.now().atStartOfDay();
        LocalDateTime unzonedPreviousDayStart = unzonedTodayStart.minusDays(daySpan);
        ZonedDateTime startOfPreviousDay = unzonedPreviousDayStart.atZone(TimeZone.getDefault().toZoneId());
        return startOfPreviousDay.toInstant().toEpochMilli();
    }

    /**
     * Returns today's date as a formatted string.
     *
     * @return Date in "MMM dd yyyy" format (e.g., "Dec 09 2025")
     */
    public static String getTodayLiteralDate() {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MMM dd yyyy");
        return pattern.format(LocalDate.now());
    }

    /**
     * Returns yesterday's date as a formatted string.
     *
     * @return Date in "MMM dd yyyy" format
     */
    public static String getYesterdayLiteralDate() {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MMM dd yyyy");
        return pattern.format(LocalDate.now().minusDays(1));
    }

    /**
     * Converts milliseconds since epoch to a formatted date/time string.
     *
     * @param milliSeconds Timestamp in milliseconds
     * @return Date/time in "MMM dd yyyy HH:mm" format
     */
    public static String getLiteralDateAndTime(Long milliSeconds) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        Instant instant = Instant.ofEpochMilli(milliSeconds);
        return pattern.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Returns a date N days ago as a formatted string.
     *
     * @param daysAgo Number of days in the past
     * @return Date in "MMM dd yyyy" format
     */
    public static String getPastLiteralDate(int daysAgo) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MMM dd yyyy");
        return pattern.format(LocalDate.now().minusDays(daysAgo));
    }
}
