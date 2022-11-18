package com.smartsparrow.plugin.payload;

import java.util.Objects;
import java.util.UUID;

public class PluginCreatorPayload {

    private UUID accountId;
    private String email;

    public UUID getAccountId() {
        return accountId;
    }

    public PluginCreatorPayload setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PluginCreatorPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginCreatorPayload that = (PluginCreatorPayload) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, email);
    }
}
