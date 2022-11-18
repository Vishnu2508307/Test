package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AssetResultNotification {

    private static final long serialVersionUID = -6343232562622866398L;

    private UUID notificationId;
    private String url;
    private UUID assetId;
    private Double width;
    private Double height;
    private String size;

    public UUID getNotificationId() {
        return notificationId;
    }

    public AssetResultNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AssetResultNotification setUrl(String url) {
        this.url = url;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetResultNotification setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public Double getWidth() {
        return width;
    }

    public AssetResultNotification setWidth(Double width) {
        this.width = width;
        return this;
    }

    public Double getHeight() {
        return height;
    }

    public AssetResultNotification setHeight(Double height) {
        this.height = height;
        return this;
    }

    public String getSize() {
        return size;
    }

    public AssetResultNotification setSize(String size) {
        this.size = size;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetResultNotification that = (AssetResultNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(url, that.url) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(width, that.width) &&
                Objects.equals(height, that.height) &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, url, assetId, width, height, size);
    }

    @Override
    public String toString() {
        return "AssetRequestNotification{" +
                "notificationId=" + notificationId +
                ", url=" + url +
                ", assetId=" + assetId +
                ", width=" + width +
                ", height=" + height +
                ", size=" + size +
                '}';
    }
}
