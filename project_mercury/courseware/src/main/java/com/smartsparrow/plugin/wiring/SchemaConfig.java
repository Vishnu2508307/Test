package com.smartsparrow.plugin.wiring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaConfig {

    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("prefix")
    private String prefix;

    public String getBucketName() {
        return bucketName;
    }

    public SchemaConfig setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public SchemaConfig setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @JsonIgnore
    public String getBucketUrl() {
        return String.format("%s/%s", bucketName, prefix);
    }

    @Override
    public String toString() {
        return "SchemaConfig{" +
                "bucketName='" + bucketName + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
