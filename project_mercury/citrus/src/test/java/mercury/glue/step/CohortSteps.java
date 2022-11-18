package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.EventSteps.COHORT_BROADCAST_ID;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.cohort.CohortHelper.assertCohortFields;
import static mercury.helpers.cohort.CohortHelper.cohortBroadcastMessage;
import static mercury.helpers.cohort.CohortHelper.fetchCohortSummary;
import static mercury.helpers.cohort.CohortHelper.fetchCohortSummaryWithField;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cucumber.api.java.en.And;
import cucumber.api.java.en.But;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;
import mercury.helpers.cohort.CohortHelper;

public class CohortSteps {

    public static final String DEFAULT_COHORT_NAME = "Citrus cohort";
    public static final String DEFAULT_COHORT_TYPE = "OPEN";
    public static final String DEFAULT_PRODUCT_ID = "A103000103955";

    public static final String DEFAULT_LTI_COHORT_NAME = "Lti type cohort";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates a cohort in workspace \"([^\"]*)\"$")
    public void createsACohortInWorkspace(String user, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        Map<String, String> cohort = new HashMap<>();
        cohort.put("name", DEFAULT_COHORT_NAME);
        cohort.put("enrollmentType", DEFAULT_COHORT_TYPE);
        cohort.put("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));
        cohort.put("productId", DEFAULT_PRODUCT_ID);
        runner.variable("cohort", cohort);

        messageOperations.sendJSON(runner, CohortHelper.createCohortRequest(
                cohort.get("name"),
                cohort.get("enrollmentType"),
                cohort.get("workspaceId"),
                cohort.get("productId"))
                );
    }

    @Then("^the cohort is successfully created$")
    public void theCohortIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortHelper.createCohortResponse("@variable('cohort_id')@", DEFAULT_COHORT_NAME, DEFAULT_COHORT_TYPE)));
    }

    @Then("^\"([^\"]*)\" is not able to create the cohort due to missing permission level$")
    public void isNotAbleToCreateTheCohortDueToMissingPermissionLevel(String user) {
        messageOperations.receiveJSON(runner, action -> action.payload(CohortHelper.createCohortPermissionFailResponse()));
    }

    @When("^\"([^\"]*)\" creates a cohort in workspace \"([^\"]*)\" with invalid date")
    public void createsACohortInWorkspaceWithUnsoppertedDate(String accountName, String cohortName, Map<String, String> cohortFields) {
        authenticationSteps.authenticateUser(accountName);

        // the map value type must be object for the lti credentials param to work
        Map<String, Object> fields = new HashMap<>();
        fields.putAll(cohortFields);

        runner.variable("cohort", fields);
        messageOperations.sendJSON(runner, CohortHelper.createCohortRequest(fields));
    }

    @Then("^the cohort is not created$")
    public void theCohortIsNotCreated() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                createCohortInavlidDateFailResponse()));
    }
    public static String createCohortInavlidDateFailResponse() {
        return "{" +
                "\"type\":\"workspace.cohort.create.error\"," +
                "\"code\":400," +
                "\"message\": \"Invalid startDate\"," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    @When("^\"([^\"]*)\" has created a cohort with values$")
    public void hasCreatedACohortWithValues(String creator, Map<String, String> cohortFields) {
        authenticationSteps.authenticateUser(creator);

        runner.variable("cohort", cohortFields);
        messageOperations.sendJSON(runner, CohortHelper.createCohortRequest(cohortFields));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.create.ok";
                    }

                    @Override
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        context.setVariable("cohort_id", payload.getSummaryPayload().getCohortId().toString());
                        assertCohortFields(cohortFields, payload);
                    }
                }));
    }

    @Then("^\"([^\"]*)\" fetches the cohort$")
    public void canFetchTheCohortDetails(String loggedAccount) {
        authenticationSteps.authenticateUser(loggedAccount);

        messageOperations.sendJSON(runner, CohortHelper.getCohortRequest(interpolate("cohort_id")));
    }

    @Then("^the cohort is successfully fetched$")
    public void theCohortIsSuccessfullyFetched() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.get.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        Map<String, String> fields = (Map<String, String>) context.getVariable("cohort", Map.class);
                        assertEquals(context.getVariable("cohort_id"), payload.getSummaryPayload().getCohortId().toString());
                        assertCohortFields(fields, payload);
                    }
                }));
    }

    @Then("^\"([^\"]*)\" is not able to fetch the cohort due to missing permission level$")
    public void isNotAbleToFetchTheCohortDueToMissingPermissionLevel(String user) {
        messageOperations.receiveJSON(runner, action -> action.payload(CohortHelper.getCohortPermissionErrorResponse()));
    }

    @Then("^\"([^\"]*)\" can not fetch the cohort details$")
    public void canNotFetchTheCohortDetails(String loggedAccount) throws Throwable {
        authenticationSteps.authenticateUser(loggedAccount);

        messageOperations.sendJSON(runner, CohortHelper.getCohortRequest(interpolate("cohort_id")));

        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortHelper.getCohortErrorResponse(401, "Unauthorized: Unauthorized permission level")));
    }

    @Then("^count of enrollments is (\\d+)$")
    public void countOfEnrollmentsIs(int count) {
        messageOperations.receiveJSON(runner,
                action -> action.jsonPath("$.response.cohort.summary.enrollmentsCount", count));
    }

    @When("^\"([^\"]*)\" updates this cohort$")
    public void updatesThisCohort(String user, Map<String, String> cohortFields) throws Throwable {
        authenticationSteps.authenticateUser(user);

        Map<String, Object> fields = new HashMap<>();

        if (cohortFields.get("enrollmentType").equals("LTI") && cohortFields.containsKey("ltiKey") &&
                cohortFields.containsKey("ltiSecret")) {
            fields.putAll(cohortFields);
            LtiConsumerCredential creds = new LtiConsumerCredential()
                    .setKey(fields.get("ltiKey").toString())
                    .setSecret(fields.get("ltiSecret").toString());

            fields.put("ltiConsumerCredential", creds);

            fields.remove("ltiKey");
            fields.remove("ltiSecret");
        } else {
            fields.putAll(cohortFields);
        }

        runner.variable("cohort", fields);
        messageOperations.sendJSON(runner, CohortHelper.updateCohortRequest(interpolate("cohort_id"), fields));
    }

    @Then("^the cohort is successfully updated$")
    public void theCohortIsSuccessfullyUpdated() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.change.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        Map<String, String> fields = (Map<String, String>) context.getVariable("cohort", Map.class);
                        assertEquals(context.getVariable("cohort_id"), payload.getSummaryPayload().getCohortId().toString());
                        assertCohortFields(fields, payload);
                    }
                }));
    }

    @When("^\"([^\"]*)\" has updated the cohort$")
    public void hasUpdatedTheCohort(String user, Map<String, String> dataTable) throws Throwable {
        updatesThisCohort(user, dataTable);
        theCohortIsSuccessfullyUpdated();
    }


    @Then("^\"([^\"]*)\" is not able to update the cohort due to missing permission level$")
    public void isNotAbleToUpdateTheCohortDueToMissingPermissionLevel(String account) {
        messageOperations.receiveJSON(runner, action -> action.payload(CohortHelper.updateCohortPermissionFailResponse()));
    }

    @And("^\"([^\"]*)\" should receive a cohort \"([^\"]*)\" notification via a \"([^\"]*)\" client$")
    public void shouldReceiveACohortNotificationViaAClient(String userName, String cohortAction, String clientName) {
        messageOperations.receiveJSON(runner, action-> action
                .payload(cohortBroadcastMessage("@variable('cohort_id')@", cohortAction,
                        interpolate(clientName + "_" + COHORT_BROADCAST_ID))), clientName);
    }

    @But("^\"([^\"]*)\" should not receive any cohort notification$")
    public void shouldNotReceiveAnyCohortNotification(String userName) throws Throwable {
        /*Here we want to check that the user doesn't get any broadcast messages in a socket.
         As right now I don't have elegant solution I send 'me.get' message and expect 'me.get.ok' response.*/
        messageOperations.sendJSON(runner, "{\"type\":\"me.get\"}");
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "me.get.ok"));
    }

    @And("^\"([^\"]*)\" has created a cohort for workspace \"([^\"]*)\"$")
    public void hasCreatedACohortForWorkspace(String user, String workspaceName) {
        createsACohortInWorkspace(user, workspaceName);
        theCohortIsSuccessfullyCreated();
    }

    @And("^\"([^\"]*)\" has created a cohort \"([^\"]*)\" for workspace \"([^\"]*)\"$")
    public void hasCreatedACohortForWorkspace(String user, String cohortName, String workspaceName) {
        Map<String, String> fields = new HashMap<>(2);
        fields.put("name", cohortName);
        fields.put("enrollmentType", DEFAULT_COHORT_TYPE);
        fields.put("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));

        hasCreatedACohortWithValues(user, fields);
    }

    @And("^\"([^\"]*)\" has created (\\d+) cohorts for workspace \"([^\"]*)\"$")
    public void hasCreatedCohortsForWorkspace(String user, int cohortCount, String workspaceName) {
        for (int i = 0; i < cohortCount; i++) {
            Map<String, String> fields = new HashMap<>(2);
            fields.put("name", DEFAULT_COHORT_NAME + "_" + i);
            fields.put("enrollmentType", DEFAULT_COHORT_TYPE);
            fields.put("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));

            hasCreatedACohortWithValues(user, fields);
        }
    }

    @And("^\"([^\"]*)\" can fetch the cohort$")
    public void canFetchTheCohort(String accountName) {
        canFetchTheCohortDetails(accountName);
        theCohortIsSuccessfullyFetched();
    }

    @And("^\"([^\"]*)\" has created a cohort in workspace \"([^\"]*)\"$")
    public void hasCreatedACohortInWorkspace(String user, String workspaceName) {
        createsACohortInWorkspace(user, workspaceName);
        theCohortIsSuccessfullyCreated();
    }

    @When("{string} tries to fetch the cohort summary")
    public void triesToFetchTheCohortSummary(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchCohortSummary(interpolate("cohort_id")));//
    }

    @Then("{string} is enrolled to the cohort with type {string}")
    public void isEnrolledToTheCohortWithType(String user, String enrollmentType) {
        messageOperations.receiveJSON(runner, action -> action.payload("{}"));
    }

    @Then("{string} has access to the cohort")
    public void hasAccessToTheCohort(String user) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.learn.cohort.id", interpolate("cohort_id")));
    }

    @When("{string} tries to fetch the cohort summary {string}")
    public void triesToFetchTheCohortSummary(String accountName, String cohortName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendGraphQL(runner, fetchCohortSummary(interpolate(nameFrom(cohortName, "id"))));
    }

    @Then("{string} has access to the {string} cohort")
    public void hasAccessToTheCohort(String accountName, String cohortName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.learn.cohort.id", interpolate(nameFrom(cohortName, "id"))));
    }

    @And("{string} has created a cohort {string} with values")
    public void hasCreatedACohortWithValues(String accountName, String cohortName, Map<String, String> cohortFields) {
        authenticationSteps.authenticateUser(accountName);

        // the map value type must be object for the lti credentials param to work
        Map<String, Object> fields = new HashMap<>();

        // special case for when the LTI credentials are passed along
        if (cohortFields.containsKey("ltiKey") && cohortFields.containsKey("ltiSecret")) {
            fields.putAll(cohortFields);
            LtiConsumerCredential creds = new LtiConsumerCredential()
                    .setKey(fields.get("ltiKey").toString())
                    .setSecret(fields.get("ltiSecret").toString());
            fields.put("ltiConsumerCredential", creds);
            fields.remove("ltiKey");
            fields.remove("ltiSecret");
        } else {
            fields.putAll(cohortFields);
        }

        runner.variable("cohort", fields);
        messageOperations.sendJSON(runner, CohortHelper.createCohortRequest(fields));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.create.ok";
                    }

                    @Override
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        context.setVariable(nameFrom(cohortName, "id"), payload.getSummaryPayload().getCohortId().toString());
                        context.setVariable(nameFrom(cohortName, "product_id"), String.valueOf(payload.getSettingsPayload().getProductId()));
                        assertCohortFields(cohortFields, payload);
                    }
                }));
    }

    @When("{string} has updated cohort {string} with values")
    public void updatesCohortWithValues(String accountName, String cohortName, Map<String, String> cohortFields) {
        authenticationSteps.authenticateUser(accountName);

        runner.variable("cohort", cohortFields);
        messageOperations.sendJSON(runner, CohortHelper.updateCohortRequest(interpolate(nameFrom(cohortName, "id")), cohortFields));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.change.ok";
                    }

                    @Override
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        context.setVariable(nameFrom(cohortName, "id"), payload.getSummaryPayload().getCohortId().toString());
                        context.setVariable(nameFrom(cohortName, "product_id"), String.valueOf(payload.getSettingsPayload().getProductId()));
                        assertCohortFields(cohortFields, payload);
                    }
                }));
    }

    @Then("{string} can access the {string} field {string} from the {string} cohort")
    public void canAccessTheFieldFromTheCohort(final String accountName, final String fieldName, final String fieldValue,
                                               final String cohortName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendGraphQL(runner, fetchCohortSummaryWithField(interpolate(nameFrom(cohortName, "id")), fieldName));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.learn.cohort." + fieldName, fieldValue));
    }

    @When("{string} creates a cohort in workspace with Lti type {string} with consumer credential")
    public void createsACohortInWorkspaceWithLtiTypeWithConsumerCredential(String user, String workspaceName, String credential) {
        authenticationSteps.authenticateUser(user);

        JSONObject credentialData = new JSONObject(credential);
        assertNotNull(credentialData);

        Map<String, String> cohort = new HashMap<>();
        cohort.put("name", DEFAULT_LTI_COHORT_NAME);
        cohort.put("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));

        LtiConsumerCredential ltiConsumerCredential = new LtiConsumerCredential()
                .setKey(credentialData.getString("key"))
                .setSecret(credentialData.getString("secret"));

        runner.variable("cohort", cohort);

        messageOperations.sendJSON(runner, CohortHelper.createLtiCohortRequest(
                    cohort.get("name"),
                    cohort.get("workspaceId"),
                    ltiConsumerCredential
                )
        );
    }

    @Then("^the cohort with Lti type is successfully created$")
    public void theCohortWithLtiTypeIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<CohortPayload>(CohortPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "cohort";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.create.ok";
                    }

                    @Override
                    public void validate(CohortPayload payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(DEFAULT_LTI_COHORT_NAME, payload.getSummaryPayload().getName());
                        assertNotNull(payload.getSettingsPayload().getLtiConsumerCredentials());
                        assertNotNull(payload.getSettingsPayload().getLtiConsumerCredentials().get(0).getKey());
                        assertNotNull(payload.getSettingsPayload().getLtiConsumerCredentials().get(0).getSecret());
                    }
                }));
    }

    @When("^\"([^\"]*)\" creates a cohort in workspace \"([^\"]*)\" without productId$")
    public void createsACohortInWorkspaceWithoutProductId(String user, String workspaceName) {
        authenticationSteps.authenticateUser(user);
        Map<String, String> cohort = new HashMap<>();
        cohort.put("name", DEFAULT_COHORT_NAME);
        cohort.put("enrollmentType", DEFAULT_COHORT_TYPE);
        cohort.put("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)));
        runner.variable("cohort", cohort);
        messageOperations.sendJSON(runner, CohortHelper.createCohortRequest(
                cohort.get("name"),
                cohort.get("enrollmentType"),
                cohort.get("workspaceId"),
                cohort.get("productId"))
        );
    }
}
