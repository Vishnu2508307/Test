package com.smartsparrow.plugin.lang;

public class S3BucketLoadFileException extends Exception {

    public S3BucketLoadFileException(String s) {
        super(s);
    }

    public S3BucketLoadFileException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
