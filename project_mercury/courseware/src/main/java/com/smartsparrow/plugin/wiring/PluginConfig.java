package com.smartsparrow.plugin.wiring;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginConfig {

    @JsonProperty("distribution.bucketName")
    private String distributionBucketName;
    @JsonProperty("repository.bucketName")
    private String repositoryBucketName;
    @JsonProperty("distribution.publicUrl")
    private String distributionPublicUrl;
    @JsonProperty("repository.publicUrl")
    private String repositoryPublicUrl;

    @JsonProperty("allowSync")
    private Boolean allowSync;

    public String getDistributionBucketName() {
        return distributionBucketName;
    }

    public PluginConfig setDistributionBucketName(String distributionBucketName) {
        this.distributionBucketName = distributionBucketName;
        return this;
    }

    public String getRepositoryBucketName() {
        return repositoryBucketName;
    }

    public PluginConfig setRepositoryBucketName(String repositoryBucketName) {
        this.repositoryBucketName = repositoryBucketName;
        return this;
    }

    public String getDistributionPublicUrl() {
        return distributionPublicUrl;
    }

    public PluginConfig setDistributionPublicUrl(String distributionPublicUrl) {
        this.distributionPublicUrl = distributionPublicUrl;
        return this;
    }

    public String getRepositoryPublicUrl() {
        return repositoryPublicUrl;
    }

    public PluginConfig setRepositoryPublicUrl(final String repositoryPublicUrl) {
        this.repositoryPublicUrl = repositoryPublicUrl;
        return this;
    }

    /**
     * Whether this deployment environment allows a user to "create" plugin versions and manifest data by syncing
     * plugins already present at the repository. This is a development feature and should be set to true in
     * production environments.
     *
     * @return true if syncing plugin data from plugin repo will be allowed.
     *
     */
    public Boolean getAllowSync() {
        return allowSync;
    }

    public PluginConfig setAllowSync(Boolean allowSync) {
        this.allowSync = allowSync;
        return this;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "distributionBucketName='" + distributionBucketName + '\'' +
                ", repositoryBucketName='" + repositoryBucketName + '\'' +
                ", distributionPublicUrl='" + distributionPublicUrl + '\'' +
                ", repositoryPublicUrl='" + repositoryPublicUrl + '\'' +
                '}';
    }
}
