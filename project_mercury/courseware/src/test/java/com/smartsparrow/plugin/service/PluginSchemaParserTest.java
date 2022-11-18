package com.smartsparrow.plugin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.util.Json;

class PluginSchemaParserTest {

    @InjectMocks
    private PluginSchemaParser pluginSchemaParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("JSON object with learnerEditable property should be extracted")
    void extractOutputSchema() {
        String json = "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"" +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";

        String result = pluginSchemaParser.extractOutputSchema(json);

        assertEquals("{\"selection\":{\"learnerEditable\":true," +
                "\"label\":\"selection\",\"type\":\"list\"}}", result);
    }

    @Test
    @DisplayName("JSON object only with learnerEditable=true should be extracted")
    void extractOutputSchema_false() {
        String json = "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"" +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": false," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";

        String result = pluginSchemaParser.extractOutputSchema(json);

        assertEquals("{}", result);
    }

    @Test
    @DisplayName("Fault should thrown if json is invalid")
    void extractOutputSchema_invalidJson() {
        String json = "invalid json";

        assertThrows(JSONException.class, () -> pluginSchemaParser.extractOutputSchema(json));
    }

    @Test
    @DisplayName("Fault should thrown if learnerEditable has incorrect type")
    void extractOutputSchema_invalidLearnerEditableValue() {
        String json = "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"" +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": \"foo\"," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";

        assertThrows(JSONException.class, () -> pluginSchemaParser.extractOutputSchema(json));
    }

    @Test
    @DisplayName("Values for output schema should be extracted")
    void extractOutputConfig_success() {
        String outputSchema = "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"listType\": \"text\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  } }";
        String config = "{\"selection\":[\"Answer 2\"], \"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("{\"selection\":[\"Answer 2\"]}", result);
    }

    @Test
    @DisplayName("Output config should have null values if no values in config")
    void extractOutputConfig_noValueInConfig() {
        String outputSchema = "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"listType\": \"text\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  } }";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("{\"selection\":null}", result);
    }

    @Test
    @DisplayName("Output config should have null value for the field if it does not present in config")
    void extractOutputConfig_theOneOfFieldsNoInConfig() {
        String outputSchema =  "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        String expectedJson = "{\"selection\":null,\"items\":[\"Choice 1\",\"Answer 2\",\"3rd Option\"]}";
        assertTrue(Json.parse(expectedJson).similar(Json.parse(result)));
    }

    @Test
    @DisplayName("Output config should have default values for group if available if no values in config")
    void extractOutputConfig_defaultValueGroupNode() {
        String outputSchema = "{" +
                "  \"progress\": {" +
                "    \"learnerName\": {" +
                "      \"type\": \"text\"," +
                "      \"default\": \"test\"," +
                "      \"learnerEditable\": true" +
                "  } } }";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("{\"progress\":{\"learnerName\":\"test\"}}", result);
    }

    @Test
    @DisplayName("Output config should have default values if available if no values in config")
    void extractOutputConfig_defaultValueObjectNode() {
        String outputSchema = "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"listType\": \"text\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"," +
                "    \"default\": [\"Answer 2\"]" +
                "  } }";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("{\"selection\":\"[\\\"Answer 2\\\"]\"}", result);
    }

    @Test
    @DisplayName("Output config should have null values if no values in config or default")
    void extractOutputConfig_noDefaultValue() {
        String outputSchema = "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"listType\": \"text\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  } }";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("{\"selection\":null}", result);
    }

    @Test
    @DisplayName("Throw exception if schema is invalid")
    void extractOutputConfig_invalidSchema() {
        String outputSchema = "invalid";
        String config = "{\"selection\":[\"Answer 2\"], \"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        assertThrows(JSONException.class, () -> pluginSchemaParser.extractOutputConfig(outputSchema, config));
    }

    @Test
    @DisplayName("Throw exception if config is invalid")
    void extractOutputConfig_invalidConfig() {
        String outputSchema = "{}";
        String config = "invalid";

        assertThrows(JSONException.class, () -> pluginSchemaParser.extractOutputConfig(outputSchema, config));
    }

    @Test
    @DisplayName("Extract empty config if outputSchema is empty")
    void extractOutputConfig_emptySchema() {
        String outputSchema = "";
        String config = "{\"items\":[\"Choice 1\", \"Answer 2\", \"3rd Option\"]}";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Extract empty output config if config is empty")
    void extractOutputConfig_emptyConfig() {
        String outputSchema = "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"listType\": \"text\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  } }";
        String config = "";

        String result = pluginSchemaParser.extractOutputConfig(outputSchema, config);

        assertEquals("", result);
    }

    @Test
    @DisplayName("Data and schema should have exactly the same set of fields")
    void validateDataAgainstSchema() {
        String outputSchema =  "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";
        String data = "{\"selection\":null,\"items\":[\"Choice 1\",\"Answer 2\",\"3rd Option\"]}";

        pluginSchemaParser.validateDataAgainstSchema(data, outputSchema);
    }

    @Test
    @DisplayName("Throw error if data has extra fields")
    void validateDataAgainstSchema_extraFieldsInData() {
        String outputSchema =  "{" +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";
        String data = "{\"selection\":null,\"items\":[\"Choice 1\",\"Answer 2\",\"3rd Option\"]}";

        assertThrows(InvalidFieldsException.class, () -> pluginSchemaParser.validateDataAgainstSchema(data, outputSchema));
    }

    @Test
    @DisplayName("Throw error if data does not have all fields from schema")
    void validateDataAgainstSchema_notAllFieldsFromSchema() {
        String outputSchema =  "{" +
                "  \"items\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "  }," +
                "  \"selection\": {" +
                "    \"type\": \"list\"," +
                "    \"learnerEditable\": true," +
                "    \"label\": \"selection\"" +
                "  }" +
                "}";
        String data = "{\"selection\":null}";

        assertThrows(InvalidFieldsException.class, () -> pluginSchemaParser.validateDataAgainstSchema(data, outputSchema));
    }

    @Test
    @DisplayName("It should extract learner editable properties within a group")
    void parse_groupedProperties() {
        String configurationSchema = "{\n" +
                                    "    \"title\": {\n" +
                                    "        \"type\": \"text\"\n" +
                                    "    },\n" +
                                    "    \"description\": {\n" +
                                    "        \"type\": \"rich-text\"\n" +
                                    "    },\n" +
                                    "    \"learnerCurrentAttempt\": {\n" +
                                    "        \"type\": \"number\",\n" +
                                    "        \"default\": 1,\n" +
                                    "        \"readOnly\": true,\n" +
                                    "        \"label\": \"Current Attempt\",\n" +
                                    "        \"learnerEditable\": true\n" +
                                    "    },\n" +
                                    "    \"progress\": {\n" +
                                    "        \"type\": \"group\",\n" +
                                    "        \"properties\": {\n" +
                                    "            \"progressPercentage\": {\n" +
                                    "                \"type\": \"number\",\n" +
                                    "                \"default\": 0\n" +
                                    "            },\n" +
                                    "            \"learnerName\": {\n" +
                                    "                \"type\": \"text\",\n" +
                                    "                \"default\": \"\",\n" +
                                    "                \"learnerEditable\": true\n" +
                                    "            },\n" +
                                    "            \"confidence\": {\n" +
                                    "                \"type\": \"number\",\n" +
                                    "                \"default\": 1,\n" +
                                    "                \"learnerEditable\": false\n" +
                                    "            }\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "}";

        String outputSchema = pluginSchemaParser.extractOutputSchema(configurationSchema);
        assertEquals("{\"progress\":{" +
                                "\"learnerName\":{" +
                                    "\"default\":\"\"," +
                                    "\"learnerEditable\":true," +
                                    "\"type\":\"text\"" +
                                    "}}," +
                                "\"learnerCurrentAttempt\":{" +
                                    "\"default\":1," +
                                    "\"learnerEditable\":true," +
                                    "\"readOnly\":true," +
                                    "\"label\":\"Current Attempt\"," +
                                    "\"type\":\"number\"}}", outputSchema);
    }

    @Test
    @DisplayName("It should not create an epmty object when a group node doesn't have any learnerEditable nodes")
    void parse_group_noneEditable() {
        String configurationSchema = "{\n" +
                "    \"title\": {\n" +
                "        \"type\": \"text\"\n" +
                "    },\n" +
                "    \"description\": {\n" +
                "        \"type\": \"rich-text\"\n" +
                "    },\n" +
                "    \"learnerCurrentAttempt\": {\n" +
                "        \"type\": \"number\",\n" +
                "        \"default\": 1,\n" +
                "        \"readOnly\": true,\n" +
                "        \"label\": \"Current Attempt\",\n" +
                "        \"learnerEditable\": true\n" +
                "    },\n" +
                "    \"progress\": {\n" +
                "        \"type\": \"group\",\n" +
                "        \"properties\": {\n" +
                "            \"progressPercentage\": {\n" +
                "                \"type\": \"number\",\n" +
                "                \"default\": 0\n" +
                "            },\n" +
                "            \"learnerName\": {\n" +
                "                \"type\": \"text\",\n" +
                "                \"default\": \"\",\n" +
                "                \"learnerEditable\": false\n" +
                "            },\n" +
                "            \"confidence\": {\n" +
                "                \"type\": \"number\",\n" +
                "                \"default\": 1,\n" +
                "                \"learnerEditable\": false\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String outputSchema = pluginSchemaParser.extractOutputSchema(configurationSchema);
        assertEquals("{" +
                                "\"learnerCurrentAttempt\":{" +
                                    "\"default\":1," +
                                    "\"learnerEditable\":true," +
                                    "\"readOnly\":true," +
                                    "\"label\":\"Current Attempt\"," +
                                    "\"type\":\"number\"" +
                                "}}", outputSchema);
    }

}
