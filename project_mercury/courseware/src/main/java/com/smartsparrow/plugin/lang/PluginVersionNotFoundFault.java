package com.smartsparrow.plugin.lang;

import java.util.UUID;

import com.smartsparrow.exception.IllegalArgumentFault;

public class PluginVersionNotFoundFault extends IllegalArgumentFault {

    public PluginVersionNotFoundFault(String message) {
        super(message);
    }

    public PluginVersionNotFoundFault(UUID pluginId, String version) {
        super(String.format("version %s for plugin %s not found", version, pluginId));
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
