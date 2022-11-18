package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetAssetMessage extends ReceivedMessage {

    private String urn;

    public String getUrn() {
        return urn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAssetMessage that = (GetAssetMessage) o;
        return Objects.equals(urn, that.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn);
    }

    @Override
    public String toString() {
        return "GetAssetMessage{" +
                "urn='" + urn + '\'' +
                "} " + super.toString();
    }
}
