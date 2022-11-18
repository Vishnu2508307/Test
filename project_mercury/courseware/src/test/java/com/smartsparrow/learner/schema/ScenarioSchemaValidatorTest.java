package com.smartsparrow.learner.schema;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class ScenarioSchemaValidatorTest {

    @InjectMocks
    ScenarioSchemaValidator scenarioSchemaValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validScenario() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"CHAINED_CONDITION\",\n" +
                "      \"operator\": \"AND\",\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"type\": \"EVALUATOR\",\n" +
                "          \"operator\": \"IS\",\n" +
                "          \"operandType\": \"STRING\", \n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"SCOPE\",\n" +
                "            \t\"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\"\n" +
                "            },\n" +
                "            \"value\": \"Option A\"\n" +
                "          },\n" +
                "          \"options\": [\n" +
                "            {\"IGNORE_CASE\": true},\n" +
                "            {\"DECIMAL\": 2}\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertAll(() -> scenarioSchemaValidator.validate(json));
    }

    @Test
    void invalidScenarioTypeNotFound() {
        String json = "{\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"CHAINED_CONDITION\",\n" +
                "      \"operator\": \"AND\",\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"type\": \"EVALUATOR\",\n" +
                "          \"operator\": \"IS\",\n" +
                "          \"operandType\": \"STRING\", \n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"SCOPE\",\n" +
                "            \t\"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\"\n" +
                "            },\n" +
                "            \"value\": \"Option A\"\n" +
                "          },\n" +
                "          \"options\": [\n" +
                "            {\"IGNORE_CASE\": true},\n" +
                "            {\"DECIMAL\": 2}\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ValidationException e = assertThrows(ValidationException.class, () ->
                scenarioSchemaValidator.validate(json));
        assertEquals("#: required key [type] not found", e.getMessage());
    }

    @Test
    void invalidScenarioSecondLevelOperatorNotFound() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"CHAINED_CONDITION\",\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"type\": \"EVALUATOR\",\n" +
                "          \"operator\": \"IS\",\n" +
                "          \"operandType\": \"STRING\", \n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"SCOPE\",\n" +
                "            \t\"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\"\n" +
                "            },\n" +
                "            \"value\": \"Option A\"\n" +
                "          },\n" +
                "          \"options\": [\n" +
                "            {\"IGNORE_CASE\": true},\n" +
                "            {\"DECIMAL\": 2}\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ValidationException e = assertThrows(ValidationException.class, () ->
                scenarioSchemaValidator.validate(json));
        assertEquals("#/conditions/0: #: no subschema matched out of the total 2 subschemas", e.getMessage());
    }

    @Test
    void invalidScenarioMissingResolverType() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"CHAINED_CONDITION\",\n" +
                "      \"operator\": \"AND\",\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"type\": \"EVALUATOR\",\n" +
                "          \"operator\": \"IS\",\n" +
                "          \"operandType\": \"STRING\", \n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "            \t\"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\"\n" +
                "            },\n" +
                "            \"value\": \"Option A\"\n" +
                "          },\n" +
                "          \"options\": [\n" +
                "            {\"IGNORE_CASE\": true},\n" +
                "            {\"DECIMAL\": 2}\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ValidationException e = assertThrows(ValidationException.class, () ->
                scenarioSchemaValidator.validate(json));
        assertEquals("#/conditions/0: #: no subschema matched out of the total 2 subschemas", e.getMessage());
    }

    @Test
    void invalidScenarioMissingOptions() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"CHAINED_CONDITION\",\n" +
                "      \"operator\": \"AND\",\n" +
                "      \"conditions\": [\n" +
                "        {\n" +
                "          \"type\": \"EVALUATOR\",\n" +
                "          \"operator\": \"IS\",\n" +
                "          \"operandType\": \"STRING\", \n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"SCOPE\",\n" +
                "            \t\"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\"\n" +
                "            },\n" +
                "            \"value\": \"Option A\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ValidationException e = assertThrows(ValidationException.class, () ->
                scenarioSchemaValidator.validate(json));
        assertEquals("#/conditions/0: #: no subschema matched out of the total 2 subschemas", e.getMessage());
    }
}
