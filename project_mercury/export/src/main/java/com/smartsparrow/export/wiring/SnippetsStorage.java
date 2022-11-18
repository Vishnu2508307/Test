package com.smartsparrow.export.wiring;

/**
 * Informs coureseware export module to which type of storage to use.
 *
 * CASSANDRA - legacy, added for backwards compatibility while migrationt to newer REDIS storage.
 */
public enum SnippetsStorage {
    CASSANDRA,
    REDIS,
    S3
}
