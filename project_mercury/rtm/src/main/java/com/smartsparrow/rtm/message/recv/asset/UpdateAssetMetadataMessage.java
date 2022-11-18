package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateAssetMetadataMessage extends ReceivedMessage {

    private String assetUrn;
    private String key;
    private String value;

    public String getAssetUrn() {
        return assetUrn;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateAssetMetadataMessage that = (UpdateAssetMetadataMessage) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, key, value);
    }

    @Override
    public String toString() {
        return "UpdateAssetMetadataMessage{" +
                "assetUrn=" + assetUrn +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
