package com.smartsparrow.util.log.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonLog {

    private long relative;
    private long timestamp;
    private String dateTime;
    private ThreadLog threadLog;
    private String level;
    private String logger;
    private EventLog event;

    public long getRelative() {
        return relative;
    }

    public JsonLog setRelative(long relative) {
        this.relative = relative;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JsonLog setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.dateTime = DateFormat.asRFC1123(timestamp);
        return this;
    }

    public String getDateTime() {
        return dateTime;
    }

    public ThreadLog getThreadLog() {
        return threadLog;
    }

    public JsonLog setThreadLog(ThreadLog threadLog) {
        this.threadLog = threadLog;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public JsonLog setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getLogger() {
        return logger;
    }

    public JsonLog setLogger(String logger) {
        this.logger = logger;
        return this;
    }

    public EventLog getEvent() {
        return event;
    }

    public JsonLog setEvent(EventLog event) {
        this.event = event;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonLog jsonLog = (JsonLog) o;
        return relative == jsonLog.relative &&
                timestamp == jsonLog.timestamp &&
                Objects.equals(dateTime, jsonLog.dateTime) &&
                Objects.equals(threadLog, jsonLog.threadLog) &&
                Objects.equals(level, jsonLog.level) &&
                Objects.equals(logger, jsonLog.logger) &&
                Objects.equals(event, jsonLog.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relative, timestamp, dateTime, threadLog, level, logger, event);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
