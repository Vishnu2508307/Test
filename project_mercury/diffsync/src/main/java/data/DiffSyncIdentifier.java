package data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Describe the name of a diff sync stack
 */
public class DiffSyncIdentifier implements Serializable {
    private static final long serialVersionUID = -2014773865434251030L;

    private  DiffSyncIdentifierType type;
    private  String clientId;
    private  String serverId;

    public DiffSyncIdentifierType getType() {
        return type;
    }

    public DiffSyncIdentifier setType(final DiffSyncIdentifierType type) {
        this.type = type;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public DiffSyncIdentifier setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public DiffSyncIdentifier setServerId(final String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getUrn() {
        return String.format("%s:%s:%s", getType(), getClientId(), getServerId());
    }

    public boolean isSameClient(DiffSyncIdentifier identifier) {
        return this.clientId.equals(identifier.getClientId());
    }

    public boolean isSameServer(DiffSyncIdentifier identifier) {
        return this.serverId.equals(identifier.getServerId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncIdentifier that = (DiffSyncIdentifier) o;
        return  type == that.type && Objects.equals(clientId, that.clientId) && Objects.equals(serverId, that.serverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, clientId, serverId);
    }

    @Override
    public String toString() {
        return "DiffSyncIdentifier{" +
                "type=" + type +
                ", clientId='" + clientId + '\'' +
                ", serverId='" + serverId + '\'' +
                '}';
    }
}

