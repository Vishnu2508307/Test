package com.smartsparrow.data;

import java.util.Objects;

/**
 * Unique identifier for a server instance
 */
public class ServerIdentifier {

    private String serverId;

    public String getServerId() {
        return serverId;
    }

    public ServerIdentifier setServerId(final String serverId) {
        this.serverId = serverId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerIdentifier that = (ServerIdentifier) o;
        return Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId);
    }

    @Override
    public String toString() {
        return "ServerIdentifier{" +
                "serverId=" + serverId +
                '}';
    }
}
