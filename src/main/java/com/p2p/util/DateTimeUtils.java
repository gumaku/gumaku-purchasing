package com.p2p.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateTimeUtils {
    private DateTimeUtils() {}

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Hong_Kong");
    public static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(DEFAULT_ZONE_ID);

    public static ZonedDateTime now() {
        return ZonedDateTime.now(DEFAULT_ZONE_ID);
    }

    public static String format(ZonedDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }

    public static ZonedDateTime parse(String dateTimeStr) {
        return dateTimeStr != null ? 
            ZonedDateTime.parse(dateTimeStr, DEFAULT_FORMATTER.withZone(DEFAULT_ZONE_ID)) : null;
    }

    public static long minutesBetween(ZonedDateTime start, ZonedDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static boolean isExpired(ZonedDateTime deadline) {
        return deadline != null && deadline.isBefore(now());
    }

    public static boolean isWithinDays(ZonedDateTime dateTime, int days) {
        return dateTime != null && 
               dateTime.isAfter(now().minusDays(days)) && 
               dateTime.isBefore(now());
    }
} 