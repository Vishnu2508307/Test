package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetStatusErrorByUrnId {

    private UUID id;
    private UUID assetId;
    private String assetUrn;
    private AssetStatus status;
    private String errorCause;
    private String errorMessage;

    public UUID getId() {
        return id;
    }

    public AssetStatusErrorByUrnId setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetStatusErrorByUrnId setAssetId(final UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public AssetStatusErrorByUrnId setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public AssetStatusErrorByUrnId setStatus(final AssetStatus status) {
        this.status = status;
        return this;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public AssetStatusErrorByUrnId setErrorCause(final String errorCause) {
        this.errorCause = errorCause;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public AssetStatusErrorByUrnId setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetStatusErrorByUrnId that = (AssetStatusErrorByUrnId) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(status, that.status) &&
                Objects.equals(errorCause, that.errorCause) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetId, assetUrn, status, errorCause, errorMessage);
    }

    @Override
    public String toString() {
        return "AssetStatusErrorByUrnId{" +
                "id=" + id +
                ", assetId=" + assetId +
                ", assetUrn='" + assetUrn + '\'' +
                ", status='" + status + '\'' +
                ", errorCause='" + errorCause + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
