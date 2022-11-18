package mercury.helpers.courseware;

public class AncestryHelper {

    public static String getCoursewareAncestry(String workspaceId, String elementId) {
        return "query {\n" +
                "  workspace(workspaceId: \"" + workspaceId + "\") {\n" +
                "    getCoursewareAncestry(elementId: \"" + elementId + "\") {\n" +
                "      elementId\n" +
                "      type\n" +
                "      ancestry {\n" +
                "        elementId\n" +
                "        elementType\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getLearnerAncestry(String cohortId, String deploymentId, String elementId) {
        return "query {\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        getLearnerAncestry(elementId: \"" + elementId + "\") {\n" +
                "          elementId\n" +
                "          type\n" +
                "          ancestry {\n" +
                "            elementId\n" +
                "            elementType\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
