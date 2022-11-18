package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AccountSubscriptionMigrateMessage extends ReceivedMessage implements AccountMessage {

    private UUID accountId;
    private Set<AccountRole> roles;

    @Override
    public UUID getAccountId() {
        return accountId;
    }

    public Set<AccountRole> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountSubscriptionMigrateMessage that = (AccountSubscriptionMigrateMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, roles);
    }

    @Override
    public String toString() {
        return "AccountSubscriptionMigrateMessage{" +
                "accountId=" + accountId +
                ", roles=" + roles +
                '}';
    }
}
