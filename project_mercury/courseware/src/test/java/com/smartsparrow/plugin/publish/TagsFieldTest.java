package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagsFieldTest {
    @InjectMocks
    private TagsField tagsField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        List<String> tagsList = new ArrayList<>();
        tagsList.add("plugin");
        tagsList.add("smart");
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.TAGS, tagsList);
        tagsField = new TagsField(PluginParserConstant.TAGS, jsonObject);
        List<String> tags = tagsField.parse(new PluginParserContext());
        assertNotNull(tags);
    }

    @Test
    void parse_missingTagField() {
        Map<String, Object> jsonObject = new HashMap<>();
        tagsField = new TagsField(PluginParserConstant.TAGS, jsonObject);
        List<String> tags = tagsField.parse(new PluginParserContext());
        assertNull(tags);
    }
}
