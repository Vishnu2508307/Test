package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AccountShadowAttributeMessage extends ReceivedMessage {

    private UUID accountId;

    private String value;

    private AccountShadowAttributeName accountShadowAttributeName;

    public AccountShadowAttributeMessage() {
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getValue() {
        return value;
    }

    public AccountShadowAttributeName getAccountShadowAttributeName() {
        return accountShadowAttributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountShadowAttributeMessage that = (AccountShadowAttributeMessage) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(accountShadowAttributeName, that.accountShadowAttributeName) && Objects.equals( value, this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountShadowAttributeName);
    }

    @Override
    public String toString() {
        return "AccountShadowMessage{" +
                ", accountId=" + accountId +
                ", accountShadowAttributeName=" + accountShadowAttributeName +
                ", value='" + value + '\'' +
                '}';
    }
}
