package com.smartsparrow.export.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExportInfraResponse {

    @JsonProperty("submitTopicNameOrArn")
    private String submitTopicNameOrArn;

    @JsonProperty("resultQueueName")
    private String resultQueueName;

    @JsonProperty("errorQueueName")
    private String errorQueueName;

    @JsonProperty("delayQueueNameOrArn")
    private String delayQueueNameOrArn;

    @JsonProperty("bucketName")
    private String bucketName;

    @JsonProperty("snippetBucketName")
    private String snippetBucketName;

    @JsonProperty("bucketUrl")
    private String bucketUrl;

    @JsonProperty("concurrentConsumers")
    private Integer concurrentConsumers;

    @JsonProperty("maxMessagesPerPoll")
    private Integer maxMessagesPerPoll;

    @JsonProperty("waitTimeSeconds")
    private Integer waitTimeSeconds;

    @JsonProperty("submitExportFailureTopic")
    private String submitExportFailureTopic;

    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public ExportInfraResponse setSubmitTopicNameOrArn(final String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getResultQueueName() {
        return resultQueueName;
    }

    public ExportInfraResponse setResultQueueName(String resultQueueName) {
        this.resultQueueName = resultQueueName;
        return this;
    }

    public String getErrorQueueName() {
        return errorQueueName;
    }

    public ExportInfraResponse setErrorQueueName(String errorQueueName) {
        this.errorQueueName = errorQueueName;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public ExportInfraResponse setDelayQueueNameOrArn(final String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public ExportInfraResponse setBucketName(final String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getSnippetBucketName() {
        return snippetBucketName;
    }

    public ExportInfraResponse setSnippetBucketName(String snippetBucketName) {
        this.snippetBucketName = snippetBucketName;
        return this;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public ExportInfraResponse setBucketUrl(final String bucketUrl) {
        this.bucketUrl = bucketUrl;
        return this;
    }

    public Integer getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public ExportInfraResponse setConcurrentConsumers(Integer concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
        return this;
    }

    public Integer getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public ExportInfraResponse setMaxMessagesPerPoll(Integer maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
        return this;
    }

    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public ExportInfraResponse setWaitTimeSeconds(Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
        return this;
    }

    public String getSubmitExportFailureTopic() {
        return submitExportFailureTopic;
    }

    public ExportInfraResponse setSubmitExportFailureTopic(final String submitExportFailureTopic) {
        this.submitExportFailureTopic = submitExportFailureTopic;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportInfraResponse that = (ExportInfraResponse) o;
        return Objects.equals(submitTopicNameOrArn, that.submitTopicNameOrArn)
                && Objects.equals(resultQueueName, that.resultQueueName)
                && Objects.equals(errorQueueName, that.errorQueueName)
                && Objects.equals(delayQueueNameOrArn, that.delayQueueNameOrArn)
                && Objects.equals(bucketName, that.bucketName)
                && Objects.equals(snippetBucketName, that.snippetBucketName)
                && Objects.equals(bucketUrl, that.bucketUrl)
                && Objects.equals(concurrentConsumers, that.concurrentConsumers)
                && Objects.equals(maxMessagesPerPoll, that.maxMessagesPerPoll)
                && Objects.equals(submitExportFailureTopic, that.submitExportFailureTopic)
                && Objects.equals(waitTimeSeconds, that.waitTimeSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submitTopicNameOrArn,
                resultQueueName,
                errorQueueName,
                delayQueueNameOrArn,
                bucketName,
                snippetBucketName,
                bucketUrl,
                concurrentConsumers,
                maxMessagesPerPoll,
                submitExportFailureTopic,
                waitTimeSeconds);
    }

    @Override
    public String toString() {
        return "ExportInfraResponse{" +
                "submitTopicNameOrArn='" + submitTopicNameOrArn + '\'' +
                ", resultQueueName='" + resultQueueName + '\'' +
                ", errorQueueName='" + errorQueueName + '\'' +
                ", delayQueueNameOrArn='" + delayQueueNameOrArn + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", snippetBucketName='" + snippetBucketName + '\'' +
                ", bucketUrl='" + bucketUrl + '\'' +
                ", concurrentConsumers=" + concurrentConsumers +
                ", maxMessagesPerPoll=" + maxMessagesPerPoll +
                ", waitTimeSeconds=" + waitTimeSeconds +
                ", submitExportFailureTopic='" + submitExportFailureTopic + '\'' +
                '}';
    }
}
