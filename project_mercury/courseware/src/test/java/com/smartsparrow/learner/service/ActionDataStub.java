package com.smartsparrow.learner.service;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;

import javax.annotation.Nullable;

import org.assertj.core.util.Strings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.resolver.Resolver;

public class ActionDataStub {

    public static String progressActionInteractiveCompleteJson() {
        return progressActionJson(ProgressionType.INTERACTIVE_COMPLETE);
    }

    public static String progressActionInteractiveRepeatJson() {
        return progressActionJson(ProgressionType.INTERACTIVE_REPEAT);
    }

    public static String progressActionJson(final ProgressionType progressionType) {
        return "  {\n"
                + "    \"action\": \"CHANGE_PROGRESS\",\n"
                + "    \"resolver\": {\n"
                + "      \"type\": \"LITERAL\"\n"
                + "    },\n"
                + "    \"context\": {\n"
                + "      \"progressionType\": \"" + progressionType.name() + "\"\n"
                + "    }\n"
                + "  }\n";
    }

    public static String feedbackActionLiteralJson() {
        return "  {\n"
                + "    \"action\": \"SEND_FEEDBACK\",\n"
                + "    \"resolver\": {\n"
                + "      \"type\": \"LITERAL\"\n"
                + "    },\n"
                + "    \"context\": {\n"
                + "      \"value\": \"This is a feedback\""
                + "    }\n"
                + "  }\n";
    }

    public static String changeCompetencyActionLiteralJson(String operator, Float value) {
        // note action type is deprecated SET_COMPETENCY instead of newer CHANGE_COMPETENCY. Tests should assert that
        // the newer type was deserialized
        String operatorField = !Strings.isNullOrEmpty(operator) ? "\"operator\": \"" + operator + "\"," : "";
        return "  {\n"
                + "\"action\": \"SET_COMPETENCY\",\n"
                + "\"resolver\": {\n"
                + "\"type\": \"LITERAL\"\n"
                + "},\n"
                + "\"context\": {\n"
                + "\"documentId\": \"" + DOCUMENT_ID + "\","
                + "\"documentItemId\": \"" + ITEM_A_ID + "\","
                +  operatorField
                + "\"value\": "+ value
                + "}\n"
                + "}";
    }

    public static String changeScopeActionLiteralResolver(String studentScopeURN, String sourceId) {
        return "{\n" +
                "\"action\": \"CHANGE_SCOPE\",\n" +
                "\"resolver\": {\n" +
                "\"type\": \"LITERAL\"\n" +
                "},\n" +
                "\"context\": {\n" +
                "\"studentScopeURN\": \"" + studentScopeURN + "\",\n" +
                "\"sourceId\": \"" + sourceId + "\",\n" +
                "\"schemaProperty\": {\n" +
                "\"type\":\"list\"\n" +
                "},\n" +
                "\"dataType\": \"STRING\",\n" +
                "\"operator\": \"SET\",\n" +
                "\"context\": [\n" +
                "\"selection\"\n" +
                "],\n" +
                "\"value\": \"a\"\n" +
                "}\n" +
                "}";
    }

    public static String changeScopeActionScopeResolver(String sourceId, String studentScopeURN) {
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
                "\"type\": \"list\"\n" +
                "},\n" +
                "\"dataType\": \"STRING\",\n" +
                "\"operator\": \"SET\",\n" +
                "\"context\": [\n" +
                "\"selection\"\n" +
                "],\n" +
                "\"value\": null\n" +
                "}\n" +
                "}";
    }

    public static String createChangeProgressAction(ProgressionType option, @Nullable String coursewareElementId) {
        ObjectMapper mapper = new ObjectMapper();


        ObjectNode contextNode = mapper.createObjectNode().put("progressionType", option.name());
        if (!Strings.isNullOrEmpty(coursewareElementId)) {
            contextNode.put("coursewareElementId", coursewareElementId);
        }

        ObjectNode node = (ObjectNode) mapper.createObjectNode()
                .put("action", Action.Type.CHANGE_PROGRESS.name())
                .set("resolver", mapper.createObjectNode().put("type", Resolver.Type.LITERAL.name()));
        node.set("context", contextNode);
        return node.toString();

    }

}
