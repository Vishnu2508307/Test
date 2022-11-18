package com.smartsparrow.ingestion.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IngestionInfraResponse {

    @JsonProperty("adapterEpubQueueNameOrArn")
    private String adapterEpubQueueNameOrArn ;//= "ingestion-adapter-epub-submit";

    @JsonProperty("adapterDocxQueueNameOrArn")
    private String adapterDocxQueueNameOrArn ;//= "ingestion-adapter-docx-submit";

    @JsonProperty("ambrosiaIngestionQueueNameOrArn")
    private String ambrosiaIngestionQueueNameOrArn ;//= "ingestion-ambrosia-submit";

    @JsonProperty("ingestionCancelQueueNameOrArn")
    private String ingestionCancelQueueNameOrArn ;//= "ingestion-cancel-submit";

    @JsonProperty("bucketName")
    private String bucketName;

    @JsonProperty("bucketUrl")
    private String bucketUrl;

    public String getAdapterEpubQueueNameOrArn() {
        return adapterEpubQueueNameOrArn;
    }

    public String getAdapterDocxQueueNameOrArn() {
        return adapterDocxQueueNameOrArn;
    }

    public IngestionInfraResponse setAdapterEpubQueueNameOrArn(final String adapterEpubQueueNameOrArn) {
        this.adapterEpubQueueNameOrArn = adapterEpubQueueNameOrArn;
        return this;
    }

    public IngestionInfraResponse setAdapterDocxQueueNameOrArn(final String adapterDocxQueueNameOrArn) {
        this.adapterDocxQueueNameOrArn = adapterDocxQueueNameOrArn;
        return this;
    }

    public String getAmbrosiaIngestionQueueNameOrArn() {
        return ambrosiaIngestionQueueNameOrArn;
    }

    public IngestionInfraResponse setAmbrosiaIngestionQueueNameOrArn(final String ambrosiaIngestionQueueNameOrArn) {
        this.ambrosiaIngestionQueueNameOrArn = ambrosiaIngestionQueueNameOrArn;
        return this;
    }

    public String getIngestionCancelQueueNameOrArn() {
        return ingestionCancelQueueNameOrArn;
    }

    public IngestionInfraResponse setIngestionCancelQueueNameOrArn(final String ingestionCancelQueueNameOrArn) {
        this.ingestionCancelQueueNameOrArn = ingestionCancelQueueNameOrArn;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public IngestionInfraResponse setBucketName(final String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getBucketUrl() {
        return bucketUrl;
    }

    public IngestionInfraResponse setBucketUrl(final String bucketUrl) {
        this.bucketUrl = bucketUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionInfraResponse that = (IngestionInfraResponse) o;
        return Objects.equals(adapterEpubQueueNameOrArn, that.adapterEpubQueueNameOrArn) &&
                Objects.equals(adapterDocxQueueNameOrArn, that.adapterDocxQueueNameOrArn) &&
                Objects.equals(ambrosiaIngestionQueueNameOrArn, that.ambrosiaIngestionQueueNameOrArn) &&
                Objects.equals(ingestionCancelQueueNameOrArn, that.ingestionCancelQueueNameOrArn) &&
                Objects.equals(bucketName, that.bucketName) &&
                Objects.equals(bucketUrl, that.bucketUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adapterEpubQueueNameOrArn, adapterDocxQueueNameOrArn, ambrosiaIngestionQueueNameOrArn,
                ingestionCancelQueueNameOrArn, bucketName, bucketUrl);
    }

    @Override
    public String toString() {
        return "IngestionInfraResponse{" +
                "adapterEpubQueueNameOrArn='" + adapterEpubQueueNameOrArn + '\'' +
                "adapterDocxQueueNameOrArn='" + adapterDocxQueueNameOrArn + '\'' +
                ", ambrosiaIngestionQueueNameOrArn='" + ambrosiaIngestionQueueNameOrArn + '\'' +
                ", ingestionCancelQueueNameOrArn='" + ingestionCancelQueueNameOrArn + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", bucketUrl='" + bucketUrl + '\'' +
                '}';
    }
}
