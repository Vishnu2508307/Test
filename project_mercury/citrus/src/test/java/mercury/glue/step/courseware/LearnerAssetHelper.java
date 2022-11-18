package mercury.glue.step.courseware;

public class LearnerAssetHelper {

    public static String queryPublishedActivityAssets(final String cohortId, final String deploymentId,
                                                      final String activityId) {
        return "query {\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity(activityId:\"" + activityId + "\") {\n" +
                "          assets {\n" +
                "            edges {\n" +
                "              node {\n" +
                "                asset {\n" +
                "                  id\n" +
                "                  assetProvider\n" +
                "                }\n" +
                "                metadata\n" +
                "                source\n" +
                "                urn\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
