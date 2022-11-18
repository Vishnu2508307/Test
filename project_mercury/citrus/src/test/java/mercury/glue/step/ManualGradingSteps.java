package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.learner.ManualGradingHelper.createManualGrade;
import static mercury.helpers.learner.ManualGradingHelper.fetchManualGradeReport;
import static mercury.helpers.learner.ManualGradingHelper.fetchManualGradeReportByActivity;
import static mercury.helpers.learner.ManualGradingHelper.fetchManualGradeReportByInteractive;
import static mercury.helpers.learner.ManualGradingHelper.fetchManualGradeReportWithGrades;
import static mercury.helpers.learner.ManualGradingHelper.fetchManualGradingComponent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class ManualGradingSteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches manual grading configurations for deployment {string}")
    public void fetchesManualGradingConfigurationsForDeployment(String user, String deploymentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchManualGradingComponent(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id"))
        ));
    }

    @Then("one manual grading configuration is returned with values")
    public void oneManualGradingConfigurationIsReturnedWithValues(Map<String, String> values) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map cohort = (Map) data.get("cohortById");
                List deployments = (List) cohort.get("deployment");
                Map deployment = (Map) deployments.get(0);
                List manualGradingComponents = (List) deployment.get("manualGradingComponents");

                assertEquals(1, manualGradingComponents.size());

                Map manualGradingConfiguration = (Map) manualGradingComponents.get(0);

                String expectedComponentId = context.getVariable(interpolate(nameFrom(values.get("componentId"), "id")));
                Double expectedMaxScore = Double.valueOf(values.get("maxScore"));
                String expectedParentId = context.getVariable(interpolate(nameFrom(values.get("parentId"), "id")));
                String expectedParentType = values.get("parentType");
                String expectedDeploymentId = context.getVariable(interpolate(nameFrom(values.get("deploymentId"), "id")));

                assertEquals(expectedComponentId, manualGradingConfiguration.get("componentId"));
                assertEquals(expectedParentId, manualGradingConfiguration.get("parentId"));
                assertEquals(expectedDeploymentId, manualGradingConfiguration.get("deploymentId"));
                assertEquals(expectedParentType, manualGradingConfiguration.get("parentType"));
                assertEquals(expectedMaxScore, Double.valueOf(manualGradingConfiguration.get("maxScore").toString()));
            }
        }));
    }

    @When("{string} fetches enrolled student manual grade report with configurations")
    public void fetchesEnrolledStudentManualGradeReportWithConfigurations(String user, Map<String, String> configurations) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchManualGradeReport(
                interpolate("cohort_id"),
                interpolate(nameFrom(configurations.get("componentId"), "id")),
                interpolate(nameFrom(configurations.get("deploymentId"), "id")),
                interpolate(nameFrom(configurations.get("parentId"), "id")),
                configurations.get("parentType")
        ));
    }

    @Then("the manual grade report has one enrollment with")
    public void theManualGradeReportHasOneEnrollmentWith(Map<String, String> expectedValues) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map cohort = (Map) data.get("cohortById");
                Map enrollments = (Map) cohort.get("enrollments");
                List edges = (List) enrollments.get("edges");
                Map edge = (Map) edges.get(0);
                Map node = (Map) edge.get("node");
                Map report = (Map) node.get("getLatestAttemptStudentManualGradeReport");

                String expectedStudentId = context.getVariable(getAccountIdVar(expectedValues.get("studentId")));
                String expectedState = expectedValues.get("state");

                assertEquals(expectedState, report.get("state"));
                assertEquals(expectedStudentId, report.get("studentId"));

                Object attemptId = report.get("attemptId");

                if (attemptId != null) {
                    context.setVariable("LATEST_ATTEMPT", attemptId.toString());
                }
            }
        }));
    }

    @When("{string} create a manual grade for component {string} and deployment {string} with args")
    public void createAManualGradeForComponentAndDeploymentWithArgs(String user, String componentName,
                                                                    String deploymentName, Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, createManualGrade(
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(getAccountIdVar(args.get("studentId"))),
                Double.valueOf(args.get("score")),
                interpolate(args.get("attemptId")),
                args.get("operator")
        ));
    }

    @Then("the manual grade is successfully created with")
    public void theManualGradeIsSuccesfullyCreatedWith(Map<String, String> args) {
        messageOperations.receiveJSON(runner, action -> {
            action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                @Override
                public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                    Map response = (Map) payload.get("response");
                    Map data = (Map) response.get("data");
                    Map manualGradeCreate = (Map) data.get("ManualGradeCreate");

                    String expectedInstructorId = context.getVariable(interpolate(nameFrom(args.get("instructorId"), "id")));
                    Double expectedScore = Double.valueOf(args.get("score"));

                    assertEquals(expectedInstructorId, manualGradeCreate.get("instructorId").toString());
                    assertEquals(expectedScore, Double.valueOf(manualGradeCreate.get("score").toString()));
                    assertNotNull(manualGradeCreate.get("id"));
                    assertNotNull(manualGradeCreate.get("createdAt"));
                }
            });
        });
    }

    @When("{string} fetches enrolled student manual grade report grades with configurations")
    public void fetchesEnrolledStudentManualGradeReportGradesWithConfigurations(String user, Map<String, String> args) {
        messageOperations.sendGraphQL(runner, fetchManualGradeReportWithGrades(
                interpolate("cohort_id"),
                interpolate(nameFrom(args.get("componentId"), "id")),
                interpolate(nameFrom(args.get("deploymentId"), "id")),
                interpolate(nameFrom(args.get("parentId"), "id")),
                args.get("parentType")
        ));
    }

    @Then("the manual grade report with grades has one enrollment with")
    public void theManualGradeReportWithGradesHasOneEnrollmentWith(Map<String, String> args) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map cohort = (Map) data.get("cohortById");
                Map enrollments = (Map) cohort.get("enrollments");
                List edges = (List) enrollments.get("edges");
                Map edge = (Map) edges.get(0);
                Map node = (Map) edge.get("node");
                Map report = (Map) node.get("getLatestAttemptStudentManualGradeReport");

                String expectedStudentId = context.getVariable(getAccountIdVar(args.get("studentId")));
                String expectedState = args.get("state");

                assertEquals(expectedState, report.get("state"));
                assertEquals(expectedStudentId, report.get("studentId"));

                Map completedWalkable = (Map) report.get("completedWalkable");

                assertNotNull(completedWalkable);
                assertNotNull(completedWalkable.get("elementId"));
                assertNotNull(completedWalkable.get("elementType"));
                assertNotNull(completedWalkable.get("evaluatedAt"));
                assertNotNull(completedWalkable.get("evaluationId"));
                assertNotNull(completedWalkable.get("parentElementAttemptId"));
                assertNotNull(completedWalkable.get("elementAttemptId"));

                //validating scenario correctness field from evalution object
                String expectedScenarioCorrectness = args.get("scenarioCorrectness");
                Map scenarioCorrectnessMap = (Map)completedWalkable.get("evaluation");
                assertNotNull(scenarioCorrectnessMap.get("scenarioCorrectness"));
                assertEquals(expectedScenarioCorrectness, scenarioCorrectnessMap.get("scenarioCorrectness"));

                List grades = (List) report.get("grades");

                assertEquals(Integer.valueOf(args.get("grades")), Integer.valueOf(grades.size()));

                Map grade = (Map) grades.get(0);

                String expectedGradedBy = context.getVariable(interpolate(getAccountIdVar(args.get("gradedBy"))));
                Double expectedScore = Double.valueOf(args.get("gradeScore"));

                assertEquals(expectedGradedBy, grade.get("instructorId").toString());
                assertEquals(expectedScore, Double.valueOf(grade.get("score").toString()));
            }
        }));
    }

    @Then("the manual grade report by activity has one enrollment with")
    public void theManualGradeReportByActivityHasOneEnrollmentWith(List<String> componentNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            @SuppressWarnings("unchecked")
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map cohort = (Map) data.get("cohortById");
                Map enrollments = (Map) cohort.get("enrollments");
                List edges = (List) enrollments.get("edges");
                Map edge = (Map) edges.get(0);
                Map node = (Map) edge.get("node");
                Map learnerPerformance = (Map) node.get("learnerPerformanceByDeployment");
                List<Map> manualGradeComponentReports = (List) learnerPerformance.get("getLatestAttemptActivityManualGradeReport");

                verifyManualGradeReports(context, manualGradeComponentReports, componentNames);
            }
        }));
    }

    @When("{string} fetches enrolled student manual grade reports by activity with configurations")
    public void fetchesEnrolledStudentManualGradeReportsByActivityWithConfigurations(String user, Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchManualGradeReportByActivity(
                interpolate("cohort_id"),
                interpolate(nameFrom(args.get("activityId"), "id")),
                interpolate(nameFrom(args.get("deploymentId"), "id"))
        ));
    }

    @When("{string} fetches enrolled student manual grade reports by interactive with configurations")
    public void fetchesEnrolledStudentManualGradeReportsByInteractiveWithConfigurations(String user, Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchManualGradeReportByInteractive(
                interpolate("cohort_id"),
                interpolate(getAccountIdVar(args.get("studentId"))),
                interpolate(nameFrom(args.get("deploymentId"), "id")),
                interpolate(nameFrom(args.get("interactiveId"), "id"))
        ));
    }

    @Then("the manual grade report by interactive has one enrollment with")
    public void theManualGradeReportByInteractiveHasOneEnrollmentWith(List<String> componentNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            @SuppressWarnings("unchecked")
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map cohort = (Map) data.get("cohortById");
                Map enrollment = (Map) cohort.get("enrollmentByStudent");
                List<Map> manualGradeComponentReports = (List) enrollment.get("getLatestAttemptInteractiveManualGradeReport");

                verifyManualGradeReports(context, manualGradeComponentReports, componentNames);
            }
        }));
    }

    private void verifyManualGradeReports(final TestContext context, final List<Map> manualGradeComponentReports, final List<String> componentNames) {
        assertNotNull(manualGradeComponentReports);
        assertEquals(componentNames.size(), manualGradeComponentReports.size());

        final List<String> expectedComponentIds = componentNames.stream()
                .map(componentName -> context.getVariable(interpolate(nameFrom(componentName, "id"))))
                .collect(Collectors.toList());

        manualGradeComponentReports.forEach(manualGradeComponentReport -> {
            final String actualComponentId = manualGradeComponentReport.get("componentId").toString();
            assertNotNull(manualGradeComponentReport.get("attemptId"));
            assertNotNull(actualComponentId);
            assertNotNull(manualGradeComponentReport.get("parentId"));
            assertNotNull(manualGradeComponentReport.get("parentType"));
            assertNotNull(manualGradeComponentReport.get("state"));
            assertNotNull(manualGradeComponentReport.get("studentId"));
            assertNotNull(manualGradeComponentReport.get("completedWalkable"));
            assertNotNull(manualGradeComponentReport.get("grades"));
            assertTrue(expectedComponentIds.contains(actualComponentId));
        });
    }

    @Then("no manual grading configuration are returned")
    public void noManualGradingConfigurationAreReturned() {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "\"type\":\"graphql.response\"," +
                "\"response\":{" +
                    "\"data\":{" +
                        "\"cohortById\":{" +
                            "\"deployment\":[{\"manualGradingComponents\":[]}]}}}," +
                "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the manual grade report by activity is empty")
    public void theManualGradeReportByActivityIsEmpty() {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "\"type\":\"graphql.response\"," +
                "\"response\":{" +
                    "\"data\":{" +
                        "\"cohortById\":{" +
                            "\"enrollments\":{" +
                                "\"edges\":[]" +
                            "}" +
                        "}" +
                    "}" +
                "},\"replyTo\":\"@notEmpty()@\"}"));
    }
}
