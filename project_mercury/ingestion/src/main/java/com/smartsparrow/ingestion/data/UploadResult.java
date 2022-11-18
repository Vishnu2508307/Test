package com.smartsparrow.ingestion.data;

import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class UploadResult {
    @JsonProperty("Records")
    private List<S3EventNotificationRecord> records;

    public List<S3EventNotificationRecord> getRecords() {
        return records;
    }

    public UploadResult setRecords(final List<S3EventNotificationRecord> records) {
        this.records = records;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadResult that = (UploadResult) o;
        return Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "records=" + records +
                '}';
    }
}
