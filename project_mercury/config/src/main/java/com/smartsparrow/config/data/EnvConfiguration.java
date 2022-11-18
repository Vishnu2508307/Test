package com.smartsparrow.config.data;

import java.util.Objects;

/**
 * This class represents environment configuration. Relates to cassandra table: config.env
 */
public class EnvConfiguration {

    private String region;
    private String key;
    private String value;

    public String getRegion() {
        return region;
    }

    public EnvConfiguration setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getKey() {
        return key;
    }

    public EnvConfiguration setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public EnvConfiguration setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvConfiguration that = (EnvConfiguration) o;
        return region == that.region &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, key, value);
    }

    @Override
    public String toString() {
        return "EnvConfiguration{" +
                "region=" + region +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
