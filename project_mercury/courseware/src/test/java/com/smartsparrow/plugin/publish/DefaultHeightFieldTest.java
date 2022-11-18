package com.smartsparrow.plugin.publish;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


public class DefaultHeightFieldTest {
    @InjectMocks
    private DefaultHeightField defaultHeightField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.DEFAULT_HEIGHT, "auto");
        defaultHeightField = new DefaultHeightField(PluginParserConstant.DEFAULT_HEIGHT, jsonObject);
        String defaultHeight = defaultHeightField.parse(new PluginParserContext());
        assertNotNull(defaultHeight);
    }

    @Test
    void parse_missingDefaultHeightField() {
        Map<String, Object> jsonObject = new HashMap<>();
        defaultHeightField = new DefaultHeightField(PluginParserConstant.DEFAULT_HEIGHT, jsonObject);
        String defaultHeight = defaultHeightField.parse(new PluginParserContext());
        assertNull(defaultHeight);
    }
}
