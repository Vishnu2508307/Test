package mercury.helpers.learner;

public class LearnerWalkableFetchHelper {

    public static String fetchWalkableQuery(String cohortId, String deploymentId, String pathwayId) {
        return "{ " +
                "learn { " +
                  "cohort(cohortId: \"" + cohortId + "\") { " +
                    "id " +
                    "deployment(deploymentId: \"" + deploymentId + "\") { " +
                      "activity { " +
                        "pathways(pathwayId: \"" + pathwayId + "\") { " +
                          "walkables { " +
                            "edges { " +
                              "node { " +
                                "id " +
                                "elementType " +
                             "} " +
                           "} " +
                         "} " +
                       "} " +
                     "} " +
                   "} " +
                 "} " +
               "} " +
             "} ";
    }

    public static String fetchScopeEntryQuery(String cohortId, String deploymentId) {
        return "    {\n" +
                "      learn {\n" +
                "        cohort(cohortId: \""+cohortId+"\") {\n" +
                "          deployment(deploymentId: \""+deploymentId+"\") {\n" +
                "            activity {\n" +
                "              pathways {\n" +
                "                walkables {\n" +
                "                  edges {\n" +
                "                    node {\n" +
                "                      id\n" +
                "                      scope {\n" +
                "                        sourceId\n" +
                "                        scopeURN\n" +
                "                        data\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "              scope {\n" +
                "                sourceId\n" +
                "                scopeURN\n" +
                "                data\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }";
    }

    public static String fetchActivityScopeEntryQuery(String cohortId, String deploymentId, String activityId) {
        return "{learn {\n" +
                "  cohort(cohortId: \"" + cohortId + "\") {\n" +
                "    deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "      activity(activityId: \"" + activityId + "\") {\n" +
                "        scope {\n" +
                "          data\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}}";
    }
}
