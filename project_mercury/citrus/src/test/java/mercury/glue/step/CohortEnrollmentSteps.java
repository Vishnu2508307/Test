package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.helpers.cohort.CohortEnrollmentHelper;
import mercury.helpers.cohort.CohortHelper;
import mercury.helpers.learner.LearnerCohortHelper;

public class CohortEnrollmentSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Given("^\"([^\"]*)\" enrolls \"([^\"]*)\" to the created cohort$")
    public void enrollsToTheCreatedCohort(String user, String anotherUser) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortEnrollmentHelper.createCohortEnrollRequest(
                interpolate("cohort_id"),
                interpolate(getAccountIdVar(anotherUser))
        ));
    }

    @Then("^\"([^\"]*)\" is not able to \"([^\"]*)\" \"([^\"]*)\" due to missing permission level$")
    public void isNotAbleToDueToMissingPermissionLevel(String user, String enrollmentAction, String anotherUser) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortEnrollmentHelper.getUnauthorizedResponse(String.format("account.%s.error", enrollmentAction))
        ));
    }

    @Given("^\"([^\"]*)\" has enrolled \"([^\"]*)\" to the created cohort$")
    public void hasEnrolledToTheCreatedCohort(String user, String anotherUser) throws JsonProcessingException {
        enrollsToTheCreatedCohort(user, anotherUser);
        isSuccessfullyEnrolled(anotherUser);
    }

    @Given("^\"([^\"]*)\" has disenrolled \"([^\"]*)\" from the created cohort$")
    public void hasDisenrolledfromTheCreatedCohort(String user, String anotherUser) throws JsonProcessingException {
        disenrollFromTheCreatedCohort(user, anotherUser);
        isSuccessfullyDisenrolled(anotherUser);
    }

    @When("^\"([^\"]*)\" disenroll \"([^\"]*)\" from the created cohort$")
    public void disenrollFromTheCreatedCohort(String user, String anotherUser) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortEnrollmentHelper.createCohortDisenrollRequest(
                interpolate("cohort_id"),
                interpolate(getAccountIdVar(anotherUser))
        ));
    }

    @Then("^\"([^\"]*)\" is successfully enrolled$")
    public void isSuccessfullyEnrolled(final String anotherUser) {
        messageOperations.receiveJSON(runner, action ->
                action.payload(CohortEnrollmentHelper.getCohortEnrollSuccessResponse(
                        interpolate("cohort_id"),
                        interpolate(getAccountIdVar(anotherUser)),
                        interpolate(getSubscriptionIdVar(anotherUser))
                )));
    }

    @Then("^\"([^\"]*)\" is successfully disenrolled$")
    public void isSuccessfullyDisenrolled(String account) {
        // the account is not really needed here however it makes it fluent to read in the feature file
        messageOperations.receiveJSON(runner, action ->
                action.payload(CohortEnrollmentHelper.getCohortDisenrollSuccessResponse()));
    }

    @When("{string} fetches the enrollment for the cohort the following accounts are listed")
    public void fetchesTheEnrollmentForTheCohortTheFollowingAccountsAreListed(String user, Map<String, String> expectedAccountNames) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, CohortHelper.fetchEnrollments(interpolate("cohort_id")));

        messageOperations.receiveJSON(runner, action -> {
            action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                @Override
                public void validate(final Map payload, final Map<String, Object> headers, final TestContext context) {
                    Map response = (Map) payload.get("response");
                    Map data = (Map) response.get("data");
                    Map cohort = (Map) data.get("cohortById");
                    Map enrollments = (Map) cohort.get("enrollments");
                    List edges = (List) enrollments.get("edges");

                    assertEquals(expectedAccountNames.size(), edges.size());

                    List<Map.Entry<String, String>> expectedAccountNamesList = new ArrayList<>(expectedAccountNames.entrySet());

                    for (int i = 0; i < edges.size(); i++) {
                        Map edge = (Map) edges.get(i);
                        Map node = (Map) edge.get("node");
                        Map.Entry<String, String> expectedEntry = expectedAccountNamesList.get(i);
                        String expectedAccountId = context.getVariable(getAccountIdVar(expectedEntry.getKey()));
                        String expectedEnrollmentStatus = expectedEntry.getValue();
                        assertEquals(expectedAccountId, node.get("accountId"));
                        assertEquals(expectedEnrollmentStatus, node.get("enrollmentStatus"));
                    }
                }
            });
        });
    }

    @And("{string} autoenroll to cohort {string}")
    public void autoenrollToCohort(final String accountName, final String cohortName) {
        // authorize the account
        authenticationSteps.authenticatesViaIes(accountName);
        // fetch the cohort so the user is enrolled. For this to happen the enrollmentType must be OPEN
        messageOperations.sendGraphQL(runner, LearnerCohortHelper.fetchCohort(interpolate(nameFrom(cohortName, "id"))));

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"graphql.response\"," +
                                "\"response\":{" +
                                    "\"data\":{" +
                                        "\"learn\":{" +
                                            "\"cohort\":{" +
                                                "\"id\":\"" + interpolate(nameFrom(cohortName, "id")) + "\"," +
                                                "\"name\":\"@notEmpty()@\"}}}},\"replyTo\":\"@notEmpty()@\"}"));
    }
}
