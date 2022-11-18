package com.smartsparrow.config.service;

import java.util.Objects;

public class TestConfig {
    private String bucketName;
    private String bucketUrl;

    public String getBucketName() {
        return bucketName;
    }

    public TestConfig setBucketName(final String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public TestConfig setBucketUrl(final String bucketUrl) {
        this.bucketUrl = bucketUrl;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestConfig that = (TestConfig) o;
        return Objects.equals(bucketName, that.bucketName) &&
                Objects.equals(bucketUrl, that.bucketUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName, bucketUrl);
    }

    @Override
    public String toString() {
        return "TestConfig{" +
                "bucketName='" + bucketName + '\'' +
                ", bucketUrl='" + bucketUrl + '\'' +
                '}';
    }
}
