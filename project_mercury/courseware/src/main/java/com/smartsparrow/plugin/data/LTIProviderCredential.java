package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class LTIProviderCredential {

    private UUID pluginId;
    private UUID id;
    private String key;
    private String secret;
    private Set<String> allowedFields;

    public LTIProviderCredential() {
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public LTIProviderCredential setPluginId(final UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public LTIProviderCredential setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return key;
    }

    public LTIProviderCredential setKey(final String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public LTIProviderCredential setSecret(final String secret) {
        this.secret = secret;
        return this;
    }

    public Set<String> getAllowedFields() {
        return allowedFields;
    }

    public LTIProviderCredential setAllowedFields(final Set<String> allowedFields) {
        this.allowedFields = allowedFields;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIProviderCredential that = (LTIProviderCredential) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(secret, that.secret) &&
                Objects.equals(allowedFields, that.allowedFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, id, key, secret, allowedFields);
    }

    @Override
    public String toString() {
        return "LTIProviderCredential{" +
                "pluginId=" + pluginId +
                ", id=" + id +
                ", key='" + key + '\'' +
                ", secret='" + secret + '\'' +
                ", allowedFields=" + allowedFields +
                '}';
    }
}
