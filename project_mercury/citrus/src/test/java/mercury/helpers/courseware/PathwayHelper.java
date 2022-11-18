package mercury.helpers.courseware;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.consol.citrus.context.TestContext;
import com.smartsparrow.courseware.payload.PathwayPayload;

import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class PathwayHelper {

    public static String createPathway(String activityId, String pathwayType) {
        return createPathway(activityId, pathwayType, null);
    }

    public static String createPathway(String activityId, String pathwayType, String config) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.pathway.create");
        pb.addField("activityId", activityId);
        pb.addField("pathwayType", pathwayType);
        pb.addField("config", config);
        return pb.build();
    }

    public static String createPathway(String activityId, String pathwayType, String config, UUID pathwayId) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.pathway.create");
        pb.addField("activityId", activityId);
        pb.addField("pathwayType", pathwayType);
        pb.addField("config", config);
        pb.addField("pathwayId", pathwayId);
        return pb.build();
    }

    public static String replaceConfig(String pathwayId, String config) {
        return new PayloadBuilder()
                .addField("type", "author.pathway.config.replace")
                .addField("pathwayId", pathwayId)
                .addField("config", config)
                .build();
    }

    public static String createPathwayOk(String pathwayId, String activityId, String pathwayType) {
        return "{" +
                    "\"type\":\"author.pathway.create.ok\"," +
                    "\"response\":{" +
                        "\"pathway\":{" +
                            "\"pathwayId\":\"" + pathwayId + "\"," +
                            "\"pathwayType\":\"" + pathwayType + "\"," +
                            "\"parentActivityId\":\"" + activityId + "\"" +
                        "}" +
                    "}," +
                    "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String createPathwayError(int code, String message) {
        return "{" +
                "\"type\": \"author.pathway.create.error\"," +
                "\"code\": " + code + "," +
                "\"message\": \"" + message + "\"," +
                "\"replyTo\": \"@notEmpty()@\"" +
                "}";
    }

    public static String getPathway(String pathwayId) {
        return new PayloadBuilder()
                .addField("type","author.pathway.get")
                .addField("pathwayId", pathwayId)
                .build();
    }

    /**
     * Returns a response message validation callback that will be executed as a verification action on message received
     *
     * @param consumer a {@link BiConsumer} that accepts a {@link PathwayPayload} and {@link TestContext} as argument
     * @return a response message validation callback
     */
    public static ResponseMessageValidationCallback validatePathwayPayload(BiConsumer<PathwayPayload, TestContext> consumer) {
        return new ResponseMessageValidationCallback<PathwayPayload>(PathwayPayload.class) {
            @Override
            public void validate(PathwayPayload payload, Map<String, Object> headers, TestContext context) {
                consumer.accept(payload, context);
            }

            @Override
            public String getRootElementName() {
                return "pathway";
            }

            @Override
            public String getType() {
                return "author.pathway.get.ok";
            }
        };
    }

    public static String deletePathway(String pathwayId, String parentActivityId) {
        return new PayloadBuilder()
                .addField("type", "author.pathway.delete")
                .addField("pathwayId", pathwayId)
                .addField("parentActivityId", parentActivityId)
                .build();
    }

    public static String deletePathwayOk(String pathwayId) {
        return "{" +
                    "\"type\":\"author.pathway.delete.ok\"," +
                    "\"response\":{" +
                        "\"parentActivityId\":\"@notEmpty()@\"," +
                        "\"pathwayId\":\"" + pathwayId + "\"" +
                    "},\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String deletePathwayError(int code, String message) {
        return "{" +
                "  \"type\": \"author.pathway.delete.error\"," +
                "  \"code\": " + code + "," +
                "  \"message\": \"" + message + "\"," +
                "  \"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    public static String getPathwayErrorResponse(int code, String errorMessage) {
        return "{" +
                    "\"type\":\"author.pathway.get.error\"," +
                    "\"code\":"+code+"," +
                    "\"message\":\""+errorMessage+"\"," +
                    "\"replyTo\":\"@notEmpty()@\"}";
    }

    public static String fetchWalkablesGraphQlQuery(String cohortId, String deploymentId) {
        return "    {\n" +
                "      learn {\n" +
                "        cohort(cohortId: \"" + cohortId + "\") {\n" +
                "          deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "            activity {\n" +
                "              pathways {\n" +
                "                id\n" +
                "                walkables {\n" +
                "                  edges {\n" +
                "                    node {\n" +
                "                      id\n" +
                "                      elementType\n" +
                "                      studentScopeURN\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }";
    }

    public static String fetchWalkablesForPathwayGraphQlQuery(String cohortId, String deploymentId, String activityId, String pathwayId) {
        return "    {\n" +
                "      learn {\n" +
                "        cohort(cohortId: \"" + cohortId + "\") {\n" +
                "          deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "            activity(activityId: \"" + activityId + "\")  {\n" +
                "              pathways(pathwayId: \"" + pathwayId + "\")  {\n" +
                "                walkables {\n" +
                "                  edges {\n" +
                "                    node {\n" +
                "                      id\n" +
                "                      elementType\n" +
                "                      studentScopeURN\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }";
    }

    public static String createPathwayWithPreload(String activityId, String pathwayType, String config, String preloadPathway) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.pathway.create");
        pb.addField("activityId", activityId);
        pb.addField("pathwayType", pathwayType);
        pb.addField("config", config);
        pb.addField("preloadPathway", preloadPathway);
        return pb.build();
    }

    public static String createPathwayWithPreload(String activityId, String pathwayType, String config, UUID pathwayId, String preloadPathway) {
        PayloadBuilder pb = new PayloadBuilder();
        pb.addField("type", "author.pathway.create");
        pb.addField("activityId", activityId);
        pb.addField("pathwayType", pathwayType);
        pb.addField("config", config);
        pb.addField("pathwayId", pathwayId);
        pb.addField("preloadPathway", preloadPathway);
        return pb.build();
    }
}
