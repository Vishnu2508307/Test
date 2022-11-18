package com.smartsparrow.iam.data.permission.competency;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class AccountDocumentPermission {

    private UUID accountId;
    private UUID documentId;
    private PermissionLevel permissionLevel;

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public AccountDocumentPermission setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public AccountDocumentPermission setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public AccountDocumentPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDocumentPermission that = (AccountDocumentPermission) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(documentId, that.documentId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, documentId, permissionLevel);
    }

    @Override
    public String toString() {
        return "AccountDocumentPermission{" +
                "accountId=" + accountId +
                ", documentId=" + documentId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
