package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class EditRoleMessage extends ReceivedMessage implements AccountMessage {

    private UUID accountId;
    private String role;

    public String getRole() {
        return role;
    }

    @Override
    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EditRoleMessage that = (EditRoleMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), accountId, role);
    }
}
