package com.smartsparrow.rtm.message.recv.asset;

import java.util.Map;
import java.util.Objects;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateAssetMessage extends ReceivedMessage {

    private String url;
    private AssetMediaType mediaType;
    private AssetVisibility assetVisibility;
    private Map<String, String> metadata;
    private AssetProvider assetProvider;

    public String getUrl() {
        return url;
    }

    public AssetMediaType getMediaType() {
        return mediaType;
    }

    public AssetVisibility getAssetVisibility() {
        return assetVisibility;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AssetProvider getAssetProvider() {
        return assetProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAssetMessage that = (CreateAssetMessage) o;
        return Objects.equals(url, that.url) &&
                mediaType == that.mediaType &&
                assetVisibility == that.assetVisibility &&
                Objects.equals(metadata, that.metadata) &&
                assetProvider == that.assetProvider;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, mediaType, assetVisibility, metadata, assetProvider);
    }

    @Override
    public String toString() {
        return "CreateAssetMessage{" +
                "url='" + url + '\'' +
                ", mediaType=" + mediaType +
                ", assetVisibility=" + assetVisibility +
                ", metadata=" + metadata +
                ", assetProvider=" + assetProvider +
                "} " + super.toString();
    }
}
