package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetRequestNotification {

    private static final long serialVersionUID = -6343232562622866398L;

    private UUID notificationId;
    private String url;
    private UUID assetId;
    private Double originalWidth;
    private Double originalHeight;
    private Double threshold;
    private String size;

    public UUID getNotificationId() {
        return notificationId;
    }

    public AssetRequestNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AssetRequestNotification setUrl(String url) {
        this.url = url;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetRequestNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public Double getOriginalWidth() {
        return originalWidth;
    }

    public AssetRequestNotification setOriginalWidth(Double originalWidth) {
        this.originalWidth = originalWidth;
        return this;
    }

    public Double getOriginalHeight() {
        return originalHeight;
    }

    public AssetRequestNotification setOriginalHeight(Double originalHeight) {
        this.originalHeight = originalHeight;
        return this;
    }

    public Double getThreshold() {
        return threshold;
    }

    public AssetRequestNotification setThreshold(Double threshold) {
        this.threshold = threshold;
        return this;
    }

    public String getSize() {
        return size;
    }

    public AssetRequestNotification setSize(String size) {
        this.size = size;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetRequestNotification that = (AssetRequestNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(url, that.url) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(originalWidth, that.originalWidth) &&
                Objects.equals(originalHeight, that.originalHeight) &&
                Objects.equals(threshold, that.threshold) &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, url, assetId, originalWidth, originalHeight, threshold, size);
    }

    @Override
    public String toString() {
        return "AssetRequestNotification{" +
                "notificationId=" + notificationId +
                ", url=" + url +
                ", assetId=" + assetId +
                ", originalWidth=" + originalWidth +
                ", originalHeight=" + originalHeight +
                ", threshold=" + threshold +
                ", size=" + size +
                '}';
    }
}
