package com.smartsparrow.ingestion.data;

public enum IngestionStatus {
    UPLOADING,
    ANALYZING,
    IMPORTING,
    FAILED,
    COMPLETED,
    UPLOADED,
    UPLOAD_FAILED,
    UPLOAD_CANCELLED,
    DELETED
}
