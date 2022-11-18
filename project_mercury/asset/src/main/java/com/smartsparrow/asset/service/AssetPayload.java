package com.smartsparrow.asset.service;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.smartsparrow.asset.data.Asset;

public class AssetPayload {

    // the asset urn
    private String urn;
    // the asset object
    private Asset asset;
    // all the asset sources
    private Map<String, Object> source = Maps.newHashMap();
    // the asset metadata properties
    private Map<String, String> metadata = Maps.newHashMap();

    public Asset getAsset() {
        return asset;
    }

    public AssetPayload setAsset(Asset asset) {
        this.asset = asset;
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public AssetPayload setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AssetPayload putSource(String key, Object value) {
        source.put(key, value);
        return this;
    }

    public AssetPayload putAllSources(Map<String, Object> map) {
        source.putAll(map);
        return this;
    }

    public AssetPayload putMetadata(String key, String value) {
        metadata.put(key, value);
        return this;
    }

    public AssetPayload putAllMetadata(Map<String, String> map) {
        metadata.putAll(map);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetPayload that = (AssetPayload) o;
        return Objects.equals(urn, that.urn) &&
                Objects.equals(asset, that.asset) &&
                Objects.equals(source, that.source) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, asset, source, metadata);
    }

    @Override
    public String toString() {
        return "AssetPayload{" +
                "urn='" + urn + '\'' +
                ", asset=" + asset +
                ", source=" + source +
                ", metadata=" + metadata +
                '}';
    }
}
