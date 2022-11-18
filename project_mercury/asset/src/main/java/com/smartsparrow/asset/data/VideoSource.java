package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class VideoSource implements AssetSource {

    private UUID assetId;
    private VideoSourceName name;
    private String url;
    private String resolution;

    @Override
    public UUID getAssetId() {
        return assetId;
    }

    public VideoSource setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public VideoSourceName getName() {
        return name;
    }

    public VideoSource setName(VideoSourceName name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public VideoSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getResolution() {
        return resolution;
    }

    public VideoSource setResolution(String resolution) {
        this.resolution = resolution;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoSource that = (VideoSource) o;
        return Objects.equals(assetId, that.assetId) &&
                name == that.name &&
                Objects.equals(url, that.url) &&
                Objects.equals(resolution, that.resolution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, name, url, resolution);
    }

    @Override
    public String toString() {
        return "VideoSource{" +
                "assetId=" + assetId +
                ", name=" + name +
                ", url='" + url + '\'' +
                ", resolution='" + resolution + '\'' +
                '}';
    }
}
