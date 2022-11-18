package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class ImageSource implements AssetSource {

    private UUID assetId;
    private ImageSourceName name;
    private String url;
    private Double width;
    private Double height;

    @Override
    public UUID getAssetId() {
        return assetId;
    }

    public ImageSource setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public ImageSourceName getName() {
        return name;
    }

    public ImageSource setName(ImageSourceName name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ImageSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public Double getWidth() {
        return width;
    }

    public ImageSource setWidth(Double width) {
        this.width = width;
        return this;
    }

    public Double getHeight() {
        return height;
    }

    public ImageSource setHeight(Double height) {
        this.height = height;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageSource that = (ImageSource) o;
        return Objects.equals(assetId, that.assetId) &&
                name == that.name &&
                Objects.equals(url, that.url) &&
                Objects.equals(width, that.width) &&
                Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, name, url, width, height);
    }

    @Override
    public String toString() {
        return "ImageSource{" +
                "assetId=" + assetId +
                ", name=" + name +
                ", url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
