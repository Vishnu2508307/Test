package com.smartsparrow.plugin.publish;

import com.smartsparrow.plugin.lang.PluginPublishException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PluginParserTest {

    @InjectMocks
    private PluginParser pluginParser;

    @Mock
    PluginParserContext pluginParserContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() throws IOException, PluginPublishException {
        Map<String, PluginField> fieldMap = new HashMap<>();
        Map<String, Object> jsonObject = new HashMap<>();

        //set plugin id
       jsonObject.put(PluginParserConstant.PLUGIN_ID, "4d1e3400-d526-11ea-85e2-d91eca3ca4ab");
        fieldMap.put(PluginParserConstant.PLUGIN_ID, new PluginIdField(PluginParserConstant.PLUGIN_ID, jsonObject));

        //set views
        Map<String, Object> viewmap = new LinkedHashMap<>();
        Map<String, Object> learnerMap = new LinkedHashMap<>();
        learnerMap.put("contentType", "javascript");
        viewmap.put("LEARNER", learnerMap);
        jsonObject.put(PluginParserConstant.VIEWS, viewmap);
        fieldMap.put(PluginParserConstant.VIEWS, new ManifestViewField(PluginParserConstant.VIEWS, jsonObject));

        Map<String, FallbackStrategy> fallbackStrategyMap = new HashMap<>();
        PluginParserBuilder pluginParserBuilder = new PluginParserBuilder()
                .setFieldMap(fieldMap)
                .setFallbackStrategies(fallbackStrategyMap);

        PluginParsedFields pluginPublishFields = pluginParser.parse(pluginParserContext, pluginParserBuilder);
        assertNotNull(pluginPublishFields);
        assertNotNull(pluginPublishFields.getPluginManifest());
        assertNotNull(pluginPublishFields.getViews());
    }
}
