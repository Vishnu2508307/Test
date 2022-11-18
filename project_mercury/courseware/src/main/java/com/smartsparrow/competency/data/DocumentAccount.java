package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

public class DocumentAccount {

    private UUID documentId;
    private UUID accountId;

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentAccount setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DocumentAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentAccount that = (DocumentAccount) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, accountId);
    }

    @Override
    public String toString() {
        return "DocumentAccount{" +
                "documentId=" + documentId +
                ", accountId=" + accountId +
                '}';
    }
}
