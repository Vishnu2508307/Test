package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.consol.citrus.context.TestContext;
import com.smartsparrow.courseware.payload.FeedbackPayload;

import mercury.common.ResponseMessageValidationCallback;

public class FeedbackHelper {

    //TODO: change it to static methods

    static final String CREATE_MESSAGE = "{" +
                "   \"type\": \"author.interactive.feedback.create\"," +
                "   \"interactiveId\" : \"%s\"," +
                "   \"pluginId\" : \"%s\"," +
                "   \"pluginVersion\" : \"1.*\"" +
                "}";

    static final String FEEDBACK_DTO =
                "      \"feedbackId\":\"%s\"," +
                "      \"interactiveId\":\"%s\"," +
                "      \"plugin\": {" +
                "           \"pluginId\":\"${" + PLUGIN_ID_VAR + "}\"," +
                "           \"version\":\"1.*\"," +
                "           \"type\":\"course\"," +
                "           \"name\":\"Course Citrus plugin\"," +
                "            \"pluginFilters\":[{" +
                "                    \"pluginId\":\"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "                    \"version\":\"1.2.0\"," +
                "                      \"filterType\":\"ID\"," +
                "                       \"filterValues\":\"@notEmpty()@\"" +
                "                         }]" +
                "       }";

    static final String FEEDBACK_DTO_WITH_CONFIG = FEEDBACK_DTO + ",\"config\": \"%s\"";

    static final String CREATE_OK_RESPONSE = "{" +
            "  \"type\": \"author.interactive.feedback.create.ok\"," +
            "  \"response\": {" +
            "    \"feedback\":{" +
            FEEDBACK_DTO +
            "     }" +
            "  }" +
            "}";

    static final String CREATE_ERROR_RESPONSE = "{" +
            "    \"type\": \"author.interactive.feedback.create.error\"," +
            "    \"code\": %s," +
            "    \"message\": \"%s\"" +
            "}";

    static final String REPLACE_MESSAGE = "{" +
            "  \"type\": \"author.interactive.feedback.replace\"," +
            "  \"feedbackId\": \"${%s}\"," +
            "  \"config\": \"%s\"" +
            "}";

    static final String REPLACE_OK_RESPONSE = "{" +
            "  \"type\": \"author.interactive.feedback.replace.ok\"," +
            "  \"response\": {" +
            "    \"feedbackConfig\": {" +
            "      \"id\": \"@notEmpty()@\"," +
            "      \"feedbackId\": \"${%s}\"," +
            "      \"config\" : \"%s\"" +
            "    }" +
            "  }" +
            "}";

    static final String REPLACE_ERROR_RESPONSE = "{" +
            "  \"type\": \"author.interactive.feedback.replace.error\"," +
            "  \"code\": %s," +
            "  \"message\": \"%s\"" +
            "}";

    static final String GET_MESSAGE = "{" +
            "    \"type\": \"author.interactive.feedback.get\"," +
            "    \"feedbackId\" : \"${%s}\"" +
            "}";

    static final Function<String, String> GET_OK_RESPONSE = config -> "{" +
            "  \"type\": \"author.interactive.feedback.get.ok\"," +
            "  \"response\": {" +
            "    \"feedback\":{" +
            (config == null ? FEEDBACK_DTO : FEEDBACK_DTO_WITH_CONFIG) +
            "     }" +
            "  }" +
            "}";

    static final String GET_ERROR_RESPONSE = "{" +
            "  \"type\": \"author.interactive.feedback.get.error\"," +
            "  \"code\": %s," +
            "  \"message\": \"%s\"" +
            "}";

    static final String DELETE_MESSAGE = "{" +
            "    \"type\": \"author.interactive.feedback.delete\"," +
            "    \"feedbackId\" : \"${%s}\"," +
            "    \"interactiveId\" : \"${%s}\"" +
            "}";

    static final String DELETE_OK_RESPONSE = "{" +
            "  \"type\": \"author.interactive.feedback.delete.ok\"," +
            "  \"response\": {" +
            "    \"feedbackId\":\"${%s}\"," +
            "    \"interactiveId\":\"${%s}\"" +
            "  }" +
            "}";

    static final String DELETE_ERROR_RESPONSE = "{" +
            "    \"type\": \"author.interactive.feedback.delete.error\"," +
            "    \"code\": %s," +
            "    \"message\": \"%s\"" +
            "}";

    public static ResponseMessageValidationCallback validateFeedbackGetResponse(
            BiConsumer<FeedbackPayload, TestContext> consumer) {
        return new ResponseMessageValidationCallback<FeedbackPayload>(FeedbackPayload.class) {
            @Override
            public void validate(FeedbackPayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return "feedback";
            }

            @Override
            public String getType() {
                return "author.interactive.feedback.get.ok";
            }
        };
    }
}
