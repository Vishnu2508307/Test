package com.smartsparrow.plugin.publish;

import java.io.IOException;

public interface FallbackStrategy<T> {

    T apply(PluginParserContext pluginParserContext) throws IOException;
}
