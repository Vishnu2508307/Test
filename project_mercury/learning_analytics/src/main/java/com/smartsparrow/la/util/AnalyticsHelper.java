package com.smartsparrow.la.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AnalyticsHelper {

    public static String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    public static String generateTransactionDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return LocalDateTime.now().format(dateTimeFormatter);
    }
}
