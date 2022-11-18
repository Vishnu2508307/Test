package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.CitrusAssert;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.helpers.cohort.CohortHelper;
import mercury.helpers.common.GenericMessageHelper;

public class CohortCollaboratorsListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" lists the collaborators for the created cohort$")
    public void listsTheCollaboratorsForTheCreatedCohort(String user) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, CohortHelper.createCohortCollaboratorsListingRequest(
                Variables.interpolate("cohort_id"),
                null
        ));
    }

    @SuppressWarnings("unchecked")
    @Then("^the following cohort collaborators are listed$")
    public void theFollowingCohortCollaboratorsAreListed(final Map<String, String> dataTable) {

        messageOperations.receiveJSON(runner, action -> action
                .validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {

                    @Override
                    public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                        try {
                            Set<String> actualAccountIds = CohortHelper
                                    .getActualAccountIdsFrom((Map<String, Object>) payload.getResponse().get("collaborators"), "accounts");
                            Set<String> actualTeamIds = CohortHelper
                                    .getActualTeamIdsFrom((Map<String, Object>) payload.getResponse().get("collaborators"), "teams");
                            Map<String, Set<String>> expectedAccountsAndTeams = CohortHelper.getExpectedCollaboratorsIdsFrom(dataTable, context);

                            Set<String> expectedAccounts = expectedAccountsAndTeams.get("accounts");
                            Set<String> expectedTeams = expectedAccountsAndTeams.get("teams");
                            assertEquals(expectedAccounts.size() + expectedTeams.size(), payload.getResponse().get("total"));
                            assertEquals(expectedAccounts, actualAccountIds);
                            assertEquals(expectedTeams, actualTeamIds);

                        } catch (IOException e) {
                            CitrusAssert.fail(e.getMessage());
                        }
                    }
                }));
    }

    @Then("^the collaborators listing request is not authorized$")
    public void theCollaboratorsListingRequestIsNotAuthorized() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                GenericMessageHelper.getUnauthorizedResponse("workspace.cohort.collaborator.summary.error")
        ));
    }

    @When("^\"([^\"]*)\" lists \"([^\"]*)\" cohort collaborators$")
    public void listsCohortCollaborators(String user, String limit) throws JsonProcessingException {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, CohortHelper.createCohortCollaboratorsListingRequest(
                Variables.interpolate("cohort_id"),
                Integer.valueOf(limit)
        ));
    }

    @SuppressWarnings("unchecked")
    @Then("^only (\\d+) team and (\\d+) account out of (\\d+) cohort collaborators are listed$")
    public void onlyTeamAndAccountOutOfCohortCollaboratorsAreListed(int teamCount, int accountCount, int total) {
        messageOperations.receiveJSON(runner, action -> action
                .validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
                    @Override
                    public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                        try {
                            Set<String> actualAccountIds = CohortHelper
                                    .getActualAccountIdsFrom((Map<String, Object>) payload.getResponse().get("collaborators"), "accounts");
                            Set<String> actualTeamIds = CohortHelper
                                    .getActualTeamIdsFrom((Map<String, Object>) payload.getResponse().get("collaborators"), "teams");
                            assertEquals(teamCount, actualTeamIds.size());
                            assertEquals(accountCount, actualAccountIds.size());
                            assertEquals(total, payload.getResponse().get("total"));
                        } catch (IOException e) {
                            CitrusAssert.fail(e.getMessage());
                        }
                    }
                }));
    }
}
