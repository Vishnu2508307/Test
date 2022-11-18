package com.smartsparrow.plugin.publish;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.plugin.service.SchemaValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationSchemaFieldTest {

    @InjectMocks
    private ConfigurationSchemaField configurationSchemaField;

    @Mock
    private SchemaValidationService schemaValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() throws IOException, PluginPublishException {

        ClassLoader classLoader = getClass().getClassLoader();
        File configSchemaFile = load(classLoader, "configSchema.json");
        assertNotNull(configSchemaFile);
        Map<String, File> files = new HashMap<>();
        files.put("configSchema.json", configSchemaFile);

        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files)
                .setPluginType(PluginType.COMPONENT);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.CONFIG_SCHEMA, "configSchema.json");
        when(schemaValidationService.getSchemaFileName(any(PluginType.class))).thenReturn("");
        configurationSchemaField
                .setJsonObjectMap(jsonObject)
                .setFieldName(PluginParserConstant.CONFIG_SCHEMA);
        String configSchema = configurationSchemaField.parse(pluginParserContext);
        verify(schemaValidationService).getSchemaFileName(any(PluginType.class));
        assertNotNull(configSchema);
    }

    @Test
    void parse_Error_missingConfigSchemaField() {
        Map<String, Object> jsonObject = new HashMap<>();

        when(schemaValidationService.getSchemaFileName(any(PluginType.class))).thenReturn("");
        configurationSchemaField
                .setJsonObjectMap(jsonObject)
                .setFieldName(PluginParserConstant.CONFIG_SCHEMA);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            configurationSchemaField.parse(new PluginParserContext());
        });
        assertTrue(t.getMessage().contains("configuration schema field missing in manifest"));
    }

    @Test
    void parse_Error_missingConfigSchemaFile() {
        Map<String, File> files = new HashMap<>();
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files)
                .setPluginType(PluginType.COMPONENT);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.CONFIG_SCHEMA, "config.schema.json");

        when(schemaValidationService.getSchemaFileName(any(PluginType.class))).thenReturn("");
        configurationSchemaField
                .setJsonObjectMap(jsonObject)
                .setFieldName(PluginParserConstant.CONFIG_SCHEMA);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            configurationSchemaField.parse(pluginParserContext);
        });
        assertTrue(t.getMessage().contains("file missing"));
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }
}
