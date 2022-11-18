package mercury.glue.step;

import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.helpers.cohort.CohortEnrollmentHelper;

public class CohortEnrollmentsListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" lists the enrollments for the created cohort$")
    public void listsTheEnrollmentsForTheCreatedCohort(String user) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, CohortEnrollmentHelper.createCohortEnrollmentsListingRequest(
                Variables.interpolate("cohort_id")
        ));
    }

    @Then("^\"([^\"]*)\" shows up in the list of enrolled users$")
    public void showsUpInTheListOfEnrolledUsers(String account) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortEnrollmentHelper.validateCohortEnrollmentsListResponse(
                        Variables.interpolate("cohort_id"),
                        Variables.interpolate(getAccountIdVar(account))
                )
        ));
    }

    @When("^\"([^\"]*)\" lists the enrollments for the cohort \"([^\"]*)\"$")
    public void listsTheEnrollmentsForTheCohort(String user,String cohortName) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);
        final TestContext testContext = messageOperations.getTestContext(runner);

        messageOperations.sendJSON(runner, CohortEnrollmentHelper.createCohortEnrollmentsListingRequest(
                testContext.getVariable(nameFrom(cohortName, "id"))
        ));
    }

    @Then("^\"([^\"]*)\" shows up in the list of enrolled users for the cohort \"([^\"]*)\"$")
    public void showsUpInTheListOfEnrolledUsers(String account,String cohortName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualEnrollList, final Map<String, Object> headers, final TestContext context) {
                        final TestContext testContext = messageOperations.getTestContext(runner);
                        assertEquals(testContext.getVariable(nameFrom(cohortName, "id")),((LinkedHashMap) actualEnrollList.get(0)).get("cohortId"));
                        assertEquals(1,actualEnrollList.size());
                    }

                    @Override
                    public String getRootElementName() {
                        return "enrollments";
                    }

                    @Override
                    public String getType() {
                        return "workspace.cohort.enrollment.list.ok";
                    }
                }));

    }

    @Then("^the request is not authorized$")
    public void theRequestIsNotAuthorized() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortEnrollmentHelper.getUnauthorizedResponse("enrollment.list.error")
        ));
    }
}
