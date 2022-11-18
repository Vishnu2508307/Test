package mercury.glue.step.project;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.project.ProjectHelper.summaryProjectAccountsRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;

public class ProjectCollaborators {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" fetches the accounts for project \"([^\"]*)\"$")
    public void fetchesTheAccountsForProject(String accountName, String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner,
                summaryProjectAccountsRequest(Variables.interpolate(Variables.nameFrom(projectName, "id")), null));
    }

    @When("^\"([^\"]*)\" fetches the accounts for project \"([^\"]*)\" with limit (\\d+)$")
    public void fetchesTheAccountsForProjectWithLimit(String accountName, String projectName, int limit) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner,
                summaryProjectAccountsRequest(Variables.interpolate(Variables.nameFrom(projectName, "id")), limit));
    }

    @SuppressWarnings("unchecked")
    @Then("^the project collaborators list contains$")
    public void theWorkspaceCollaboratorsListContains(Map<String, String> expectedCollaborators) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new ResponseMessageValidationCallback<Object>(Object.class) {
            @Override
            public void validate(Object payload, Map<String, Object> headers, TestContext context) {

                List<String> accountColl = expectedCollaborators.entrySet().stream()
                        .filter(one -> one.getValue().trim().equals("account"))
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                List<String> teamColl = expectedCollaborators.entrySet().stream()
                        .filter(one -> one.getValue().trim().equals("team"))
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                assertNotNull(accountColl);
                assertNotNull(teamColl);

                Map<String, Object> collaborators = (Map<String, Object>) payload;

                List<Object> actualAccountCollaborators = (List<Object>) collaborators.get("accounts");
                List<Object> actualTeamCollaborators = (List<Object>) collaborators.get("teams");

                if (!accountColl.isEmpty()) {
                    assertEquals(accountColl.size(), actualAccountCollaborators.size());
                }

                if (!teamColl.isEmpty()) {
                    assertEquals(teamColl.size(), actualTeamCollaborators.size());
                }
            }

            @Override
            public String getRootElementName() {
                return "collaborators";
            }

            @Override
            public String getType() {
                return "workspace.project.collaborator.summary.ok";
            }
        }));
    }

}