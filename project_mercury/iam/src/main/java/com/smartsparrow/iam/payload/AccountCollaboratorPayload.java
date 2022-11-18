package com.smartsparrow.iam.payload;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.iam.service.PermissionLevel;

public class AccountCollaboratorPayload extends CollaboratorPayload {

    private AccountPayload accountPayload;

    @JsonProperty("account")
    public AccountPayload getAccountPayload() {
        return accountPayload;
    }

    public void setAccountPayload(AccountPayload accountPayload) {
        this.accountPayload = accountPayload;
    }

    public static AccountCollaboratorPayload from(@Nonnull AccountPayload accountPayload, PermissionLevel permissionLevel) {
        AccountCollaboratorPayload payload = new AccountCollaboratorPayload();
        payload.setAccountPayload(accountPayload);
        payload.setPermissionLevel(permissionLevel);
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountCollaboratorPayload that = (AccountCollaboratorPayload) o;
        return Objects.equals(accountPayload, that.accountPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountPayload);
    }

    @Override
    public String toString() {
        return "AccountCollaboratorPayload{" +
                "accountPayload=" + accountPayload +
                "} " + super.toString();
    }
}
