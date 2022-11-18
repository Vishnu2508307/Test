package com.smartsparrow.plugin.lang;

import com.smartsparrow.exception.IllegalArgumentFault;

public class PluginSchemaParserFault extends IllegalArgumentFault {

    /**
     * Construct a fault if plugin schema can not be parsed.
     *
     * @param message the message to return to the client
     */
    public PluginSchemaParserFault(String message) {
        super(message);
    }
}
