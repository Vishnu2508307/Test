package com.smartsparrow.plugin.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaInfraResponse {

    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("prefix")
    private String prefix;

    public String getBucketName() {
        return bucketName;
    }

    public SchemaInfraResponse setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public SchemaInfraResponse setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @JsonIgnore
    public String getBucketUrl() {
        return String.format("%s/%s", bucketName, prefix);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaInfraResponse that = (SchemaInfraResponse) o;
        return Objects.equals(bucketName, that.bucketName) && Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName, prefix);
    }

    @Override
    public String toString() {
        return "SchemaInfraResponse{" +
                "bucketName='" + bucketName + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
