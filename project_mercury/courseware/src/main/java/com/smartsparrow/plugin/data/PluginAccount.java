package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

/**
 * This represents plugin visibility (accessibility) for account in workspace context
 */
public class PluginAccount {

    private UUID accountId;
    private UUID pluginId;

    public UUID getAccountId() {
        return accountId;
    }

    public PluginAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginAccount setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginAccount that = (PluginAccount) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountId, pluginId);
    }

    @Override
    public String toString() {
        return "PluginAccount{" +
                "accountId=" + accountId +
                ", pluginId=" + pluginId +
                '}';
    }
}
