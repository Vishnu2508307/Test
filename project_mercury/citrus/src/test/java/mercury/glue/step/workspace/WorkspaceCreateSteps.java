package mercury.glue.step.workspace;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.workspace.WorkspaceHelper;

public class WorkspaceCreateSteps {

    private static final String DEFAULT_NAME = "Citrus Workspace";
    private static final String DEFAULT_DESCRIPTION = "Above Man’s war-wracked world a veteran throng\n" +
            "Of singing spirits gather in the air,\n" +
            "Called from the Poets’ Heaven to take their share\n" +
            "In Right’s impending victory over Wrong";

    public static final String DEFAULT_WORKSPACE_NAME = "workspace";
    public static final String WORKSPACE_ID_VAR = nameFrom(DEFAULT_WORKSPACE_NAME, "id");

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates workspace \"([^\"]*)\"$")
    public void createsWorkspace(String user, String workspaceName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, WorkspaceHelper.createWorkspaceRequest(workspaceName, DEFAULT_DESCRIPTION));
    }

    @Then("^workspace \"([^\"]*)\" is successfully created$")
    public void theWorkspaceIsSuccessfullyCreated(String workspaceName) {
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.workspace.id", getWorkspaceIdVar(workspaceName))
                        .payload(WorkspaceHelper.createWorkspaceResponse(workspaceName)));
    }

    @Then("^the workspace is not created due to missing permission level$")
    public void theWorkspaceIsNotCreatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.code", HttpStatus.SC_UNAUTHORIZED)
                .jsonPath("$.message", "Unauthorized: Unauthorized permission level"));
    }

    @Given("^\"([^\"]*)\" has created workspace \"([^\"]*)\"$")
    public void hasCreatedWorkspace(String user, String workspace) {
        createsWorkspace(user, workspace);
        theWorkspaceIsSuccessfullyCreated(workspace);
    }

    @Given("^\"([^\"]*)\" has created a workspace$")
    public void hasCreatedAWorkspace(String accountName) {
        createsWorkspace(accountName, DEFAULT_NAME);
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.workspace.id", WORKSPACE_ID_VAR));
    }

    @When("^\"([^\"]*)\" tries updating workspace \"([^\"]*)\" with$")
    public void triesUpdatingWorkspaceWith(String user, String workspace, Map<String, String> fields) {

        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, WorkspaceHelper.updateWorkspaceRequest(
                Variables.interpolate(getWorkspaceIdVar(workspace)),
                fields
        ));
    }

    @Then("^workspace \"([^\"]*)\" is successfully updated with$")
    public void workspaceIsSuccessfullyUpdatedWith(String workspace, Map<String, String> dataTable) {
        messageOperations.receiveJSON(runner, action->action.payload(WorkspaceHelper.updateWorkspaceResponse(
                Variables.interpolate(getWorkspaceIdVar(workspace)),
                workspace,
                dataTable.getOrDefault("description", null)
        )));
    }

    @Then("^the workspace is not updated due to missing permission level$")
    public void theWorkspaceIsNotUpdatedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"workspace.change.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }


    @When("^\"([^\"]*)\" tries deleting workspace \"([^\"]*)\"$")
    public void triesDeletingWorkspace(String user, String workspace) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, WorkspaceHelper.deleteWorkspaceRequest(
                Variables.interpolate(getWorkspaceIdVar(workspace)), workspace, interpolate(getAccountIdVar(user)),
                interpolate(getSubscriptionIdVar(user))
        ));
    }

    @Then("^workspace \"([^\"]*)\" is successfully deleted")
    public void theWorkspaceIsSuccessfullyDeleted(String workspaceName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"workspace.delete.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the workspace is not deleted due to missing permission level$")
    public void theWorkspaceIsNotDeletedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                       "\"type\":\"workspace.delete.error\"," +
                       "\"code\":401," +
                       "\"message\":\"@notEmpty()@\"," +
                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
