package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AuthenticateMessage extends ReceivedMessage {

    // these fields
    private String email;
    private String password;

    // or this
    private String bearerToken;

    public AuthenticateMessage() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuthenticateMessage that = (AuthenticateMessage) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(
                bearerToken, that.bearerToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, bearerToken);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("email", "REDACTED")
                .add("password", "REDACTED")
                .add("bearerToken", bearerToken)
                .toString();
    }
}
