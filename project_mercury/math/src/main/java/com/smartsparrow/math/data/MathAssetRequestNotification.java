package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class MathAssetRequestNotification {

    private static final long serialVersionUID = -6548999904168982346L;

    private UUID notificationId;
    private String mathML;
    private UUID assetId;

    public UUID getNotificationId() {
        return notificationId;
    }

    public MathAssetRequestNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getMathML() {
        return mathML;
    }

    public MathAssetRequestNotification setmathML(String mathML) {
        this.mathML = mathML;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public MathAssetRequestNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetRequestNotification that = (MathAssetRequestNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(mathML, that.mathML) &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, mathML, assetId);
    }

    @Override
    public String toString() {
        return "AssetRequestNotification{" +
                "notificationId=" + notificationId +
                ", mathML=" + mathML +
                ", assetId=" + assetId +
                '}';
    }
}
