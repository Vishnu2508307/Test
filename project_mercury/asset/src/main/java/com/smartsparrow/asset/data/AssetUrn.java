package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AssetUrn {

    private final UUID assetId;
    private final AssetProvider assetProvider;

    public AssetUrn(String urn) {
        String[] parts = urn.split(":");
        this.assetId = UUID.fromString(parts[2]);
        this.assetProvider = AssetProvider.fromLabel(parts[1]);
    }

    public AssetUrn(UUID assetId, AssetProvider assetProvider){
        this.assetId = assetId;
        this.assetProvider = assetProvider;
    }


    public UUID getAssetId() {
        return assetId;
    }

    public AssetProvider getAssetProvider() {
        return assetProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetUrn assetUrn = (AssetUrn) o;
        return Objects.equals(assetId, assetUrn.assetId) &&
                assetProvider == assetUrn.assetProvider;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, assetProvider);
    }

    @Override
    public String toString() {
        return String.format("urn:%s:%s", this.assetProvider.getLabel(), this.assetId);
    }

}
