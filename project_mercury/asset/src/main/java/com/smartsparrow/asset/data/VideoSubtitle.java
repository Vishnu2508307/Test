package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class VideoSubtitle {

    private UUID assetId;
    private String lang;
    private String url;

    public UUID getAssetId() {
        return assetId;
    }

    public VideoSubtitle setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public VideoSubtitle setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public VideoSubtitle setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoSubtitle that = (VideoSubtitle) o;
        return Objects.equals(assetId, that.assetId) &&
                Objects.equals(lang, that.lang) &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, lang, url);
    }

    @Override
    public String toString() {
        return "VideoSubtitle{" +
                "assetId=" + assetId +
                ", lang='" + lang + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
