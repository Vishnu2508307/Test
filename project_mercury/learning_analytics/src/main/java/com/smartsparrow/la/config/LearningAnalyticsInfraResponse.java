package com.smartsparrow.la.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LearningAnalyticsInfraResponse {

    @JsonProperty("adminBaseURL")
    private String adminBaseURL;

    @JsonProperty("appName")
    private String appName;

    @JsonProperty("originatingSystemCode")
    private String originatingSystemCode;

    @JsonProperty("statusBaseURL")
    private String statusBaseURL;

    @JsonProperty("publishBaseURL")
    private String publishBaseURL;

    @JsonProperty("schemaRegistryBaseURL")
    private String schemaRegistryBaseURL;

    @JsonProperty("autobahnEnvironment")
    private String autobahnEnvironment;

    public String getAdminBaseURL() {
        return adminBaseURL;
    }

    public LearningAnalyticsInfraResponse setAdminBaseURL(String adminBaseURL) {
        this.adminBaseURL = adminBaseURL;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public LearningAnalyticsInfraResponse setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getOriginatingSystemCode() {
        return originatingSystemCode;
    }

    public LearningAnalyticsInfraResponse setOriginatingSystemCode(String originatingSystemCode) {
        this.originatingSystemCode = originatingSystemCode;
        return this;
    }

    public String getStatusBaseURL() {
        return statusBaseURL;
    }

    public LearningAnalyticsInfraResponse setStatusBaseURL(String statusBaseURL) {
        this.statusBaseURL = statusBaseURL;
        return this;
    }

    public String getPublishBaseURL() {
        return publishBaseURL;
    }

    public LearningAnalyticsInfraResponse setPublishBaseURL(String publishBaseURL) {
        this.publishBaseURL = publishBaseURL;
        return this;
    }

    public String getSchemaRegistryBaseURL() {
        return schemaRegistryBaseURL;
    }

    public LearningAnalyticsInfraResponse setSchemaRegistryBaseURL(String schemaRegistryBaseURL) {
        this.schemaRegistryBaseURL = schemaRegistryBaseURL;
        return this;
    }

    public String getAutobahnEnvironment() {
        return autobahnEnvironment;
    }

    public LearningAnalyticsInfraResponse setAutobahnEnvironment(String autobahnEnvironment) {
        this.autobahnEnvironment = autobahnEnvironment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningAnalyticsInfraResponse that = (LearningAnalyticsInfraResponse) o;
        return Objects.equals(adminBaseURL, that.adminBaseURL) &&
                Objects.equals(appName, that.appName) &&
                Objects.equals(originatingSystemCode, that.originatingSystemCode) &&
                Objects.equals(statusBaseURL, that.statusBaseURL) &&
                Objects.equals(publishBaseURL, that.publishBaseURL) &&
                Objects.equals(schemaRegistryBaseURL, that.schemaRegistryBaseURL) &&
                Objects.equals(autobahnEnvironment, that.autobahnEnvironment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminBaseURL, appName, originatingSystemCode, statusBaseURL, publishBaseURL, schemaRegistryBaseURL, autobahnEnvironment);
    }

    @Override
    public String toString() {
        return "LearningAnalyticsInfraResponse{" +
                "adminBaseURL='" + adminBaseURL + '\'' +
                ", appName='" + appName + '\'' +
                ", originatingSystemCode='" + originatingSystemCode + '\'' +
                ", statusBaseURL='" + statusBaseURL + '\'' +
                ", publishBaseURL='" + publishBaseURL + '\'' +
                ", schemaRegistryBaseURL='" + schemaRegistryBaseURL + '\'' +
                ", autobahnEnvironment='" + autobahnEnvironment + '\'' +
                '}';
    }
}
