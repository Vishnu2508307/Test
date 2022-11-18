package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class MathAssetErrorNotification {

    private UUID notificationId;
    private String errorMessage;
    private String cause;
    private UUID assetId;

    public UUID getNotificationId() {
        return notificationId;
    }

    public MathAssetErrorNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public MathAssetErrorNotification setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public MathAssetErrorNotification setCause(String cause) {
        this.cause = cause;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public MathAssetErrorNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetErrorNotification that = (MathAssetErrorNotification) o;
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
        return "MathAssetErrorNotification{" +
                "notificationId=" + notificationId +
                ", errorMessage='" + errorMessage + '\'' +
                ", cause='" + cause + '\'' +
                ", assetId=" + assetId +
                '}';
    }
}
