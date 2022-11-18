package com.smartsparrow.plugin.lang;

public class S3BucketUploadException extends Exception {
    public S3BucketUploadException() {
        super();
    }

    public S3BucketUploadException(String message) {
        super(message);
    }

    public S3BucketUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
