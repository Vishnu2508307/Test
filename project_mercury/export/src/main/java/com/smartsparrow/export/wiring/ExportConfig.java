package com.smartsparrow.export.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ExportConfig {

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

    @JsonProperty("snippetsStorage")
    private SnippetsStorage snippetsStorage;


    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public ExportConfig setSubmitTopicNameOrArn(final String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getResultQueueName() {
        return resultQueueName;
    }

    public ExportConfig setResultQueueName(String resultQueueName) {
        this.resultQueueName = resultQueueName;
        return this;
    }

    public String getErrorQueueName() {
        return errorQueueName;
    }

    public ExportConfig setErrorQueueName(String errorQueueName) {
        this.errorQueueName = errorQueueName;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public ExportConfig setDelayQueueNameOrArn(final String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public ExportConfig setBucketName(final String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getSnippetBucketName() {
        return snippetBucketName;
    }

    public ExportConfig setSnippetBucketName(String snippetBucketName) {
        this.snippetBucketName = snippetBucketName;
        return this;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public ExportConfig setBucketUrl(final String bucketUrl) {
        this.bucketUrl = bucketUrl;
        return this;
    }

    public Integer getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public ExportConfig setConcurrentConsumers(Integer concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
        return this;
    }

    public Integer getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public ExportConfig setMaxMessagesPerPoll(Integer maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
        return this;
    }

    public Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public ExportConfig setWaitTimeSeconds(Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
        return this;
    }

    public String getSubmitExportFailureTopic() {
        return submitExportFailureTopic;
    }

    public ExportConfig setSubmitExportFailureTopic(final String submitExportFailureTopic) {
        this.submitExportFailureTopic = submitExportFailureTopic;
        return this;
    }

    public SnippetsStorage getSnippetsStorage() {
        return snippetsStorage;
    }

    public ExportConfig setSnippetsStorage(SnippetsStorage snippetsStorage) {
        this.snippetsStorage = snippetsStorage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportConfig that = (ExportConfig) o;

        if (submitTopicNameOrArn != null ? !submitTopicNameOrArn.equals(that.submitTopicNameOrArn) : that.submitTopicNameOrArn != null)
            return false;
        if (resultQueueName != null ? !resultQueueName.equals(that.resultQueueName) : that.resultQueueName != null)
            return false;
        if (errorQueueName != null ? !errorQueueName.equals(that.errorQueueName) : that.errorQueueName != null)
            return false;
        if (delayQueueNameOrArn != null ? !delayQueueNameOrArn.equals(that.delayQueueNameOrArn) : that.delayQueueNameOrArn != null)
            return false;
        if (bucketName != null ? !bucketName.equals(that.bucketName) : that.bucketName != null) return false;
        if (snippetBucketName != null ? !snippetBucketName.equals(that.snippetBucketName) : that.snippetBucketName != null)
            return false;
        if (bucketUrl != null ? !bucketUrl.equals(that.bucketUrl) : that.bucketUrl != null) return false;
        if (concurrentConsumers != null ? !concurrentConsumers.equals(that.concurrentConsumers) : that.concurrentConsumers != null)
            return false;
        if (maxMessagesPerPoll != null ? !maxMessagesPerPoll.equals(that.maxMessagesPerPoll) : that.maxMessagesPerPoll != null)
            return false;
        if (waitTimeSeconds != null ? !waitTimeSeconds.equals(that.waitTimeSeconds) : that.waitTimeSeconds != null)
            return false;
        if (submitExportFailureTopic != null ? !submitExportFailureTopic.equals(that.submitExportFailureTopic) : that.submitExportFailureTopic != null)
            return false;
        return snippetsStorage == that.snippetsStorage;
    }

    @Override
    public int hashCode() {
        int result = submitTopicNameOrArn != null ? submitTopicNameOrArn.hashCode() : 0;
        result = 31 * result + (resultQueueName != null ? resultQueueName.hashCode() : 0);
        result = 31 * result + (errorQueueName != null ? errorQueueName.hashCode() : 0);
        result = 31 * result + (delayQueueNameOrArn != null ? delayQueueNameOrArn.hashCode() : 0);
        result = 31 * result + (bucketName != null ? bucketName.hashCode() : 0);
        result = 31 * result + (snippetBucketName != null ? snippetBucketName.hashCode() : 0);
        result = 31 * result + (bucketUrl != null ? bucketUrl.hashCode() : 0);
        result = 31 * result + (concurrentConsumers != null ? concurrentConsumers.hashCode() : 0);
        result = 31 * result + (maxMessagesPerPoll != null ? maxMessagesPerPoll.hashCode() : 0);
        result = 31 * result + (waitTimeSeconds != null ? waitTimeSeconds.hashCode() : 0);
        result = 31 * result + (submitExportFailureTopic != null ? submitExportFailureTopic.hashCode() : 0);
        result = 31 * result + (snippetsStorage != null ? snippetsStorage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExportConfig{" +
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
                ", snippetsStorage=" + snippetsStorage +
                '}';
    }
}
