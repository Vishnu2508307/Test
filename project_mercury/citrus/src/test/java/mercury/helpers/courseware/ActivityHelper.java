package mercury.helpers.courseware;

import java.util.Map;
import java.util.function.BiConsumer;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.smartsparrow.courseware.payload.ActivityPayload;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class ActivityHelper {

    public static String createChildActivity(String pluginId, String pluginVersion, String parentPathwayId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.activity.create");
        pb.addField("parentPathwayId", parentPathwayId);
        pb.addField("pluginId", pluginId);
        pb.addField("pluginVersion", pluginVersion);
        return pb.build();
    }

    public static String createChildActivityWithId(String pluginId, String pluginVersion, String parentPathwayId, String activityId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.activity.create");
        pb.addField("parentPathwayId", parentPathwayId);
        pb.addField("pluginId", pluginId);
        pb.addField("pluginVersion", pluginVersion);
        pb.addField("activityId", activityId);
        return pb.build();
    }

    public static String duplicateActivityIntoPathway(String activityId, String pathwayId, Integer index, Boolean newDuplicateFlow) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.activity.duplicate");
        pb.addField("activityId", activityId);
        pb.addField("parentPathwayId", pathwayId);
        if (index != null && index != -1) {
            pb.addField("index", index);
        }

        if(newDuplicateFlow != null){
            pb.addField("newDuplicateFlow", newDuplicateFlow);
        }

        return pb.build();
    }

    public static String moveActivityToPathway(String activityId, String pathwayId, Integer index) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.activity.move");
        pb.addField("activityId", activityId);
        pb.addField("pathwayId", pathwayId);
        pb.addField("index", index);
        return pb.build();
    }

    public static String deleteChildActivity(String activityId, String parentPathwayId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.activity.delete");
        pb.addField("activityId", activityId);
        pb.addField("parentPathwayId", parentPathwayId);
        return pb.build();
    }

    public static String activityOk(String type, String activityId, String pluginId, String creatorEmail,
                                    String creatorId) {
        return "{" +
                    "\"type\":\"" + type + "\"," +
                    "\"response\":{" +
                        "\"activity\":{" +
                            "\"activityId\":\"" + activityId + "\"," +
                            "\"plugin\":{" +
                                "\"pluginId\":\"" + pluginId + "\"," +
                                "\"name\":\"Course Citrus plugin\"," +
                                "\"type\":\"course\"," +
                                "\"version\":\"1.*\"," +
                                "\"pluginFilters\":[{" +
                                       "\"pluginId\":\"" + pluginId + "\"," +
                                       "\"version\":\"1.2.0\"," +
                                       "\"filterType\":\"ID\"," +
                                       "\"filterValues\":\"@notEmpty()@\"" +
                                 "}]" +
                            "}," +
                        "\"creator\":{" +
                            "\"accountId\":\"" + creatorId + "\"," +
                            "\"subscriptionId\":\"@notEmpty()@\"," +
                            "\"iamRegion\":\"GLOBAL\"," +
                            "\"primaryEmail\":\"" + creatorEmail + "\"," +
                            "\"roles\":\"@notEmpty()@\"," +
                            "\"email\":\"@notEmpty()@\"," +
                            "\"authenticationType\":\"@notEmpty()@\"" +
                        "}," +
                        "\"createdAt\":\"@notEmpty()@\"," +
                        "\"themePayload\":\"@notEmpty()@\"," +
                        "\"studentScopeURN\":\"@notEmpty()@\"" +
                        "}" +
                    "}," +
                    "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String createChildActivityOk(String type, String activityId, String pluginId, String creatorEmail,
                                               String creatorId, String parentPathwayId) {
        return "{" +
                    "\"type\":\"" + type + "\"," +
                    "\"response\":{" +
                        "\"activity\":{" +
                            "\"activityId\":\"" + activityId + "\"," +
                            "\"plugin\":{" +
                                "\"pluginId\":\"" + pluginId + "\"," +
                                "\"name\":\"Course Citrus plugin\"," +
                                "\"type\":\"course\"," +
                                "\"version\":\"1.*\"," +
                                "\"pluginFilters\":[{" +
                                      "\"pluginId\":\"" + pluginId + "\"," +
                                      "\"version\":\"1.2.0\"," +
                                      "\"filterType\":\"ID\"," +
                                      "\"filterValues\":\"@notEmpty()@\"" +
                                 "}]" +
                            "}," +
                            "\"creator\":{" +
                                "\"accountId\":\"" + creatorId + "\"," +
                                "\"subscriptionId\":\"@notEmpty()@\"," +
                                "\"iamRegion\":\"GLOBAL\"," +
                                "\"primaryEmail\":\"" + creatorEmail + "\"," +
                                "\"roles\":\"@notEmpty()@\"," +
                                "\"email\":\"@notEmpty()@\"," +
                                "\"authenticationType\":\"@notEmpty()@\"" +
                            "}," +
                     "\"createdAt\":\"@notEmpty()@\"," +
                            "\"parentPathwayId\":\"" + parentPathwayId + "\"," +
                            "\"themePayload\":\"@notEmpty()@\"," +
                            "\"studentScopeURN\":\"@notEmpty()@\"" +
                        "}" +
                    "}," +
                    "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String createChildActivityOkWithPluginName(String type, String activityId, String pluginId, String creatorEmail,
                                               String creatorId, String parentPathwayId,String plunginName,String pluginVersion) {

        return "{" +
                "\"type\":\"" + type + "\"," +
                "\"response\":{" +
                "\"activity\":{" +
                "\"activityId\":\"" + activityId + "\"," +
                "\"plugin\":{" +
                "\"pluginId\":\"" + pluginId + "\"," +
                "\"name\":\"" + plunginName + "\"," +
                "\"type\":\"@notEmpty()@\"," +
                "\"version\":\"" +  pluginVersion + "\"," +
                "\"pluginFilters\":\"@notEmpty()@\"" +
                "}," +
                "\"creator\":{" +
                "\"accountId\":\"" + creatorId + "\"," +
                "\"subscriptionId\":\"@notEmpty()@\"," +
                "\"iamRegion\":\"GLOBAL\"," +
                "\"primaryEmail\":\"" + creatorEmail + "\"," +
                "\"roles\":\"@notEmpty()@\"," +
                "\"email\":\"@notEmpty()@\"," +
                "\"authenticationType\":\"@notEmpty()@\"" +
                "}," +
                "\"createdAt\":\"@notEmpty()@\"," +
                "\"parentPathwayId\":\"" + parentPathwayId + "\"," +
                "\"themePayload\":\"@notEmpty()@\"," +
                "\"studentScopeURN\":\"@notEmpty()@\"" +
                "}" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String deleteChildActivityOk(String activityId) {
        return "{" +
                "\"type\":\"author.activity.delete.ok\"," +
                "\"response\":{" +
                    "\"activityId\":\"" + activityId + "\"," +
                    "\"parentPathwayId\":\"@notEmpty()@\"" +
                "},\"replyTo\":\"@notEmpty()@\"}";
    }

    public static ValidationCallback activityDuplicateOk(BiConsumer<ActivityPayload, TestContext> consumer) {
        return activityResponseOk(consumer, "activity", "author.activity.duplicate.ok");
    }

    public static ValidationCallback activityMoveOk(BiConsumer<ActivityPayload, TestContext> consumer) {
        return activityResponseOk(consumer, "activity", "author.activity.move.ok");
    }

    public static String activityDuplicateError(int code, String message) {
        return "{" +
                "\"type\":\"author.activity.duplicate.error\"," +
                "\"code\":" + code + "," +
                "\"message\":\"" + message + "\"," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static ValidationCallback activityResponseOk(BiConsumer<ActivityPayload, TestContext> consumer,
                                                        String rootElement, String type) {
        return new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
            @Override
            public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return rootElement;
            }

            @Override
            public String getType() {
                return type;
            }
        };
    }

    public static String getActivityRequest(String activityId) {
        return new PayloadBuilder()
                .addField("type", "author.activity.get")
                .addField("activityId", activityId)
                .build();
    }

    public static String getProgressQuery(String cohortId, String deploymentId, String activityId) {
        return "query {\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity(activityId: \"" + activityId + "\") {\n" +
                "          progress {\n" +
                "            attemptId\n" +
                "            evaluationId\n" +
                "            id\n" +
                "            completion {\n" +
                "              completed\n" +
                "              confidence\n" +
                "              value\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

}
