package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.helpers.cohort.CohortHelper;

public class CohortArchiveSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" archives the created cohort$")
    public void archivesTheCreatedCohort(String user) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortHelper.createCohortArchiveRequest(
                Variables.interpolate("cohort_id")
        ));
    }

    @Then("^the created cohort is archived$")
    public void theCreatedCohortIsArchived() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortHelper.getCohortArchiveResponse()
        ));
    }

    @Then("^the \"([^\"]*)\" request is not authorized$")
    public void theRequestIsNotAuthorized(String userAction) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortHelper.getCohortArchiveUnauthorizedResponse(userAction)
        ));
    }

    @Given("^\"([^\"]*)\" has archived the created cohort$")
    public void hasArchivedTheCreatedCohort(String user) throws Throwable {
        archivesTheCreatedCohort(user);
        theCreatedCohortIsArchived();
    }

    @Then("^the created cohort is un-archived$")
    public void theCreatedCohortIsUnArchived() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortHelper.getCohortUnarchiveResponse()
        ));
    }

    @When("^\"([^\"]*)\" un-archives the created cohort$")
    public void unArchivesTheCreatedCohort(String user) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortHelper.createCohortUnarchiveRequest(
                Variables.interpolate("cohort_id")
        ));
    }

    @When("^\"([^\"]*)\" has unarchived the created cohort$")
    public void hasUnarchivedTheCreatedCohort(String user) throws JsonProcessingException {
        unArchivesTheCreatedCohort(user);
        theCreatedCohortIsUnArchived();
    }
}
