package mercury.helpers.courseware;

import static mercury.glue.step.courseware.ScenarioSteps.SCENARIO_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Null;

import org.assertj.core.util.Strings;

import com.consol.citrus.context.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.util.DataType;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;

public class ScenarioHelper {

    /**
     * Create a message request for creating a scenario with all fields
     *
     * @param fields   map with scenario fields
     * @param parentId the parent id
     * @return a json string representing the message request
     */
    public static String createScenarioRequest(Map<String, String> fields, String parentId) {

        String name = fields.get("name");
        String lifecycle = fields.get("lifecycle");
        String condition = fields.get("condition");
        String actions = fields.get("actions");
        String description = fields.get("description");
        String correctness = fields.get("correctness");

        return new PayloadBuilder()
                .addField("type", "author.scenario.create")
                .addField("name", name)
                .addField("lifecycle", lifecycle)
                .addField("condition", condition)
                .addField("actions", actions)
                .addField("description", description)
                .addField("correctness", correctness)
                .addField("parentId", parentId)
                .build();
    }

    /**
     * Create a message request for creating a scenario with required fields
     *
     * @param name      the scenario name
     * @param lifecycle the scenario lifecycle
     * @param parentId  the scenario parent id
     * @return a json string representing the message request
     */
    public static String createScenarioRequest(String name, String lifecycle, String parentId) {
        return new PayloadBuilder()
                .addField("type", "author.scenario.create")
                .addField("name", name)
                .addField("lifecycle", lifecycle)
                .addField("parentId", parentId)
                .addField("actions", "[]")
                .addField("condition", "{\"type\":\"CHAINED_CONDITION\",\"operator\":\"OR\",\"conditions\":[]}")
                .build();
    }

    public static String createScenarioErrorResponse(int code, String message) {
        return "{" +
                "\"type\": \"author.scenario.create.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    /**
     * Returns a validation callback to validate a create/update scenario responses
     *
     * @param messageType             type of response message
     * @param expectedScenarioVarName citrus variable name where expected scenario is stored
     * @return a validation callback
     */
    public static ResponseMessageValidationCallback<Scenario> scenarioValidationCallback(final String messageType,
                                                                                         final String expectedScenarioVarName) {
        return new ResponseMessageValidationCallback<Scenario>(Scenario.class) {
            @Override
            public String getRootElementName() {
                return "scenario";
            }

            @Override
            public String getType() {
                return messageType;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void validate(Scenario payload, Map<String, Object> headers, TestContext context) {
                Map<String, String> expectedScenario = context.getVariable(expectedScenarioVarName, Map.class);

                assertEquals(expectedScenario.get("name"), payload.getName());
                assertEquals(expectedScenario.get("lifecycle"), (payload.getLifecycle() == null) ? null : payload.getLifecycle().name());
                assertEquals(expectedScenario.get("description"), payload.getDescription());
                assertEquals(expectedScenario.get("condition"), payload.getCondition());
                assertEquals(expectedScenario.get("actions"), payload.getActions());
                assertEquals(expectedScenario.get("correctness"), (payload.getCorrectness() == null) ? null : payload.getCorrectness().toString());
                assertNotNull(payload.getId());
            }
        };
    }

    /**
     * Returns a validation callback to validate that response contains expected scenario
     *
     * @param messageType      type of response message
     * @param expectedScenario fields of a expected scenario
     * @return a validation callback
     */
    public static ResponseMessageValidationCallback<List> scenarioListValidationCallback(final String messageType,
                                                                                         final Map<String, String> expectedScenario) {
        return new ResponseMessageValidationCallback<List>(List.class) {
            @Override
            public String getRootElementName() {
                return "scenarios";
            }

            @Override
            public String getType() {
                return messageType;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                assertEquals(1, payload.size());

                Map<String, String> actualScenario = (Map<String, String>) payload.get(0);

                assertEquals(expectedScenario.get("name"), actualScenario.get("name"));
                assertEquals(expectedScenario.get("lifecycle"), actualScenario.get("lifecycle"));
                assertEquals(expectedScenario.get("description"), actualScenario.get("description"));
                assertEquals(expectedScenario.get("condition"), actualScenario.get("condition"));
                assertEquals(expectedScenario.get("actions"), actualScenario.get("actions"));
                assertEquals(expectedScenario.get("correctness"), actualScenario.get("correctness"));
            }
        };
    }

    /**
     * Create a message request to update an existing scenario with the supplied field values
     *
     * @param scenarioId the id of the scenario to update
     * @param fields     the scenario fields values
     * @return a json string representing the message request
     */
    public static String updateScenarioRequest(String scenarioId, Map<String, String> fields) {
        return new PayloadBuilder()
                .addField("type", "author.scenario.replace")
                .addField("scenarioId", scenarioId)
                .addField("condition", fields.get("condition"))
                .addField("actions", fields.get("actions"))
                .addField("name", fields.get("name"))
                .addField("description", fields.get("description"))
                .addField("correctness", fields.get("correctness"))
                .build();
    }

    /**
     * Create a scenario reorder request
     *
     * @param parentEntityName  the name of the parent entity the scenario belongs to
     * @param scenarioLifecycle the scenario lifecycle
     * @param scenarioIds       list of scenario ids
     * @return a string representation of the json message request
     */
    public static String reorderScenariosRequest(String parentEntityName, String scenarioLifecycle, List<String> scenarioIds) {
        return new PayloadBuilder()
                .addField("type", "author.scenarios.reorder")
                .addField("parentId", Variables.interpolate(String.format("%s_id", parentEntityName)))
                .addField("lifecycle", scenarioLifecycle)
                .addField("scenarioIds", scenarioIds)
                .build();
    }

    /**
     * Validate a reorder scenario message response
     *
     * @param lifecycle   scenario lifecycle
     * @param scenarioIds a list of scenario ids
     * @return a string representation of the expected message response
     */
    public static String reorderScenariosResponse(String lifecycle, List<String> scenarioIds) {
        return "{" +
                "\"type\":\"author.scenarios.reorder.ok\"," +
                "\"response\":{" +
                "\"scenariosByParent\":{" +
                "\"parentId\":\"@notEmpty()@\"," +
                "\"lifecycle\":\"" + lifecycle + "\"," +
                "\"scenarioIds\":[" + appendScenarioIds(scenarioIds) + "]" +
                "}" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String reorderScenarioErrorResponse(int code, String message) {
        return "{" +
                "\"type\": \"author.scenarios.reorder.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    /**
     * Build a json array containing the citrus vars representing scenarios ids
     *
     * @param scenarioIds a list of scenario ids
     * @return a string representing a json array containing scenario ids
     */
    private static String appendScenarioIds(List<String> scenarioIds) {
        StringBuilder sb = new StringBuilder();
        for (String scenarioId : scenarioIds) {
            sb.append("\"");
            sb.append(scenarioId);
            sb.append("\"");
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    public static ResponseMessageValidationCallback<List> scenarioDuplicatedListValidationCallback(final List<String> expectedScenarios) {
        return new ResponseMessageValidationCallback<List>(List.class) {
            @Override
            public String getRootElementName() {
                return "scenarios";
            }

            @Override
            public String getType() {
                return "author.scenario.list.ok";
            }

            @SuppressWarnings("unchecked")
            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                assertEquals(expectedScenarios.size(), payload.size());

                for (int i = 0; i < payload.size(); i++) {
                    Map<String, String> actualScenario = (Map<String, String>) payload.get(i);
                    String expectedScenario = expectedScenarios.get(i);

                    assertEquals(expectedScenario, actualScenario.get("name"));
                    assertNotEquals(context.getVariable(SCENARIO_ID + expectedScenario), actualScenario.get("id"));
                }
            }
        };
    }

    public static String listScenariosRequest(String parentId, String lifecycle) {
        return new PayloadBuilder()
                .addField("type", "author.scenario.list")
                .addField("parentId", parentId)
                .addField("lifecycle", lifecycle)
                .build();
    }

    public static String listScenariosErrorResponse(int code, String message) {
        return "{" +
                "\"type\": \"author.scenario.list.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String createChangeProgressAction(ProgressionType option, @Nullable String coursewareElementId) {
        return createChangeProgressAction(option, coursewareElementId, null);
    }

    public static String createChangeProgressAction(ProgressionType option, @Nullable String elementId, @Nullable String elementType) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode contextNode = mapper.createObjectNode().put("progressionType", option.name());

        if (!Strings.isNullOrEmpty(elementId)) {
            contextNode.put("elementId", elementId);
        }

        if (!Strings.isNullOrEmpty(elementType)) {
            contextNode.put("elementType", elementType);
        }

        ObjectNode node = (ObjectNode) mapper.createObjectNode()
                .put("action", Action.Type.CHANGE_PROGRESS.name())
                .set("resolver", mapper.createObjectNode().put("type", Resolver.Type.LITERAL.name()));
        node.set("context", contextNode);
        return node.toString();

    }

    public static String createChangeScopeActionWithScopeResolver(String studentScopeURN, String sourceId, String type,
                                                                  DataType dataType, MutationOperator mutationOperator) {
        return "{\n" +
                "\"action\": \"CHANGE_SCOPE\",\n" +
                "\"resolver\": {\n" +
                    "\"type\": \"SCOPE\",\n" +
                    "\"category\": \"responses\",\n" +
                    "\"context\": [\n" +
                        "\"selection\"\n" +
                    "],\n" +
                    "\"schemaProperty\": {\n" +
                        "\"label\": \"Selected choices\",\n" +
                        "\"learnerEditable\": true,\n" +
                        "\"listType\": \"text\",\n" +
                        "\"type\": \"list\"\n" +
                    "},\n" +
                    "\"sourceId\": \"" + sourceId + "\",\n" +
                    "\"studentScopeURN\": \"" + studentScopeURN + "\"\n" +
                "},\n" +
                "\"context\": {\n" +
                    "\"studentScopeURN\": \"" + studentScopeURN + "\",\n" +
                    "\"sourceId\": \"" + sourceId + "\",\n" +
                    "\"schemaProperty\": {\n" +
                        "\"type\": \""+type+"\"\n" +
                    "},\n" +
                    "\"dataType\": \""+dataType.name()+"\",\n" +
                    "\"operator\": \""+mutationOperator.name()+"\",\n" +
                    "\"context\": [\n" +
                    "\"selection\"\n" +
                    "],\n" +
                    "\"value\": null\n" +
                "}\n" +
                "}";
    }

    public static String createChangeScopeActionWithScopeResolverNewEditable(String studentScopeURN, String sourceId, String type,
                                                                  DataType dataType) {
        return "{\n" +
                "\"action\": \"CHANGE_SCOPE\",\n" +
                "\"resolver\": {\n" +
                    "\"type\": \"SCOPE\",\n" +
                    "\"category\": \"responses\",\n" +
                    "\"context\": [\n" +
                        "\"selection\"\n" +
                    "],\n" +
                    "\"schemaProperty\": {\n" +
                        "\"label\": \"enabled\",\n" +
                        "\"learnerEditable\": true,\n" +
                        "\"type\": \"boolean\",\n" +
                        "\"default\": true\n" +
                    "},\n" +
                    "\"sourceId\": \"" + sourceId + "\",\n" +
                    "\"studentScopeURN\": \"" + studentScopeURN + "\"\n" +
                "},\n" +
                "\"context\": {\n" +
                    "\"studentScopeURN\": \"" + studentScopeURN + "\",\n" +
                    "\"sourceId\": \"" + sourceId + "\",\n" +
                    "\"schemaProperty\": {\n" +
                        "\"type\": \""+type+"\"\n" +
                    "},\n" +
                    "\"dataType\": \""+dataType.name()+"\",\n" +
                    "\"operator\": \"SET\",\n" +
                    "\"context\": [\n" +
                        "\"enabled\"\n" +
                    "],\n" +
                    "\"value\": true\n" +
                "}\n" +
                "}";
    }

    public static String createChangeScoreAction(String elementId, String elementType, double score, String operator) {
        return "{\n" +
                "    \"action\": \"CHANGE_SCORE\",\n" +
                "    \"resolver\": {\n" +
                "        \"type\": \"LITERAL\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"elementId\": \"" + elementId + "\",\n" +
                "        \"elementType\": \"" + elementType + "\",\n" +
                "        \"value\": " + score + ",\n" +
                "        \"operator\": \"" + operator + "\"\n" +
                "    }\n" +
                "}";
    }

    public static String createGradePassbackAction(String elementId, String elementType, double score, String operator) {
        return "{\n" +
                "    \"action\": \"GRADE_PASSBACK\",\n" +
                "    \"resolver\": {\n" +
                "        \"type\": \"LITERAL\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"elementId\": \"" + elementId + "\",\n" +
                "        \"elementType\": \"" + elementType + "\",\n" +
                "        \"value\": " + score + ",\n" +
                "        \"operator\": \"" + operator + "\"\n" +
                "    }\n" +
                "}";
    }

    public static String createSendFeedbackAction(@Nonnull final String value) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode contextNode = mapper.createObjectNode();
        contextNode.put("value", value);

        ObjectNode node = (ObjectNode) mapper.createObjectNode()
                .put("action", Action.Type.SEND_FEEDBACK.name())
                .set("resolver", mapper.createObjectNode().put("type", Resolver.Type.LITERAL.name()));
        node.set("context", contextNode);
        return node.toString();
    }

    public static String createSetCompetencyAction(@Nonnull final String documentId, @Nonnull final String documentItemId, @Nonnull Integer value) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode contextNode = mapper.createObjectNode();
        contextNode.put("documentId", documentId);
        contextNode.put("documentItemId", documentItemId);
        contextNode.put("value", value);

        ObjectNode node = (ObjectNode) mapper.createObjectNode()
                .put("action", Action.Type.SET_COMPETENCY.name())
                .set("resolver", mapper.createObjectNode().put("type", Resolver.Type.LITERAL.name()));
        node.set("context", contextNode);
        return node.toString();
    }

    public static String createUnsupportedAction(String type) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode contextNode = mapper.createObjectNode();

        ObjectNode node = (ObjectNode) mapper.createObjectNode()
                .put("action", type)
                .set("resolver", mapper.createObjectNode().put("type", Resolver.Type.LITERAL.name()));
        node.set("context", contextNode);
        return node.toString();
    }

}
