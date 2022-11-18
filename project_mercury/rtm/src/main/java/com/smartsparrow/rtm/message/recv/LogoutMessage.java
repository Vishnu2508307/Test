package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class LogoutMessage extends ReceivedMessage {

    private String bearerToken;

    public String getBearerToken() {
        return bearerToken;
    }

    public LogoutMessage setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogoutMessage that = (LogoutMessage) o;
        return Objects.equals(bearerToken, that.bearerToken);
    }

    @Override
    public int hashCode() {

        return Objects.hash(bearerToken);
    }

    @Override
    public String toString() {
        return "LogoutMessage{" +
                "bearerToken='" + bearerToken + '\'' +
                "} " + super.toString();
    }
}
