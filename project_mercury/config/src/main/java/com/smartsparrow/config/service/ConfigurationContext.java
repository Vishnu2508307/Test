package com.smartsparrow.config.service;

import java.util.Objects;

public class ConfigurationContext<T> {

    private String fileName;
    private String key;
    private String env;
    private String region;
    private String prefix;

    public String getFileName() {
        return fileName;
    }

    public ConfigurationContext<T> setFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ConfigurationContext<T> setKey(final String key) {
        this.key = key;
        return this;
    }

    public String getEnv() {
        return env;
    }

    public ConfigurationContext<T> setEnv(final String env) {
        this.env = env;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public ConfigurationContext<T> setRegion(final String region) {
        this.region = region;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public ConfigurationContext<T> setPrefix(final String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationContext<?> that = (ConfigurationContext<?>) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(key, that.key) &&
                Objects.equals(env, that.env) &&
                Objects.equals(region, that.region) &&
                Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, key, env, region, prefix);
    }

    @Override
    public String toString() {
        return "ConfigurationContext{" +
                "fileName='" + fileName + '\'' +
                ", key='" + key + '\'' +
                ", env='" + env + '\'' +
                ", region='" + region + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
