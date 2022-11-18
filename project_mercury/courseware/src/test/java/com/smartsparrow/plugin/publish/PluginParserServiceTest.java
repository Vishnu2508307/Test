package com.smartsparrow.plugin.publish;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.util.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;


public class PluginParserServiceTest {

    @InjectMocks
    private PluginParserService pluginParserService;
    @Mock
    ConfigurationSchemaField configurationSchemaField;
    @Mock
    PluginParser pluginParser;


    private static final UUID PLUGIN_ID = UUIDs.timeBased();
    private static final UUID publisherId = UUID.randomUUID();
    private static final String ZIP_HASH = "5cd282f2";

    String searchable = "[{\"contentType\": \"mcq\"," +
            "\"summary\": \"title\"," +
            "\"body\": \"selection\"}," +
            "{\"contentType\": \"text\"," +
            "\"body\": [\"options.foo\"]}," +
            "{\"contentType\": \"image\"," +
            "\"body\": [\"cards.front-text\"," +
            "\"cards.back-text\"]," +
            "\"source\": [\"cards.front-image\"," +
            "\"cards.back-image\"]}," +
            "{\"contentType\": \"text\"," +
            "\"summary\": [\"title\"]," +
            "\"body\": [\"stage.text\"]}]";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        pluginParserService = new PluginParserService(configurationSchemaField, pluginParser);

    }

    @Test
    void parseManifest_success() throws IOException, PluginPublishException {
        File plugin = build_success("manifest.json");
        Map<String, File> files = new HashMap<>();
        files.put(PluginParserConstant.MANIFEST_JSON, plugin);

        PluginParsedFields pluginParsedFields = new PluginParsedFields()
                .setSearchableFields(Json.toJsonNode(searchable))
                .setPluginManifest(new PluginManifest()
                        .setPluginId(UUID.randomUUID()))
                .setViews(new HashMap<>());

        when(pluginParser.parse(Mockito.any(), Mockito.any())).thenReturn(pluginParsedFields);
        PluginParsedFields pluginPublishFields = pluginParserService.parse(files, PLUGIN_ID, ZIP_HASH, publisherId);
        assertNotNull(pluginPublishFields);
        assertNotNull(pluginPublishFields.getPluginManifest());
        assertNotNull(pluginPublishFields.getSearchableFields());
        assertNotNull(pluginPublishFields.getViews());
    }

    @Test
    void parsePackage_success() throws IOException, PluginPublishException {
        File plugin = build_success("package.json");
        Map<String, File> files = new HashMap<>();
        files.put(PluginParserConstant.PACKAGE_JSON, plugin);
        PluginParsedFields pluginParsedFields = new PluginParsedFields()
                .setSearchableFields(Json.toJsonNode(searchable))
                .setPluginManifest(new PluginManifest()
                .setPluginId(UUID.randomUUID()))
                .setViews(new HashMap<>());

        when(pluginParser.parse(Mockito.any(), Mockito.any())).thenReturn(pluginParsedFields);
        PluginParsedFields pluginPublishFields = pluginParserService.parse(files, PLUGIN_ID, ZIP_HASH, publisherId);
        assertNotNull(pluginPublishFields);
        assertNotNull(pluginPublishFields.getPluginManifest());
        assertNotNull(pluginPublishFields.getSearchableFields());
        assertNotNull(pluginPublishFields.getViews());
    }

    @Test
    void parsePackage_ERROR_missingBronte() {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, "packageMissingBronte.json");
        assertNotNull(plugin);
        Map<String, File> files = new HashMap<>();
        files.put(PluginParserConstant.PACKAGE_JSON, plugin);

        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            pluginParserService.parse(files, PLUGIN_ID, ZIP_HASH, publisherId);
        });
        assertTrue(t.getMessage().contains("bronte is missing in package.json"));
    }

    @Test
    void parse_ERROR_manifestPackageMissing() {
        Map<String, File> files = new HashMap<>();
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            pluginParserService.parse(files, PLUGIN_ID, ZIP_HASH, publisherId);
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

    private File build_success(String fileName) throws IOException, PluginPublishException {
        JsonNode jsonNode = null;
        PluginParsedFields pluginPublishField = new PluginParsedFields()
                .setPluginManifest(new PluginManifest())
                .setSearchableFields(jsonNode)
                .setViews(new HashMap<>());
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, fileName);
        assertNotNull(plugin);
        when(configurationSchemaField.setJsonObjectMap(any())).thenReturn(new ConfigurationSchemaField());
        when(configurationSchemaField.setFieldName(anyString())).thenReturn(new ConfigurationSchemaField());
        when(pluginParser.parse(any(PluginParserContext.class), any(PluginParserBuilder.class))).thenReturn(pluginPublishField);
        return plugin;
    }
}
