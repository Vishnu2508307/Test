package com.smartsparrow.plugin.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginInfraResponse {

    @JsonProperty("distribution.bucketName")
    private String distributionBucketName;
    @JsonProperty("repository.bucketName")
    private String repositoryBucketName;
    @JsonProperty("distribution.publicUrl")
    private String distributionPublicUrl;
    @JsonProperty("repository.publicUrl")
    private String repositoryPublicUrl;

    public String getDistributionBucketName() {
        return distributionBucketName;
    }

    public PluginInfraResponse setDistributionBucketName(String distributionBucketName) {
        this.distributionBucketName = distributionBucketName;
        return this;
    }

    public String getRepositoryBucketName() {
        return repositoryBucketName;
    }

    public PluginInfraResponse setRepositoryBucketName(String repositoryBucketName) {
        this.repositoryBucketName = repositoryBucketName;
        return this;
    }

    public String getDistributionPublicUrl() {
        return distributionPublicUrl;
    }

    public PluginInfraResponse setDistributionPublicUrl(String distributionPublicUrl) {
        this.distributionPublicUrl = distributionPublicUrl;
        return this;
    }

    public String getRepositoryPublicUrl() {
        return repositoryPublicUrl;
    }

    public PluginInfraResponse setRepositoryPublicUrl(final String repositoryPublicUrl) {
        this.repositoryPublicUrl = repositoryPublicUrl;
        return this;
    }

    @Override
    public String toString() {
        return "PluginInfraResponse{" +
                "distributionBucketName='" + distributionBucketName + '\'' +
                ", repositoryBucketName='" + repositoryBucketName + '\'' +
                ", distributionPublicUrl='" + distributionPublicUrl + '\'' +
                ", repositoryPublicUrl='" + repositoryPublicUrl + '\'' +
                '}';
    }
}
