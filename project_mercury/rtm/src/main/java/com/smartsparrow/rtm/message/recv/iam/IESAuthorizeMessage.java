package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class IESAuthorizeMessage extends ReceivedMessage {

    private String pearsonUid;
    private String pearsonToken;

    public String getPearsonUid() {
        return pearsonUid;
    }

    public String getPearsonToken() {
        return pearsonToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IESAuthorizeMessage that = (IESAuthorizeMessage) o;
        return Objects.equals(pearsonUid, that.pearsonUid) &&
                Objects.equals(pearsonToken, that.pearsonToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pearsonUid, pearsonToken);
    }

    @Override
    public String toString() {
        return "IESAuthorizeMessage{" +
                "pearsonUid='" + pearsonUid + '\'' +
                ", pearsonToken='" + pearsonToken + '\'' +
                '}';
    }
}
