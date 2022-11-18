package com.smartsparrow.plugin.publish;

import com.smartsparrow.plugin.lang.PluginPublishException;

import java.io.IOException;

public interface PluginField<T,S> {

    T parse(PluginParserContext pluginParserContext) throws PluginPublishException, IOException;

}
