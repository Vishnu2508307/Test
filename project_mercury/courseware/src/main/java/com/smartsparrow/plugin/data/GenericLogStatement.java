package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

/**
 * This POJO is used for generic log messages in Cassandra
 * */
public class GenericLogStatement {

    private UUID pluginId;
    private String version;
    private UUID bucketId;
    private PluginLogLevel level;
    private UUID id;
    private String message;
    private String args;
    private String pluginContext;
    private PluginLogContext loggingContext;

    public UUID getPluginId() {
        return pluginId;
    }

    public GenericLogStatement setPluginId(final UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public GenericLogStatement setVersion(final String version) {
        this.version = version;
        return this;
    }

    public UUID getBucketId() {
        return bucketId;
    }

    public GenericLogStatement setBucketId(final UUID bucketId) {
        this.bucketId = bucketId;
        return this;
    }

    public PluginLogLevel getLevel() {
        return level;
    }

    public GenericLogStatement setLevel(final PluginLogLevel level) {
        this.level = level;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public GenericLogStatement setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public GenericLogStatement setMessage(final String message) {
        this.message = message;
        return this;
    }

    public String getArgs() {
        return args;
    }

    public GenericLogStatement setArgs(final String args) {
        this.args = args;
        return this;
    }

    public String getPluginContext() {
        return pluginContext;
    }

    public GenericLogStatement setPluginContext(final String pluginContext) {
        this.pluginContext = pluginContext;
        return this;
    }

    public PluginLogContext getLoggingContext() {
        return loggingContext;
    }

    public GenericLogStatement setLoggingContext(final PluginLogContext loggingContext) {
        this.loggingContext = loggingContext;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericLogStatement that = (GenericLogStatement) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(bucketId, that.bucketId) &&
                Objects.equals(level, that.level) &&
                Objects.equals(id, that.id) &&
                Objects.equals(message, that.message) &&
                Objects.equals(args, that.args) &&
                Objects.equals(pluginContext, that.pluginContext) &&
                Objects.equals(loggingContext, that.loggingContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, version, bucketId, level, id, message, args, pluginContext, loggingContext);
    }

    @Override
    public String toString() {
        return "GenericLogStatement{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", bucketId=" + bucketId +
                ", level='" + level + '\'' +
                ", id=" + id +
                ", message='" + message + '\'' +
                ", args='" + args + '\'' +
                ", pluginContext='" + pluginContext + '\'' +
                ", loggingContext='" + loggingContext + '\'' +
                '}';
    }
}
