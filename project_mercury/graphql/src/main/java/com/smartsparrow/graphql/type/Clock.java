package com.smartsparrow.graphql.type;

import java.time.format.DateTimeFormatter;

import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;

public class Clock {

    private java.time.Clock clock = java.time.Clock.systemUTC();

    public Clock() {
    }

    @GraphQLQuery(name = "epochSeconds", description = "The clock time in seconds since the epoch, aka unix time")
    public Long epochSeconds() {
        return (long) Math.floor(clock.millis() / 1000.0);
    }

    @GraphQLQuery(name = "epochMillis", description = "The clock time in millis since the epoch")
    public Long epochMillis() {
        return clock.millis();
    }

    @GraphQLQuery(name = "rfc1123", description = "The clock time in RFC 1123 format")
    public String asRFC1123() {
        return DateFormat.asRFC1123(clock.millis());
    }

    @GraphQLQuery(name = "format", description = "get a clock and specify the format")
    public String getFormattedClock(@GraphQLArgument(name = "pattern") @GraphQLNonNull String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.withZone(DateFormat.UTC).format(clock.instant());
    }
}
