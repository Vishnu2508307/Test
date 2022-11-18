package mercury.helpers.learner;

public class LearnerCoursewareHelper {


    public static String getLearnerCourseware(String cohortId, String deploymentId, String elementId) {
        return "query {\n" +
                " learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity {\n" +
                "          id\n" +
                "          getDefaultFirst(elementId: \"" + elementId + "\") {\n" +
                "            theme\n" +
                "            pluginId\n" +
                "            assets {\n" +
                "               edges {\n" +
                "                 node {\n" +
                "                   urn\n" +
                "                   source\n" +
                "                   metadata\n" +
                "                 asset {\n" +
                "                   id\n" +
                "                   assetProvider\n" +
                "                   assetMediaType\n" +
                "                   assetVisibility\n" +
                "             }\n" +
                "             }\n" +
                "             }\n" +
                "           }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getLearnerCoursewareForInteractive(String cohortId, String deploymentId, String elementId) {
        return "query {\n" +
                " learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity {\n" +
                "          id\n" +
                "          getDefaultFirst(elementId: \"" + elementId + "\") {\n" +
                "            pluginId\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String getLearnerCoursewareWithoutElementId(String cohortId, String deploymentId) {
        return "query {\n" +
                " learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity {\n" +
                "          id\n" +
                "          getDefaultFirst {\n" +
                "            theme\n" +
                "            pluginId\n" +
                "            assets {\n" +
                "               edges {\n" +
                "                 node {\n" +
                "                   urn\n" +
                "                   source\n" +
                "                   metadata\n" +
                "                 asset {\n" +
                "                   id\n" +
                "                   assetProvider\n" +
                "                   assetMediaType\n" +
                "                   assetVisibility\n" +
                "             }\n" +
                "             }\n" +
                "             }\n" +
                "           }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
