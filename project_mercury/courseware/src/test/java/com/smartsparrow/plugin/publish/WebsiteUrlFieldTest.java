package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WebsiteUrlFieldTest {

    @InjectMocks
    private WebsiteUrlField websiteUrlField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.WEBSITEURL, "");
        websiteUrlField = new WebsiteUrlField(PluginParserConstant.WEBSITEURL, jsonObject);
        String version = websiteUrlField.parse(new PluginParserContext());
        assertNotNull(version);
    }

    @Test
    void parse_MissingWebsiteUrlField() {
        Map<String, Object> jsonObject = new HashMap<>();
        websiteUrlField = new WebsiteUrlField(PluginParserConstant.WEBSITEURL, jsonObject);
        String version = websiteUrlField.parse(new PluginParserContext());
        assertNull(version);
    }
}
