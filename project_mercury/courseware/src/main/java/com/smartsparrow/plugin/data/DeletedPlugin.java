package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

public class DeletedPlugin {

    private UUID id;
    private UUID pluginId;
    private UUID accountId;

    public UUID getId() {
        return id;
    }

    public DeletedPlugin setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public DeletedPlugin setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public DeletedPlugin setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletedPlugin that = (DeletedPlugin) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, accountId);
    }

    @Override
    public String toString() {
        return "DeletedPlugin{" +
                "id=" + id +
                ", pluginId=" + pluginId +
                ", accountId=" + accountId +
                '}';
    }
}
