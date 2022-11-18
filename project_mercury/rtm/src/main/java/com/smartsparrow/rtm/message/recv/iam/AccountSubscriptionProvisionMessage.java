package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AccountSubscriptionProvisionMessage extends AccountProvisionMessage {

    private Set<String> roles;

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountSubscriptionProvisionMessage that = (AccountSubscriptionProvisionMessage) o;
        return Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), roles);
    }
}
