package mercury.helpers.learner;

public class ScoreHelper {

    public static String fetchRootActivityScore(final String cohortId, final String deploymentId, final String activityId) {
        return "{\n" +
                "    learn {\n" +
                "        cohort(cohortId: \"" + cohortId + "\") {\n" +
                "            deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "                id\n" +
                "                activity(activityId: \"" + activityId + "\") {\n" +
                "                    score {\n" +
                "                        value,\n" +
                "                        reason\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    public static String fetchPathwayScore(final String cohortId, final String deploymentId, final String pathwayId) {
        return "{\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity {\n" +
                "          pathways(pathwayId: \"" + pathwayId + "\") {\n" +
                "            score {\n" +
                "              reason\n" +
                "              value\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchWalkableScore(final String cohortId, final String deploymentId, final String pathwayId) {
        return "{\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        activity {\n" +
                "          pathways(pathwayId: \"" + pathwayId + "\") {\n" +
                "            id\n" +
                "            walkables {\n" +
                "              edges {\n" +
                "                node {\n" +
                "                  score {\n" +
                "                    reason\n" +
                "                    value\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchInteractiveScore(final String cohortId, final String deploymentId,
                                               final String interactiveId, final String accountId) {

        return "query {\n" +
                "  cohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollmentByStudent(studentId: \"" + accountId + "\") {\n" +
                "      interactiveScore(deploymentId: \"" + deploymentId + "\", interactiveId: \"" + interactiveId + "\") {\n" +
                "        reason\n" +
                "        value\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

    }
}
