package com.smartsparrow.plugin.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginFeatureResponse {

    @JsonProperty("allowSync")
    private Boolean allowSync;

    /**
     * Whether this deployment environment allows a user to "create" plugin versions and manifest data by syncing
     * plugins already present at the repository. This is a development feature and should be set to true in
     * production environments.
     *
     * @return true if syncing plugin data from plugin repo will be allowed.
     *
     */
    public Boolean getAllowSync() {
        return allowSync;
    }

    public PluginFeatureResponse setAllowSync(Boolean allowSync) {
        this.allowSync = allowSync;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginFeatureResponse that = (PluginFeatureResponse) o;
        return Objects.equals(allowSync, that.allowSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowSync);
    }

    @Override
    public String toString() {
        return "PluginFeatureResponse{" +
                "allowSync=" + allowSync +
                '}';
    }
}
