package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WhatsNewFieldTest {

    @InjectMocks
    private WhatsNewField whatsNewField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.WHATSNEW, "");
        whatsNewField = new WhatsNewField(PluginParserConstant.WHATSNEW, jsonObject);
        String version = whatsNewField.parse(new PluginParserContext());
        assertNotNull(version);
    }

    @Test
    void parse_missingWhatsNewField() {
        Map<String, Object> jsonObject = new HashMap<>();
        whatsNewField = new WhatsNewField(PluginParserConstant.WHATSNEW, jsonObject);
        String version = whatsNewField.parse(new PluginParserContext());
        assertNull(version);
    }
}
