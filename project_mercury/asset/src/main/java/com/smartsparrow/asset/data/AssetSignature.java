package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetSignature {

    private UUID id;
    private AssetSignatureStrategyType assetSignatureStrategyType;
    private String host;
    private String path;
    private String config;

    public UUID getId() {
        return id;
    }

    public AssetSignature setId(UUID id) {
        this.id = id;
        return this;
    }

    public AssetSignatureStrategyType getAssetSignatureStrategyType() {
        return assetSignatureStrategyType;
    }

    public AssetSignature setAssetSignatureStrategyType(AssetSignatureStrategyType assetSignatureStrategyType) {
        this.assetSignatureStrategyType = assetSignatureStrategyType;
        return this;
    }

    public String getHost() {
        return host;
    }

    public AssetSignature setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPath() {
        return path;
    }

    public AssetSignature setPath(String path) {
        this.path = path;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public AssetSignature setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetSignature that = (AssetSignature) o;
        return Objects.equals(id, that.id) &&
                assetSignatureStrategyType == that.assetSignatureStrategyType &&
                Objects.equals(host, that.host) &&
                Objects.equals(path, that.path) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetSignatureStrategyType, host, path, config);
    }

    @Override
    public String toString() {
        return "AssetSignature{" +
                "id=" + id +
                ", assetSignatureStrategyType=" + assetSignatureStrategyType +
                ", domain='" + host + '\'' +
                ", path='" + path + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
