package mercury.helpers.learner;

public class ManualGradingHelper {

    public static String fetchManualGradingComponent(String cohortId, String deploymentId) {
        return "query {\n" +
                "\tcohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    deployment(deploymentId: \"" + deploymentId + "\") {\n" +
                "      manualGradingComponents {\n" +
                "        componentId\n" +
                "        deploymentId\n" +
                "        maxScore \n" +
                "        parentId\n" +
                "        parentType\n" +
                "        componentConfigurationFields(fieldNames: [\"title\"]) {\n" +
                "          fieldName\n" +
                "          fieldValue\n" +
                "        }\n" +
                "        parentConfigurationFields(fieldNames: [\"title\"]) {\n" +
                "          fieldName\n" +
                "          fieldValue\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchManualGradeReport(String cohortId, String componentId, String deploymentId,
                                                String parentId, String parentType) {
        return "query {\n" +
                "\tcohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollments {\n" +
                "      edges {\n" +
                "        node {\n" +
                "          accountId\n" +
                "          cohortId\n" +
                "          getLatestAttemptStudentManualGradeReport(input: {\n" +
                "            componentId: \"" + componentId + "\",\n" +
                "            deploymentId: \"" + deploymentId + "\",\n" +
                "            parentId: \"" + parentId + "\",\n" +
                "            parentType: " + parentType + "\n" +
                "          }) {\n" +
                "            attemptId\n" +
                "            componentId\n" +
                "            parentId\n" +
                "            parentType\n" +
                "            state\n" +
                "            studentId\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchManualGradeReportByActivity(String cohortId, String activityId, String deploymentId) {
        return "query {\n" +
                "    cohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollments {\n" +
                "      edges {\n" +
                "        node {\n" +
                "          learnerPerformanceByDeployment(activityId: \"" + activityId + "\", " +
                "                                         deploymentId: \"" + deploymentId + "\") {\n" +
                "            getLatestAttemptActivityManualGradeReport {\n" +
                "               attemptId\n" +
                "               componentId\n" +
                "               parentId\n" +
                "               parentType\n" +
                "               state\n" +
                "               studentId\n" +
                "               completedWalkable {\n" +
                "                  elementId\n" +
                "                  elementType\n" +
                "                  evaluatedAt\n" +
                "                  evaluationId\n" +
                "                  parentElementAttemptId\n" +
                "                  elementAttemptId\n" +
                "                  configurationFields(fieldNames: [\"title\"]) {\n" +
                "                    fieldName\n" +
                "                    fieldValue\n" +
                "                  }\n" +
                "                  evaluation {\n" +
                "                    scope {\n" +
                "                      data\n" +
                "                      sourceId\n" +
                "                      scopeURN\n" +
                "                    }\n" +
                "                  }\n" +
                "               }\n" +
                "               grades {\n" +
                "                 id\n" +
                "                 instructorId\n" +
                "                 operator\n" +
                "                 score\n" +
                "                 createdAt\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchManualGradeReportByInteractive(String cohortId, String studentId, String deploymentId, String interactiveId) {
        return "query {\n" +
                "  cohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollmentByStudent(studentId: \"" + studentId + "\") {\n" +
                "      getLatestAttemptInteractiveManualGradeReport(deploymentId: \"" + deploymentId + "\", interactiveId: \"" + interactiveId + "\") {\n" +
                "        attemptId\n" +
                "        componentId\n" +
                "        parentId\n" +
                "        parentType\n" +
                "        state\n" +
                "        studentId\n" +
                "        completedWalkable {\n" +
                "          elementId\n" +
                "          elementType\n" +
                "          evaluatedAt\n" +
                "          evaluationId\n" +
                "          parentElementAttemptId\n" +
                "          configurationFields(fieldNames: [\"title\"]) {\n" +
                "            fieldName\n" +
                "            fieldValue\n" +
                "          }\n" +
                "          evaluation {\n" +
                "            scope {\n" +
                "              data\n" +
                "              sourceId\n" +
                "              scopeURN\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "        grades {\n" +
                "          id\n" +
                "          instructorId\n" +
                "          operator\n" +
                "          score\n" +
                "          createdAt\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String fetchManualGradeReportWithGrades(String cohortId, String componentId, String deploymentId,
                                                          String parentId, String parentType) {
        return "query {\n" +
                "\tcohortById(cohortId: \"" + cohortId + "\") {\n" +
                "    enrollments {\n" +
                "      edges {\n" +
                "        node {\n" +
                "          accountId\n" +
                "          cohortId\n" +
                "          getLatestAttemptStudentManualGradeReport(input: {\n" +
                "            componentId: \"" + componentId + "\",\n" +
                "            deploymentId: \"" + deploymentId + "\",\n" +
                "            parentId: \"" + parentId + "\",\n" +
                "            parentType: " + parentType + "\n" +
                "          }) {\n" +
                "            attemptId\n" +
                "            componentId\n" +
                "            parentId\n" +
                "            parentType\n" +
                "            state\n" +
                "            studentId\n" +
                "            completedWalkable {\n" +
                "              elementId\n" +
                "              elementType\n" +
                "              evaluatedAt\n" +
                "              evaluationId\n" +
                "              parentElementAttemptId\n" +
                "              elementAttemptId\n" +
                "              configurationFields(fieldNames: [\"title\"]) {\n" +
                "                fieldName\n" +
                "                fieldValue\n" +
                "              }\n" +
                "              evaluation {\n" +
                "                  scenarioCorrectness\n" +
                "                scope {\n" +
                "                  data\n" +
                "                  sourceId\n" +
                "                  scopeURN\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "            grades {\n" +
                "              id\n" +
                "              instructorId\n" +
                "              operator\n" +
                "              score\n" +
                "              createdAt\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    public static String createManualGrade(String componentId, String deploymentId, String studentId, Double score, String attemptId, String operator) {
        return "mutation {\n" +
                "  ManualGradeCreate(componentId: \"" + componentId + "\", deploymentId: \"" + deploymentId + "\",\n" +
                "    manualGrade: {\n" +
                "      studentId: \"" + studentId + "\",\n" +
                "      score: " + score + ",\n" +
                "      attemptId: \"" + attemptId + "\",\n" +
                "      operator: " + operator + "\n" +
                "  \t}) {\n" +
                "    id\n" +
                "    createdAt\n" +
                "    instructorId\n" +
                "    operator\n" +
                "    score\n" +
                "  }\n" +
                "}";
    }
}
