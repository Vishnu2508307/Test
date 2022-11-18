package com.smartsparrow.plugin.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.lang.PluginPublishException;


public class PluginFilterFieldTest {

    @InjectMocks
    private PluginFilterField pluginFilterField;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    String manifestContent = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"ID\",\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }]" +
            "}\n";

    String manifestEmptyFiltersNode = "{\n" +
            " \"filters\": []" +
            "}\n";

    String manifestFilterValuesNotArray = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"ID\",\n" +
            "      \"values\":\"foo\"\n" +
            "    }]" +
            "}\n";

    String manifestEmptyFilterValues = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"ID\",\n" +
            "      \"values\":[]\n" +
            "    }]" +
            "}\n";

    String manifestNotValidType = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"FOO\",\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }]" +
            "}\n";

    String manifestNotValidStructure = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"ID\",\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    },"+
            "{\n" +
            "      \"typeNode\": \"ID\",\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }]" +
            "}\n";

    String manifestMissingTypeNode = "{\n" +
            " \"filters\": [{\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }]" +
            "}\n";
    String manifestMissingTypeNodeValue = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"\",\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }]" +
            "}\n";
    String manifestMissingFilterValue = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"ID\"\n" +
            "    }]" +
            "}\n";
    String manifestFilterFieldNotAnArray = "{\n" +
            " \"filters\": {\n" +
            "      \"values\": [\"plugin1_UUID\", \"plugin2_UUID\", \"plugin3_UUID\"]\n" +
            "    }" +
            "}\n";

    String manifestMisingFilterNode = "{\n" +
            "}\n";

    String manifestContenteText = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"TAGS\",\n" +
            "      \"values\": [\"eTextAllowed\", \"eTextAllowed\", \"eTextAllowed\"]\n" +
            "    }]" +
            "}\n";

    String manifesteTextEmptyFilterValues = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"TAGS\",\n" +
            "      \"values\":[]\n" +
            "    }]" +
            "}\n";

    String manifesteTextFilterValuesNotArray = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"TAGS\",\n" +
            "      \"values\":\"foo\"\n" +
            "    }]" +
            "}\n";

    String manifestMissingeTextFilterValue = "{\n" +
            " \"filters\": [{\n" +
            "      \"type\": \"TAGS\"\n" +
            "    }]" +
            "}\n";

    @Test
    void parse_success() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestContent);
        List<PluginFilter> pluginFilterList = pluginFilterField.parse(new PluginParserContext()
                                                                              .setVersion("1.2.2")
                                                                              .setPluginId(UUID.randomUUID()));
        assertNotNull(pluginFilterList);
        assertNotNull(pluginFilterList.get(0).getFilterType());
        assertNotNull(pluginFilterList.get(0).getFilterValues());
    }

    @Test
    void parse_success_missingFiltersNode() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestMisingFilterNode);
        List<PluginFilter> pluginFilterList = pluginFilterField.parse(new PluginParserContext()
                                                                              .setVersion("1.2.2")
                                                                              .setPluginId(UUID.randomUUID()));
        assertNotNull(pluginFilterList);
        assertTrue(pluginFilterList.isEmpty());

    }

    @Test
    void parse_success_emptyFiltersNode() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestEmptyFiltersNode);
        List<PluginFilter> pluginFilterList = pluginFilterField.parse(new PluginParserContext()
                                                                              .setVersion("1.2.2")
                                                                              .setPluginId(UUID.randomUUID()));
        assertNotNull(pluginFilterList);
        assertTrue(pluginFilterList.isEmpty());

    }

    @Test
    void parse_missingTypeNode() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestMissingTypeNode);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter object has missing type field", t.getMessage());
    }

    @Test
    void parse_missingTypeNodeValue() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestMissingTypeNodeValue);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter object has missing or empty type field value", t.getMessage());
    }

    @Test
    void parse_missingFilterValues() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestMissingFilterValue);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter object has missing values field", t.getMessage());
    }

    @Test
    void parse_filterNotAnArray() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestFilterFieldNotAnArray);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Field 'filters' should be an array", t.getMessage());
    }

    @Test
    void parse_NotValidFilterType() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestNotValidType);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter object has not a valid type", t.getMessage());
    }

    @Test
    void parse_NotValidFilterNodeStructure() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestNotValidStructure);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter node has invalid structure", t.getMessage());
    }

    @Test
    void parse_filterValuesIsNotAnArray() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestFilterValuesNotArray);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Field 'filters values' should be an array", t.getMessage());
    }

    @Test
    void parse_emptyFilterValues() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestEmptyFilterValues);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Field 'filters values' should not be an empty array", t.getMessage());
    }

    @Test
    void parse_success_eText() throws IOException, PluginPublishException {
        pluginFilterField = new PluginFilterField("filters", manifestContenteText);
        List<PluginFilter> pluginFilterList = pluginFilterField.parse(new PluginParserContext()
                                                                              .setVersion("1.2.2")
                                                                              .setPluginId(UUID.randomUUID()));
        assertNotNull(pluginFilterList);
        assertNotNull(pluginFilterList.get(0).getFilterType());
        assertNotNull(pluginFilterList.get(0).getFilterValues());
    }

    @Test
    void parse_eTextemptyFilterValues(){
        pluginFilterField = new PluginFilterField("filters", manifesteTextEmptyFilterValues);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Field 'filters values' should not be an empty array", t.getMessage());
    }

    @Test
    void parse_eTextfilterValuesIsNotAnArray(){
        pluginFilterField = new PluginFilterField("filters", manifesteTextFilterValuesNotArray);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Field 'filters values' should be an array", t.getMessage());
    }

    @Test
    void parse_missingeTextFilterValues(){
        pluginFilterField = new PluginFilterField("filters", manifestMissingeTextFilterValue);
        Throwable throwable = assertThrows(PluginPublishException.class, () -> {
            pluginFilterField.parse(new PluginParserContext());
        });
        assertEquals("Filter object has missing values field", throwable.getMessage());
    }

}

