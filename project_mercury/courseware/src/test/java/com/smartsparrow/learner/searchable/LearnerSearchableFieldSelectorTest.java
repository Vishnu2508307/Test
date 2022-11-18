package com.smartsparrow.learner.searchable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.plugin.data.PluginSearchableField;

class LearnerSearchableFieldSelectorTest {

    @InjectMocks
    private LearnerSearchableFieldSelector searchableFieldSelector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void select_noField() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> searchableFieldSelector.select(null, null));

        assertEquals("pluginSearchableField is required", f.getMessage());
    }

    @Test
    void select_noConfig() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> searchableFieldSelector.select(new PluginSearchableField(), null));

        assertEquals("config is required", f.getMessage());
    }

    @Test
    void select_noFields() {
        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(new HashSet<>())
                .setSource(new HashSet<>())
                .setSummary(new HashSet<>())
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, "{\"foo\": \"bar\"}");

        assertNotNull(selected);
        assertTrue(selected.isEmpty());
    }

    @Test
    void select_invalidJson() {
        final String config = "invalid json";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(Sets.newHashSet("stage.plainText"))
                .setSource(new HashSet<>())
                .setSummary(new HashSet<>())
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);

        assertNotNull(selected);
        assertTrue(selected.isEmpty());
    }

    @Test
    void select_valueNotAString() {
        final String config = "{\n" +
                "  \"stage\": {\n" +
                "    \"plainText\": true\n" +
                "  },\n" +
                "  \"cards\": {\n" +
                "    \"front-text\": \"this is the front text\",\n" +
                "    \"back-text\": \"this is the back text\"\n" +
                "  }\n" +
                "}";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(Sets.newHashSet("stage.plainText"))
                .setSource(new HashSet<>())
                .setSummary(new HashSet<>())
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);
        assertNotNull(selected);
        assertTrue(selected.isEmpty());
    }

    @Test
    void select_notFound() {
        final String config = "{\n" +
                "  \"stage\": {\n" +
                "  },\n" +
                "  \"cards\": {\n" +
                "    \"front-text\": \"this is the front text\",\n" +
                "    \"back-text\": \"this is the back text\"\n" +
                "  }\n" +
                "}";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(Sets.newHashSet("stage.plainText"))
                .setSource(new HashSet<>())
                .setSummary(Sets.newHashSet("cards.front-text", "cards.back-text"))
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);
        assertNotNull(selected);
        assertFalse(selected.isEmpty());
        assertEquals("this is the front text this is the back text", selected.getSummary());
    }

    @Test
    void select_hasFields() {
        final String config = "{\n" +
                "  \"stage\": {\n" +
                "    \"plainText\": \"this is the selected text\"\n" +
                "  },\n" +
                "  \"cards\": {\n" +
                "    \"front-text\": \"this is the front text\",\n" +
                "    \"back-text\": \"this is the back text\"\n" +
                "  }\n" +
                "}";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(Sets.newHashSet("stage.plainText"))
                .setSource(new HashSet<>())
                .setSummary(Sets.newHashSet("cards.front-text", "cards.back-text"))
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);

        assertNotNull(selected);
        assertFalse(selected.isEmpty());
        assertEquals("this is the selected text", selected.getBody());
        final String expected = "this is the front text this is the back text";
        assertEquals(expected, selected.getSummary());
        assertEquals("", selected.getSource());
        assertEquals("", selected.getTag());
        assertEquals("", selected.getPreview());
    }

    @Test
    void select_withArray() {
        final String config = "{\n" +
                "  \"stage\": {\n" +
                "    \"plainText\": \"this is the selected text\"\n" +
                "  },\n" +
                "  \"cards\": [\n" +
                "    {\n" +
                "      \"front-text\": \"front text 001\",\n" +
                "      \"back-text\": \"back text 001\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"front-text\": \"front text 002\",\n" +
                "      \"back-text\": \"back text 002\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"front-text\": \"front text 003\",\n" +
                "      \"back-text\": \"back text 003\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(Sets.newHashSet("stage.plainText"))
                .setSource(new HashSet<>())
                .setSummary(Sets.newHashSet("cards.front-text", "cards.back-text"))
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);

        assertNotNull(selected);
        assertFalse(selected.isEmpty());
        assertEquals("this is the selected text", selected.getBody());
        final String expected = "front text 001 front text 002 front text 003 " +
                "back text 001 back text 002 back text 003";
        assertEquals(expected, selected.getSummary());
        assertEquals("", selected.getSource());
        assertEquals("", selected.getTag());
        assertEquals("", selected.getPreview());
    }

    @Test
    void select_nestedArrays() {
        final String config = "{\n" +
                "  \"cards\": [\n" +
                "    {\n" +
                "      \"front-text\": \"front text 001\",\n" +
                "      \"back-text\": \"back text 001\",\n" +
                "      \"foo\": [\n" +
                "        {\n" +
                "          \"bar\": \"wow 1\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"bar\": \"wow 2\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"front-text\": \"front text 002\",\n" +
                "      \"back-text\": \"back text 002\",\n" +
                "      \"foo\": [\n" +
                "        {\n" +
                "          \"bar\": \"wow 3\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"bar\": \"wow 4\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"front-text\": \"front text 003\",\n" +
                "      \"back-text\": \"back text 003\",\n" +
                "      \"foo\": [\n" +
                "        {\n" +
                "          \"bar\": \"wow 5\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"bar\": \"wow 6\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        final PluginSearchableField searchableField = new PluginSearchableField()
                .setBody(new HashSet<>())
                .setSource(new HashSet<>())
                .setSummary(Sets.newHashSet("cards.foo.bar"))
                .setTag(new HashSet<>())
                .setPreview(new HashSet<>());

        LearnerSearchableFieldValue selected = searchableFieldSelector.select(searchableField, config);

        assertNotNull(selected);
        assertFalse(selected.isEmpty());
        assertEquals("", selected.getBody());
        final String expected = "wow 1 wow 2 wow 3 wow 4 wow 5 wow 6";
        assertEquals(expected, selected.getSummary());
        assertEquals("", selected.getSource());
        assertEquals("", selected.getTag());
        assertEquals("", selected.getPreview());
    }

}