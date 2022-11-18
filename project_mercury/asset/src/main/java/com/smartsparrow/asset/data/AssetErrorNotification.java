package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetErrorNotification {

    private UUID notificationId;
    private String errorMessage;
    private String cause;
    private UUID assetId;

    public UUID getNotificationId() {
        return notificationId;
    }

    public AssetErrorNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public AssetErrorNotification setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public AssetErrorNotification setCause(String cause) {
        this.cause = cause;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetErrorNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetErrorNotification that = (AssetErrorNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(cause, that.cause) &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, errorMessage, cause, assetId);
    }

    @Override
    public String toString() {
        return "AssetErrorNotification{" +
                "notificationId=" + notificationId +
                ", errorMessage='" + errorMessage + '\'' +
                ", cause='" + cause + '\'' +
                ", assetId=" + assetId +
                '}';
    }
}
