package com.smartsparrow.plugin.publish;

import com.smartsparrow.plugin.data.PluginType;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class TypeField implements PluginField<PluginType, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;


    public TypeField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }


    @Override
    public PluginType parse(PluginParserContext pluginParserContext) {
        PluginType type = jsonObjectMap.get(fieldName) != null ? PluginType.valueOf(((String) jsonObjectMap.get(fieldName)).toUpperCase()) : null;
        checkArgument(type != null, "type missing from manifest");
        pluginParserContext.setPluginType(type);
        return type;
    }
}
