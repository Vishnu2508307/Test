package mercury.glue.step.workspace;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.workspace.WorkspaceCreateSteps.WORKSPACE_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.workspace.WorkspaceHelper;

public class WorkspacePermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" grants \"([^\"]*)\" with \"([^\"]*)\" permission level on workspace(?: \"([^\"]*)\")?$")
    public void grantsWithPermissionLevelOnWorkspace(String user, String account, String permission, String workspace) {
        authenticationSteps.authenticateUser(user);

        String workspaceId = Strings.isNullOrEmpty(workspace) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspace);

        messageOperations.sendJSON(runner, WorkspaceHelper.grantWorkspacePermissionRequest(
                interpolate(workspaceId),
                "account",
                Lists.newArrayList(interpolate(getAccountIdVar(account))),
                permission
        ));
    }

    @Then("^\"([^\"]*)\" has \"([^\"]*)\" permission level on workspace(?: \"([^\"]*)\")?$")
    public void hasPermissionLevelOnWorkspace(String account, String permission, String workspace) {
        String workspaceId = Strings.isNullOrEmpty(workspace) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspace);
        messageOperations.receiveJSON(runner, action -> action.payload(WorkspaceHelper.grantWorkspacePermissionResponse(
                Lists.newArrayList(interpolate(getAccountIdVar(account))),
                "account",
                interpolate(workspaceId),
                permission
        )));
    }

    @Given("^\"([^\"]*)\" has granted \"([^\"]*)\" with \"([^\"]*)\" permission level on workspace(?: \"([^\"]*)\")?$")
    public void hasGrantedWithPermissionLevelOnWorkspace(String user, String account, String permission, String workspace) {
        grantsWithPermissionLevelOnWorkspace(user, account, permission, workspace);
        hasPermissionLevelOnWorkspace(account, permission, workspace);
    }

    @Then("^the workspace is not shared due to missing permission level$")
    public void theWorkspaceIsNotSharedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"workspace.permission.grant.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\"'s permission on workspace \"([^\"]*)\"$")
    public void revokesSPermissionOnWorkspace(String user, String account, String workspace) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, WorkspaceHelper.revokeWorkspacePermissionRequest(
                interpolate(getWorkspaceIdVar(workspace)),
                interpolate(getAccountIdVar(account))
        ));
    }

    @Then("^the workspace permission is successfully revoked$")
    public void theWorkspacePermissionIsSuccessfullyRevoked() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"workspace.permission.revoke.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the request is denied due to missing permission level$")
    public void theRequestIsDeniedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"workspace.permission.revoke.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("^\"([^\"]*)\" grants \"([^\"]*)\" permission level on workspace \"([^\"]*)\" to \"([^\"]*)\"$")
    public void grantsPermissionLevelOnWorkspaceTo(String user, String permissionLevel, String workspaceName,
                                                   String collaboratorType, List<String> collaboratorNames) {
        authenticationSteps.authenticateUser(user);

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, WorkspaceHelper.grantWorkspacePermissionRequest(
                interpolate(workspaceId),
                collaboratorType,
                collaboratorNames.stream()
                        .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                        .collect(Collectors.toList()),
                permissionLevel
        ));
    }

    @Then("^the following \"([^\"]*)\" have \"([^\"]*)\" permission level over workspace \"([^\"]*)\"$")
    public void theFollowingHavePermissionLevelOverWorkspace(String collaboratorType, String permissionLevel,
                                                             String workspaceName, List<String> collaboratorNames) {
        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);
        messageOperations.receiveJSON(runner, action -> action.payload(WorkspaceHelper.grantWorkspacePermissionResponse(
                collaboratorNames.stream()
                        .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                        .collect(Collectors.toList()),
                collaboratorType,
                interpolate(workspaceId),
                permissionLevel
        )));
    }

    @And("^\"([^\"]*)\" has granted \"([^\"]*)\" permission level to \"([^\"]*)\" \"([^\"]*)\" over workspace \"([^\"]*)\"$")
    public void hasGrantedPermissionLevelToOverWorkspace(String user, String permissionLevel, String collaboratorType,
                                                         String collaboratorName, String workspaceName) {
        List<String> collaboratorNames = Lists.newArrayList(collaboratorName);
        grantsPermissionLevelOnWorkspaceTo(user, permissionLevel, workspaceName, collaboratorType, collaboratorNames);
        theFollowingHavePermissionLevelOverWorkspace(collaboratorType, permissionLevel, workspaceName, collaboratorNames);
    }

    @When("{string} revokes team permission on workspace {string} for {string} team")
    public void revokesTeamPermissionOnWorkspace(String user, String workspaceName, String teamName) {

        authenticationSteps.authenticateUser(user);

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, WorkspaceHelper.revokeCollaboratorTeamPermission(
                interpolate(workspaceId),
                interpolate(nameFrom(teamName, "id"))
        ));
    }
}
