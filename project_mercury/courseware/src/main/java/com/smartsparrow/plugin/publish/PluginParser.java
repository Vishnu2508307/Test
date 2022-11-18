package com.smartsparrow.plugin.publish;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.data.PluginSearchableField;
import com.smartsparrow.plugin.lang.PluginPublishException;

public class PluginParser {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * @param pluginParserContext plugin parser context object
     * @return a {@link PluginParsedFields} plugin parsed fields
     * @throws IOException when the file reading operation fails
     * @throws PluginPublishException when something goes wrong while publishing the plugin
     */
    PluginParsedFields parse(PluginParserContext pluginParserContext,
                             PluginParserBuilder pluginParserBuilder) throws IOException, PluginPublishException {
        Map<String, Object> fields = new HashMap<>();
        PluginParsedFields pluginPublishFields = new PluginParsedFields();
        for (Map.Entry<String, PluginField> stringPluginFieldEntry : pluginParserBuilder.getFieldMap().entrySet()) {
            String fieldName = stringPluginFieldEntry.getKey();
            PluginField pluginField = stringPluginFieldEntry.getValue();

            Object value = pluginField.parse(pluginParserContext);

            if (pluginParserBuilder.getFallbackStrategies().get(fieldName) != null && value == null) {
                FallbackStrategy strategies = pluginParserBuilder.getFallbackStrategies().get(fieldName);
                value = fallback(strategies, pluginParserContext);
            }

            if (fieldName.equalsIgnoreCase(PluginParserConstant.VIEWS)) {
                Map<String, Object> views = mapper.readValue((String) value, Map.class);
                pluginPublishFields.setViews(views);
            } else if (fieldName.equalsIgnoreCase(PluginParserConstant.SEARCHABLE)) {
                pluginPublishFields.setSearchableFields((JsonNode) value);
            } else if (fieldName.equalsIgnoreCase(PluginParserConstant.PLUGIN_FILTERS)) {
                pluginPublishFields.setPluginFilters((List) value);
            } else {
                fields.put(fieldName, value);
            }
        }
        ObjectMapper om = new ObjectMapper();
        PluginManifest pluginManifest = om.readValue(om.writeValueAsString(fields), PluginManifest.class);
        return pluginPublishFields.setPluginManifest(pluginManifest);
    }

    /**
     * @param strategies fallback strategies object
     * @param pluginParserContext plugin parser context
     * @return field value
     * @throws IOException when the file reading operation fails
     */
    private Object fallback(FallbackStrategy strategies, PluginParserContext pluginParserContext) throws IOException {
        return strategies.apply(pluginParserContext);
    }

}
