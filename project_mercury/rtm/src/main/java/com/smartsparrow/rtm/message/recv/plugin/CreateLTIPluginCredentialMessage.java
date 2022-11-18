package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateLTIPluginCredentialMessage extends ReceivedMessage {

    private String key;
    private String secret;
    private UUID pluginId;
    private Set<String> whiteListedFields;

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public Set<String> getWhiteListedFields() {
        return whiteListedFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateLTIPluginCredentialMessage that = (CreateLTIPluginCredentialMessage) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(whiteListedFields, that.whiteListedFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, secret, pluginId, whiteListedFields);
    }

    @Override
    public String toString() {
        return "CreateLTIPluginCredentialMessage{" +
                "key='" + key + '\'' +
                ", secret='" + "****" + '\'' +
                ", pluginId=" + pluginId +
                ", whiteListedFields=" + whiteListedFields +
                '}';
    }
}
