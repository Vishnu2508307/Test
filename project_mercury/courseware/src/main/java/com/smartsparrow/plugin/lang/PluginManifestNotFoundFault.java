package com.smartsparrow.plugin.lang;

import java.util.UUID;

import com.smartsparrow.exception.IllegalArgumentFault;

public class PluginManifestNotFoundFault extends IllegalArgumentFault {

    public PluginManifestNotFoundFault(String message) {
        super(message);
    }

    public PluginManifestNotFoundFault(UUID pluginId) {
        super(String.format("plugin manifest %s not found", pluginId));
    }

    @Override
    public int getResponseStatusCode() {
        return 404;
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }

}
