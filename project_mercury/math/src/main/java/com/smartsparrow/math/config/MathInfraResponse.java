package com.smartsparrow.math.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MathInfraResponse {

    @JsonProperty("mathMLUri")
    private String mathMLUri;
    @JsonProperty("mathMLPath")
    private String mathMLPath;
    @JsonProperty("metrics")
    private Boolean metrics;
    @JsonProperty("centerbaseline")
    private Boolean centerbaseline;
    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("submitTopicNameOrArn")
    private String submitTopicNameOrArn;
    @JsonProperty("delayQueueNameOrArn")
    private String delayQueueNameOrArn;

    public String getMathMLUri() {
        return mathMLUri;
    }

    public MathInfraResponse setMathMLUri(final String mathMLUri) {
        this.mathMLUri = mathMLUri;
        return this;
    }

    public String getMathMLPath() {
        return mathMLPath;
    }

    public MathInfraResponse setMathMLPath(final String mathMLPath) {
        this.mathMLPath = mathMLPath;
        return this;
    }

    public Boolean getMetrics() {
        return metrics;
    }

    public MathInfraResponse setMetrics(final Boolean metrics) {
        this.metrics = metrics;
        return this;
    }

    public Boolean getCenterbaseline() {
        return centerbaseline;
    }

    public MathInfraResponse setCenterbaseline(final Boolean centerbaseline) {
        this.centerbaseline = centerbaseline;
        return this;
    }

    /**
     * Accessor to retrieve the s3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    public MathInfraResponse setBucketName(final String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public MathInfraResponse setSubmitTopicNameOrArn(final String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public MathInfraResponse setDelayQueueNameOrArn(final String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathInfraResponse that = (MathInfraResponse) o;
        return Objects.equals(mathMLUri, that.mathMLUri) &&
                Objects.equals(mathMLPath, that.mathMLPath) &&
                Objects.equals(metrics, that.metrics) &&
                Objects.equals(centerbaseline, that.centerbaseline) &&
                Objects.equals(bucketName, that.bucketName) &&
                Objects.equals(submitTopicNameOrArn, that.submitTopicNameOrArn) &&
                Objects.equals(delayQueueNameOrArn, that.delayQueueNameOrArn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mathMLUri,
                            mathMLPath,
                            metrics,
                            centerbaseline,
                            bucketName,
                            submitTopicNameOrArn,
                            delayQueueNameOrArn);
    }

    @Override
    public String toString() {
        return "MathInfraResponse{" +
                "mathMLUri='" + mathMLUri + '\'' +
                ", mathMLPath='" + mathMLPath + '\'' +
                ", metrics=" + metrics +
                ", centerbaseline=" + centerbaseline +
                ", bucketName='" + bucketName + '\'' +
                ", submitTopicNameOrArn='" + submitTopicNameOrArn + '\'' +
                ", delayQueueNameOrArn='" + delayQueueNameOrArn + '\'' +
                '}';
    }
}
