package mercury.helpers.courseware;

import mercury.common.PayloadBuilder;

public class CoursewarePublishHelper {

    public static String getProjectActivityPublishRequest(String activityId, String cohortId) {
        return new PayloadBuilder()
                .addField("type", "project.activity.publish")
                .addField("activityId", activityId)
                .addField("cohortId", cohortId)
                .build();
    }

    public static String getProjectActivityPublishRequest(String activityId, String cohortId, boolean lockPluginVersionEnabled) {
        return new PayloadBuilder()
                .addField("type", "project.activity.publish")
                .addField("activityId", activityId)
                .addField("cohortId", cohortId)
                .addField("lockPluginVersionEnabled", lockPluginVersionEnabled)
                .build();
    }

    public static String getProjectActivityPublishUpdateRequest(String activityId, String deploymentId, String cohortId) {
        return new PayloadBuilder()
                .addField("type", "project.activity.publish")
                .addField("activityId", activityId)
                .addField("deploymentId", deploymentId)
                .addField("cohortId", cohortId)
                .build();
    }

    public static String getProjectActivityPublishErrorResponse(int statusCode) {
        return "{" +
                "\"type\":\"project.activity.publish.error\"," +
                "\"code\":" + statusCode + "," +
                "\"message\":\"@notEmpty()@\"," +
                "\"replyTo\":\"@notEmpty()@\"}";
    }
}
