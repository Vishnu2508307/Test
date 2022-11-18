package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SetAccountPasswordMessage  extends ReceivedMessage implements AccountMessage {

    private UUID accountId;
    private String password;

    @Override
    public UUID getAccountId() {
        return accountId;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetAccountPasswordMessage that = (SetAccountPasswordMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, password);
    }

    @Override
    public String toString() {
        return "SetAccountPasswordMessage{" +
                "accountId=" + accountId +
                ", password='" + password + '\'' +
                "} " + super.toString();
    }
}
