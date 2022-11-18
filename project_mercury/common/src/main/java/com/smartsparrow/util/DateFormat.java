package com.smartsparrow.util;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;

public class DateFormat {

    private static final Logger log = LoggerFactory.getLogger(DateFormat.class);

    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MMM_YYYY = DateTimeFormatter.ofPattern("MMM yyyy");
    public static final DateTimeFormatter RFC_1123 = RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    /**
     * Format date as a year-month string.
     *
     * For example, 2015-03 (represents March 2015)
     *
     * @param when
     * @return
     */
    public static String asYearMonth(ZonedDateTime when) {
        return YYYY_MM.format(when);
    }

    /**
     * Format epoch millis as a year-month string.
     *
     * For example, 2015-03 (represents March 2015)
     *
     * @param millis
     * @return
     */
    public static String asYearMonth(long millis) {
        return asYearMonth(millis, UTC);
    }

    /**
     * Format date as a year-month string.
     *
     * For example, 2015-03 (represents March 2015)
     *
     * @param millis
     * @return
     */
    public static String asYearMonth(long millis, ZoneId zoneId) {
        // LocalDateTime is an un-zoned datatype, but factory still takes a TimeZone arg
        return asYearMonth(ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId));
    }

    /**
     * Format date as a year-month string.
     *
     * For example, 2015-03 (represents March 2015)
     *
     * @param instant
     * @return
     */
    public static String asYearMonth(Instant instant, ZoneId zoneId) {
        // LocalDateTime is an un-zoned datatype, but factory still takes a TimeZone arg
        return asYearMonth(ZonedDateTime.ofInstant(instant, zoneId));
    }

    /**
     * Format the provided year and month as a year date.
     *
     * For example, 2015-03 (represents March 2015)
     *
     * @param year the year to use.
     * @param month the month to use.
     *
     * @return
     */
    public static String asYearMonth(Integer year, Integer month) {
        return String.format("%04d-%02d", year, month);
    }

    /**
     * Format date as a year-month-day string.
     *
     * For example, 2015-03-15 represents the ides of march.
     *
     * @param when
     * @return
     */
    public static String asYearMonthDay(ZonedDateTime when) {
        return YYYY_MM_DD.format(when);
    }

    /**
     * Format epoch millis as year-month-day string.
     *
     * @param millis
     * @return
     */
    public static String asYearMonthDay(long millis) {
        return asYearMonthDay(millis, UTC);
    }

    /**
     * Format date as a year-month-day string.
     *
     * For example, 2015-03-15 represents the ides of march.
     *
     * @param millis
     * @return
     */
    public static String asYearMonthDay(long millis, ZoneId zoneId) {
        return asYearMonthDay(ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId));
    }

    /**
     * Format date as a year-month-day string.
     *
     * For example, 2015-03-15 represents the ides of march.
     *
     * @param instant
     * @return
     */
    public static String asYearMonthDay(Instant instant, ZoneId zoneId) {
        return asYearMonthDay(ZonedDateTime.ofInstant(instant, zoneId));
    }

    /**
     * Format the parameters as an ISO week YYYY-W## string.
     *
     * @param when the time.
     * @return
     */
    public static String asYearWeek(ZonedDateTime when) {
        int year = when.get(WeekFields.ISO.weekBasedYear());
        int week = when.get(WeekFields.ISO.weekOfWeekBasedYear());
        return asYearWeek(year, week);
    }

    /**
     * Format the parameters as a YYYY-W## string.
     *
     * @param year
     * @param week
     * @return
     */
    public static String asYearWeek(int year, int week) {
        return String.format("%04d-W%02d", year, week);
    }

    /**
     * Format the parameters as a MONTH YEAR (e.g. Jan 2017)
     *
     * @param month
     * @param year
     * @return
     */
    public static String asMonthYear(final int month, final int year) {
        ZonedDateTime t = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, UTC);
        return MMM_YYYY.format(t);
    }

    /**
     * Format the millis since epoch as an RFC 1123 formatted date.
     *
     * @param millis the millis since the epoch
     * @return a RFC 1123 formatted date
     */
    public static String asRFC1123(long millis) {
        return RFC_1123.format(Instant.ofEpochMilli(millis));
    }

    /**
     * Format the time-based UUID as an RFC 1123 formatted date.
     *
     * @param uuid the time based uuid
     * @return a RFC 1123 formatted date
     */
    public static String asRFC1123(UUID uuid) {
        return asRFC1123(UUIDs.unixTimestamp(uuid));
    }


    /**
     * Parse a RFC 1123 formatted date string as long.
     * @param time  a RFC 1123 formatted date
     * @return the millis since the epoch
     */
    public static long fromRFC1123(String time) {
        return RFC_1123.parse(time, Instant::from).toEpochMilli();
    }
}
