package com.smartsparrow.plugin.publish;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class DescriptionFieldTest {
    @InjectMocks
    private DescriptionField descriptionField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.DESCRIPTION, "An electronic breadboard simulation");
        descriptionField = new DescriptionField(PluginParserConstant.DESCRIPTION, jsonObject);
        String description = descriptionField.parse(new PluginParserContext());
        assertNotNull(description);
    }

    @Test
    void parse_missingDescriptionField() {
        Map<String, Object> jsonObject = new HashMap<>();
        descriptionField = new DescriptionField(PluginParserConstant.DESCRIPTION, jsonObject);
        String description = descriptionField.parse(new PluginParserContext());
        assertNull(description);
    }
}
