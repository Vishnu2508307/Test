package com.smartsparrow.eval.deserializer;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.eval.action.progress.ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.smartsparrow.courseware.lang.ActionConditionParserFault;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.action.feedback.SendFeedbackAction;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.scope.ChangeScopeActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.service.ActionDataStub;
import com.smartsparrow.util.DataType;

class ActionDeserializerTest {

    private ActionDeserializer actionDeserializer;

    private ObjectMapper mapper = new ObjectMapper();
    private UUID coursewareElementID = UUIDs.timeBased();
    private static final String actions = "[" +
            "{" +
                "\"action\":\"CHANGE_PROGRESS\"," +
                "\"resolver\":{" +
                    "\"type\":\"LITERAL\"" +
                "}," +
                "\"context\":{" +
                    "\"progressionType\":\"INTERACTIVE_COMPLETE\"" +
                "}" +
            "}, {" +
                "\"action\": \"CHANGE_SCORE\"," +
                "\"resolver\": {" +
                    "\"type\": \"LITERAL\"" +
                "}," +
                "\"context\": {" +
                    "\"elementId\": \"f38a2a40-543c-11e9-9124-ffa2146f2d13\"," +
                    "\"elementType\": \"INTERACTIVE\"," +
                    "\"operator\": \"ADD\"," +
                    "\"value\": 5.5" +
                "}" +
            "}, {" +
                "\"action\":\"GRADE\"," +
                "\"resolver\":{" +
                    "\"type\":\"LITERAL\"" +
                "},\"context\":{}" +
            "}, {" +
                "\"action\":\"FUBAR\"," +
                "\"resolver\":{" +
                    "\"type\":\"LITERAL\"" +
                "},\"context\":{}" +
            "}, {" +
                "\"action\":\"SEND_FEEDBACK\"," +
                "\"resolver\":{" +
                    "\"type\":\"LITERAL\"" +
                "}," +
                "\"context\":{" +
                    "\"value\":\"Well done mate!\"" +
                "}" +
            "}, {" +
                "\"action\":\"CHANGE_COMPETENCY\"," +
                "\"resolver\":{" +
                    "\"type\":\"LITERAL\"" +
                "}," +
                "\"context\":{" +
                    "\"documentId\":\"f38a2a40-543c-11e9-9124-ffa2146f2d13\"," +
                    "\"documentItemId\":\"5eee65f6-3fd5-48cf-8db4-1acb287049cf\"," +
                    "\"value\":1" +
                "}" +
            "}]";

    @BeforeEach
    void setup() {
        SimpleModule module = new SimpleModule();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        module.addDeserializer(ResolverContext.class, new ResolverDeserializer(ResolverContext.class));
        mapper.registerModule(module);

        actionDeserializer = new ActionDeserializer();
    }

    @Test
    void deserializeActions() {
        List<Action> deserialized = actionDeserializer.reactiveDeserialize(actions).block();

        assertNotNull(deserialized);
        assertEquals(6, deserialized.size());

        assertEquals(Action.Type.CHANGE_PROGRESS, deserialized.get(0).getType());
        assertEquals(Action.Type.CHANGE_SCORE, deserialized.get(1).getType());
        assertEquals(Action.Type.UNSUPPORTED_ACTION, deserialized.get(2).getType());
        assertEquals(Action.Type.UNSUPPORTED_ACTION, deserialized.get(3).getType());
        assertEquals(Action.Type.SEND_FEEDBACK, deserialized.get(4).getType());
        assertEquals(Action.Type.CHANGE_COMPETENCY, deserialized.get(5).getType());
    }

    @Test
    void deserializeActions_invalid() {
        ActionConditionParserFault e = assertThrows(ActionConditionParserFault.class,
                () -> actionDeserializer.reactiveDeserialize("[{\"invalid\":\"option\"}]").block());

        assertNotNull(e);
        assertNotNull(e.getMessage());
    }

    @Test
    void deserialize_changeProgress_success() {

        Arrays.stream(ProgressionType.values()).forEach(progressionType -> {
            String json = ActionDataStub.createChangeProgressAction(progressionType, coursewareElementID.toString());
            try {
                ProgressAction action = (ProgressAction) mapper.readValue(json, Action.class);
                assertEquals(Action.Type.CHANGE_PROGRESS, action.getType());
                assertEquals(Resolver.Type.LITERAL, action.getResolver().getType());
                assertEquals(progressionType, action.getContext().getProgressionType());
                assertEquals(coursewareElementID, action.getContext().getElementId());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    @Test
    void deserialize_changeProgress_nullCoursewareElementId() {
        String json = ActionDataStub.createChangeProgressAction(INTERACTIVE_COMPLETE_AND_GO_TO, null);
        try {
            ProgressAction action = (ProgressAction) mapper.readValue(json, Action.class);
            assertEquals(Action.Type.CHANGE_PROGRESS, action.getType());
            assertEquals(Resolver.Type.LITERAL, action.getResolver().getType());
            assertEquals(ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO, action.getContext().getProgressionType());
            assertNull(action.getContext().getElementId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void deserilize_nullActionType() {
        String json =
                "  {\n"
                        + "    \"resolver\": {\n"
                        + "      \"type\": \"LITERAL\"\n"
                        + "    }\n"
                        + "  }";

        assertThrows(IllegalArgumentFault.class, () -> mapper.readValue(json, Action.class));
    }

    @Test
    void deserilize_nullContext() {
        String json =
                  "  {\n"
                + "    \"action\": \"CHANGE_PROGRESS\",\n"
                + "    \"resolver\": {\n"
                + "      \"type\": \"LITERAL\"\n"
                + "    }\n"
                + "  }";

        assertThrows(IllegalArgumentFault.class, () -> mapper.readValue(json, Action.class));
    }

    @Test
    void deserilize_sendFeedback() throws IOException {
        String json = ActionDataStub.feedbackActionLiteralJson();

        SendFeedbackAction feedbackAction = (SendFeedbackAction) mapper.readValue(json, Action.class);

        assertNotNull(feedbackAction);
        assertEquals("This is a feedback", feedbackAction.getContext().getValue());
        assertEquals(Action.Type.SEND_FEEDBACK, feedbackAction.getType());
        assertEquals(Resolver.Type.LITERAL, feedbackAction.getResolver().getType());
    }

    @Test
    void deserialize_changeCompetencyMet_setOperator() throws IOException {
        ChangeCompetencyMetAction competencyMetAction = changeCompetencyCommonTest(MutationOperator.SET.name());
        assertEquals(MutationOperator.SET, competencyMetAction.getContext().getOperator());
    }

    @Test
    void deserialize_changeCompetencyMet_addOperator() throws IOException {
        ChangeCompetencyMetAction competencyMetAction = changeCompetencyCommonTest(MutationOperator.ADD.name());
        assertEquals(MutationOperator.ADD, competencyMetAction.getContext().getOperator());
    }

    @Test
    void deserialize_changeCompetencyMet_RemoveOperator() throws IOException {
        ChangeCompetencyMetAction competencyMetAction = changeCompetencyCommonTest(MutationOperator.REMOVE.name());
        assertEquals(MutationOperator.REMOVE, competencyMetAction.getContext().getOperator());
    }

    @Test
    void deserialize_changeCompetencyMet_UnknownOperator() throws IOException {
        ChangeCompetencyMetAction competencyMetAction = changeCompetencyCommonTest("NOPE LOL");
        assertEquals(MutationOperator.SET, competencyMetAction.getContext().getOperator());
    }

    @Test
    void deserialize_changeCompetencyMet_MissingOperator() throws IOException {
        ChangeCompetencyMetAction competencyMetAction = changeCompetencyCommonTest(null);
        assertEquals(MutationOperator.SET, competencyMetAction.getContext().getOperator());
    }


    private ChangeCompetencyMetAction changeCompetencyCommonTest(String operator) throws IOException {
        String json = ActionDataStub.changeCompetencyActionLiteralJson(operator, 0f);

        ChangeCompetencyMetAction competencyMetAction = (ChangeCompetencyMetAction) mapper
                .readValue(json, Action.class);

        assertNotNull(competencyMetAction);
        ChangeCompetencyMetActionContext context = competencyMetAction.getContext();
        assertNotNull(context);
        assertEquals(DOCUMENT_ID, context.getDocumentId());
        assertEquals(ITEM_A_ID, context.getDocumentItemId());
        assertEquals(Float.valueOf(0), context.getValue());
        assertEquals(Action.Type.CHANGE_COMPETENCY, competencyMetAction.getType());
        assertEquals(Resolver.Type.LITERAL, competencyMetAction.getResolver().getType());
        return competencyMetAction;
    }

    @Test
    void deserialize_changeScopeAction_literalResolver() throws IOException {
        UUID sourceId = UUID.randomUUID();
        UUID studentScopeURN = UUID.randomUUID();
        String jsonAction = ActionDataStub.changeScopeActionLiteralResolver(studentScopeURN.toString(), sourceId.toString());

        ChangeScopeAction action = (ChangeScopeAction) mapper.readValue(jsonAction, Action.class);

        assertNotNull(action);
        ChangeScopeActionContext context = action.getContext();
        assertNotNull(context);
        assertEquals(DataType.STRING, context.getDataType());
        assertEquals(MutationOperator.SET, context.getOperator());
        Map<String, String> schemaProperty = context.getSchemaProperty();
        assertNotNull(schemaProperty);
        assertEquals("list", schemaProperty.get("type"));
        assertEquals(sourceId, context.getSourceId());
        assertEquals(studentScopeURN, context.getStudentScopeURN());
        assertEquals(Action.Type.CHANGE_SCOPE, action.getType());
    }

    @Test
    void deserialize_changeScopeAction_scopeResolver() throws IOException {
        UUID sourceId = UUID.randomUUID();
        UUID studentScopeURN = UUID.randomUUID();
        String jsonAction = ActionDataStub.changeScopeActionScopeResolver(sourceId.toString(), studentScopeURN.toString());

        ChangeScopeAction action = (ChangeScopeAction) mapper.readValue(jsonAction, Action.class);

        assertNotNull(action);
        ChangeScopeActionContext context = action.getContext();
        assertNotNull(context);
        assertEquals(DataType.STRING, context.getDataType());
        assertEquals(MutationOperator.SET, context.getOperator());
        Map<String, String> schemaProperty = context.getSchemaProperty();
        assertNotNull(schemaProperty);
        assertEquals("list", schemaProperty.get("type"));
        assertEquals(sourceId, context.getSourceId());
        assertEquals(studentScopeURN, context.getStudentScopeURN());
        assertEquals(Action.Type.CHANGE_SCOPE, action.getType());

        ScopeContext resolverContext = (ScopeContext) action.getResolver();
        assertNotNull(resolverContext);
        assertEquals(Resolver.Type.SCOPE, resolverContext.getType());
        assertEquals(sourceId, resolverContext.getSourceId());
        assertEquals(studentScopeURN, resolverContext.getStudentScopeURN());
        assertNotNull(resolverContext.getSchemaProperty());
        assertEquals("responses", resolverContext.getCategory());
        assertEquals(1, resolverContext.getContext().size());
    }

}
