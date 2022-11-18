package com.smartsparrow.rtm.message.recv.math;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MathAssetGetMessage extends ReceivedMessage {
    private String urn;

    public String getUrn() {
        return urn;
    }

    public MathAssetGetMessage setUrn(final String urn) {
        this.urn = urn;
        return this;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetGetMessage that = (MathAssetGetMessage) o;
        return Objects.equals(urn, that.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn);
    }

    @Override
    public String toString() {
        return "MathAssetGetMessage{" +
                "urn='" + urn + '\'' +
                '}';
    }
}
