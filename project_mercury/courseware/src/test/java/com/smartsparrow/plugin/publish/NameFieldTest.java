package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NameFieldTest {
    @InjectMocks
    private NameField nameField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.NAME, "Plugin Name");
        nameField = new NameField(PluginParserConstant.NAME, jsonObject);
        String name = nameField.parse(new PluginParserContext());
        assertNotNull(name);
    }

    @Test
    void parse_missingNameField() {
        Map<String, Object> jsonObject = new HashMap<>();
        nameField = new NameField(PluginParserConstant.NAME, jsonObject);
        String name = nameField.parse(new PluginParserContext());
        assertNull(name);
    }
}
