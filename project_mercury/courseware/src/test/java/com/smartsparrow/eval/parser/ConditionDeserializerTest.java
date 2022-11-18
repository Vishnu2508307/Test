package com.smartsparrow.eval.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.lang.ScenarioConditionParserFault;
import com.smartsparrow.eval.deserializer.ConditionDeserializer;
import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.DataType;

class ConditionDeserializerTest {

    @InjectMocks
    private ConditionDeserializer conditionDeserializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deserialize_success() {
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
                "              \"sourceId\": \"ed7528e0-2fe7-11e9-bf5f-6d61095c71e1\",\n" +
                "              \"studentScopeURN\": \"b04df804-fdf6-4dd2-b33e-a3ed4ad3fc1d\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "              \t\"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\",\n" +
                "                \"learnerEditable\": true,\n" +
                "                \"foo\": \"bar\",\n" +
                "                \"default\":[\"item 1\"]" +
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
        ChainedCondition rootCondition = conditionDeserializer.deserialize(json).block();

        assertNotNull(rootCondition);

        assertEquals(Condition.Type.CHAINED_CONDITION, rootCondition.getType());
        assertEquals(Operator.Type.OR, rootCondition.getOperator());

        List<? extends BaseCondition> conditions = rootCondition.getConditions();

        assertEquals(1, conditions.size());

        ChainedCondition and = (ChainedCondition) conditions.get(0);

        assertEquals(Condition.Type.CHAINED_CONDITION, and.getType());
        assertEquals(Operator.Type.AND, and.getOperator());

        List<? extends BaseCondition> andConditions = and.getConditions();

        assertEquals(1, andConditions.size());

        Evaluator evaluator = (Evaluator) andConditions.get(0);

        assertEquals(Condition.Type.EVALUATOR, evaluator.getType());
        assertEquals(Operator.Type.IS, evaluator.getOperator());
        assertEquals(DataType.STRING, evaluator.getOperandType());
        assertEquals(2, evaluator.getOptions().size());

        Operand lhs = evaluator.getLhs();
        Operand rhs = evaluator.getRhs();

        assertNotNull(lhs);
        assertNotNull(rhs);

        assertNotNull(rhs.getValue());
        assertNull(lhs.getValue());

        LiteralContext literalResolver = (LiteralContext) rhs.getResolver();
        ScopeContext scopeResolver = (ScopeContext) lhs.getResolver();

        assertNotNull(literalResolver);
        assertEquals(Resolver.Type.LITERAL, literalResolver.getType());
        assertNotNull(scopeResolver);
        assertEquals(Resolver.Type.SCOPE, scopeResolver.getType());
        assertEquals("responses", scopeResolver.getCategory());
        assertEquals(Lists.newArrayList("selection"), scopeResolver.getContext());

        Map<String,Object> schemaProperty = scopeResolver.getSchemaProperty();
        assertNotNull(schemaProperty);
        assertEquals("My List", schemaProperty.get("label"));
        assertEquals("text", schemaProperty.get("listType"));
        assertEquals("list", schemaProperty.get("type"));
    }

    @Test
    void deserialize_missingTypeOnChainedCondition() {
        String json = "{\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "  ]\n" +
                "}";

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> conditionDeserializer.deserialize(json).block());
        assertEquals("`type` node is required for a ChainedCondition", e.getMessage());

    }

    @Test
    void deserialize_missingTypeOnEvaluator() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"operator\": \"IS\",\n" +
                "      \"operandType\": \"STRING\",\n" +
                "      \"lhs\": {\n" +
                "      },\n" +
                "      \"rhs\": {\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> conditionDeserializer.deserialize(json).block());
        assertEquals("`type` node is required for a BaseCondition", e.getMessage());
    }

    @Test
    void deserialize_missingOperatorOnChainedCondition() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"conditions\": [\n" +
                "  ]\n" +
                "}";

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> conditionDeserializer.deserialize(json).block());
        assertEquals("`operator` node is required for a ChainedCondition", e.getMessage());
    }

    @Test
    void deserialize_missingOperatorOnEvaluator() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"EVALUATOR\",\n" +
                "      \"operandType\": \"STRING\",\n" +
                "      \"lhs\": {\n" +
                "      },\n" +
                "      \"rhs\": {\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> conditionDeserializer.deserialize(json).block());
        assertEquals("`operator` node is required for Evaluator", e.getMessage());
    }

    @Test
    void deserialize_missingOperandTypeOnEvaluator() {
        String json = "{\n" +
                "  \"type\": \"CHAINED_CONDITION\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"type\": \"EVALUATOR\",\n" +
                "      \"operator\": \"IS\",\n" +
                "      \"lhs\": {\n" +
                "      },\n" +
                "      \"rhs\": {\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> conditionDeserializer.deserialize(json).block());
        assertEquals("`operandType` node is required for Evaluator", e.getMessage());
    }

    @Test
    void deserialize_invalidJson() {
        String invalidJson = "{\n" +
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
                "          \"operandType\": \"STRING\",\n" +
                "          \"lhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"SCOPE\",s\n" +
                "              \"id\": \"$UUID\",\n" +
                "              \"context\": [\"selection\"],\n" +
                "              \"schemaProperty\": {\n" +
                "                \"type\": \"list\",\n" +
                "                \"listType\": \"text\",\n" +
                "                \"label\": \"My List\"\n" +
                "              },\n" +
                "              \"category\": \"responses\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"rhs\": {\n" +
                "            \"resolver\": {\n" +
                "              \"type\": \"LITERAL\",\n" +
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

        assertThrows(ScenarioConditionParserFault.class, ()-> conditionDeserializer.deserialize(invalidJson).block());
    }

    @Test
    void deserialize_InvalidTypeValue() {
        String json = "{\n" +
                "  \"type\": \"AnotherCondition\",\n" +
                "  \"operator\": \"OR\",\n" +
                "  \"conditions\": [\n" +
                "    {\n" +
                "      \"operator\": \"IS\",\n" +
                "      \"operandType\": \"STRING\",\n" +
                "      \"lhs\": {\n" +
                "      },\n" +
                "      \"rhs\": {\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertThrows(IllegalArgumentException.class, ()-> conditionDeserializer.deserialize(json).block());
    }

    @Test
    @DisplayName("It should allow empty conditions to be deserialized")
    void deserialize_emptyConditions() {
        String emptyConditions = "{\"type\":\"CHAINED_CONDITION\",\"operator\":\"OR\",\"conditions\":[]}";

        ChainedCondition chainedcondition = conditionDeserializer.deserialize(emptyConditions).block();

        assertNotNull(chainedcondition);
        assertEquals(0, chainedcondition.getConditions().size());
    }
}
