package mercury.helpers.learner;

public class WalkableHistoryHelper {

    public static String fetchHistory(final String cohortId, final String deploymentId, final String pathwayId) {
        return "{\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        id\n" +
                "        activity {\n" +
                "          pathways(pathwayId: \"" + pathwayId + "\") {\n" +
                "            id\n" +
                "            history {\n" +
                "              edges {\n" +
                "                node {\n" +
                "                  evaluationId\n" +
                "                  evaluatedAt\n" +
                "                  elementId\n" +
                "                  elementType\n" +
                "                  elementAttemptId\n" +
                "                  configurationFields(fieldNames: [\"title\"]){" +
                "                    fieldName" +
                "                    fieldValue" +
                "                  }\n" +
                "                  evaluation {\n" +
                "                    scope {\n" +
                "                      sourceId\n" +
                "                      scopeURN\n" +
                "                      data\n" +
                "                    }\n" +
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

    public static String fetchEvaluation(final String cohortId, final String deploymentId, final String evaluationId) {
        return "{\n" +
                "  learn {\n" +
                "    cohort(cohortId: \"" + cohortId + "\") {\n" +
                "      deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "        id\n" +
                "        evaluation (evaluationId: \"" + evaluationId + "\") {\n" +
                "            id\n" +
                "            elementId\n" +
                "            elementType\n" +
                "            studentId\n" +
                "            attemptId\n" +
                "            studentScopeURN\n" +
                "            completed\n" +
                "            scope {\n" +
                "                sourceId\n" +
                "                scopeURN\n" +
                "                data\n" +
                "            }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
