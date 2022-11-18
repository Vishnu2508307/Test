package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MyCloudAuthorizeMessage extends ReceivedMessage {

    private String myCloudToken;

    public String getMyCloudToken() {
        return myCloudToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyCloudAuthorizeMessage that = (MyCloudAuthorizeMessage) o;
        return Objects.equals(myCloudToken, that.myCloudToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myCloudToken);
    }

    @Override
    public String toString() {
        return "MyCloudAuthorizeMessage{" +
                "myCloudToken='" + myCloudToken + '\'' +
                '}';
    }
}
