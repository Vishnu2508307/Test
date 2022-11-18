package com.smartsparrow.plugin.publish;

import com.smartsparrow.exception.IllegalArgumentFault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PluginIdFieldTest {
    @InjectMocks
    private PluginIdField pluginIdField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.PLUGIN_ID, "4d1e3400-d526-11ea-85e2-d91eca3ca4ab");
        pluginIdField = new PluginIdField(PluginParserConstant.PLUGIN_ID, jsonObject);
         UUID pluginId = pluginIdField.parse(new PluginParserContext());
        assertNotNull(pluginId);
    }

    @Test
    void parse_success_WithPluginId() {
        Map<String, Object> jsonObject = new HashMap<>();
        pluginIdField = new PluginIdField(PluginParserConstant.PLUGIN_ID, jsonObject);
        UUID pluginId = pluginIdField.parse(new PluginParserContext()
                .setPluginId(UUID.fromString("4d1e3400-d526-11ea-85e2-d91eca3ca4ab")));
        assertNotNull(pluginId);
    }

    @Test
    void parse_ERROR() {
        Map<String, Object> jsonObject = new HashMap<>();
        pluginIdField = new PluginIdField(PluginParserConstant.PLUGIN_ID, jsonObject);
        Throwable t = assertThrows(IllegalArgumentFault.class, () -> {
            pluginIdField.parse(new PluginParserContext());
        });
        assertTrue(t.getMessage().contains("missing required plugin id"));
    }
}
