package com.smartsparrow.plugin.lang;

import java.util.UUID;

import com.smartsparrow.exception.IllegalArgumentFault;

public class PluginNotFoundFault extends IllegalArgumentFault {

    public PluginNotFoundFault(String message) {
        super(message);
    }

    public PluginNotFoundFault(UUID pluginId) {
        super(String.format("plugin %s not found", pluginId));
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
