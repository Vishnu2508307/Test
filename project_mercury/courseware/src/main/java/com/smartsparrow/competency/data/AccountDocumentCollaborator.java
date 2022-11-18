package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.collaborator.AccountCollaborator;
import com.smartsparrow.iam.service.PermissionLevel;

public class AccountDocumentCollaborator extends DocumentCollaborator implements AccountCollaborator {

    private UUID accountId;

    public UUID getAccountId() {
        return accountId;
    }

    public AccountDocumentCollaborator setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public AccountDocumentCollaborator setDocumentId(UUID documentId) {
        super.setDocumentId(documentId);
        return this;
    }

    @Override
    public AccountDocumentCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountDocumentCollaborator that = (AccountDocumentCollaborator) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accountId);
    }

    @Override
    public String toString() {
        return "AccountDocumentCollaborator{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }
}
