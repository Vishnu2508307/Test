package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AudioSource implements AssetSource {

    private UUID assetId;
    private AudioSourceName name;
    private String url;

    @Override
    public UUID getAssetId() {
        return assetId;
    }

    public AudioSource setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public AudioSourceName getName() {
        return name;
    }

    public AudioSource setName(AudioSourceName name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AudioSource setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioSource that = (AudioSource) o;
        return Objects.equals(assetId, that.assetId) &&
                name == that.name &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, name, url);
    }

    @Override
    public String toString() {
        return "AudioSource{" +
                "assetId=" + assetId +
                ", name=" + name +
                ", url='" + url + '\'' +
                '}';
    }
}
