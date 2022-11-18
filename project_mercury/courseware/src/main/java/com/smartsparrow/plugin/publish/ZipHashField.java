package com.smartsparrow.plugin.publish;


import java.util.Map;

public class ZipHashField implements PluginField<String, Map<String, Object>> {

    @Override
    public String parse(PluginParserContext pluginParserContext) {
        return pluginParserContext.getHash();
    }
}
