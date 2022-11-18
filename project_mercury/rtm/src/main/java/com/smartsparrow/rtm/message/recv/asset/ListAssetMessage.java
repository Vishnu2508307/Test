package com.smartsparrow.rtm.message.recv.asset;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListAssetMessage extends ReceivedMessage {

    private List<String> assetUrns;
    private Integer limit;

    public List<String> getAssetUrns() {
        return assetUrns;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListAssetMessage that = (ListAssetMessage) o;
        return Objects.equals(assetUrns, that.assetUrns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrns);
    }

    @Override
    public String toString() {
        return "ListAssetMessage{" +
                "assetUrns=" + assetUrns +
                ", limit=" + limit +
                '}';
    }
}
