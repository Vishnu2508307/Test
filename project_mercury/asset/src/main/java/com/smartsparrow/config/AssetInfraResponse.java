package com.smartsparrow.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssetInfraResponse {
    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("publicUrl")
    private String publicUrl;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("submitTopicNameOrArn")
    private String submitTopicNameOrArn;
    @JsonProperty("delayQueueNameOrArn")
    private String delayQueueNameOrArn;
    @JsonProperty("alfrescoUrl")
    private String alfrescoUrl;
    @JsonProperty("alfrescoPushDelayTime")
    private Long alfrescoPushDelayTime;

    /**
     * Accessor to retrieve the s3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    public AssetInfraResponse setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    /**
     * Accessor to retrieve the s3 bucket public url
     */
    public String getPublicUrl() {
        return publicUrl;
    }

    public AssetInfraResponse setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
        return this;
    }

    /**
     * Accessor to get the s3 bucket prefix. The prefix indicates a subfolder in the bucket where the assets
     * will be saved
     */
    public String getPrefix() {
        return prefix;
    }

    public AssetInfraResponse setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getSubmitTopicNameOrArn() {
        return submitTopicNameOrArn;
    }

    public AssetInfraResponse setSubmitTopicNameOrArn(String submitTopicNameOrArn) {
        this.submitTopicNameOrArn = submitTopicNameOrArn;
        return this;
    }

    public String getDelayQueueNameOrArn() {
        return delayQueueNameOrArn;
    }

    public AssetInfraResponse setDelayQueueNameOrArn(String delayQueueNameOrArn) {
        this.delayQueueNameOrArn = delayQueueNameOrArn;
        return this;
    }

    public String getAlfrescoUrl() {
        return alfrescoUrl;
    }

    public AssetInfraResponse setAlfrescoUrl(String alfrescoUrl) {
        this.alfrescoUrl = alfrescoUrl;
        return this;
    }

    /**
     * Control Alfresco push rate in millisecond
     */

    public Long getAlfrescoPushDelayTime() {
        return alfrescoPushDelayTime;
    }

    public AssetInfraResponse setAlfrescoPushDelayTime(Long alfrescoPushDelayTime) {
        this.alfrescoPushDelayTime = alfrescoPushDelayTime;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetInfraResponse that = (AssetInfraResponse) o;
        return Objects.equals(bucketName, that.bucketName) &&
                Objects.equals(publicUrl, that.publicUrl) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(submitTopicNameOrArn, that.submitTopicNameOrArn) &&
                Objects.equals(delayQueueNameOrArn, that.delayQueueNameOrArn) &&
                Objects.equals(alfrescoUrl, that.alfrescoUrl) &&
                Objects.equals(alfrescoPushDelayTime, that.alfrescoPushDelayTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName,
                            publicUrl,
                            prefix,
                            submitTopicNameOrArn,
                            delayQueueNameOrArn,
                            alfrescoUrl,
                            alfrescoPushDelayTime);
    }

    @Override
    public String toString() {
        return "AssetInfraResponse{" +
                "bucketName='" + bucketName + '\'' +
                ", publicUrl='" + publicUrl + '\'' +
                ", prefix='" + prefix + '\'' +
                ", submitTopicNameOrArn='" + submitTopicNameOrArn + '\'' +
                ", delayQueueNameOrArn='" + delayQueueNameOrArn + '\'' +
                ", alfrescoUrl='" + alfrescoUrl + '\'' +
                ", alfrescoPushDelayTime=" + alfrescoPushDelayTime +
                '}';
    }
}
