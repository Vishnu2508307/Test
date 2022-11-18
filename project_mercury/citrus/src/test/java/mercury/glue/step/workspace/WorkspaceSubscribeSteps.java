package mercury.glue.step.workspace;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.workspace.WorkspaceCreateSteps.WORKSPACE_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.base.Strings;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class WorkspaceSubscribeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} subscribe to the workspace {string} via a {string} client successfully")
    public void subscribeToTheWorkspaceViaAClient(String accountName, String workspaceName, String clientName) {

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.subscribe")
                .addField("workspaceId", interpolate(workspaceId))
                .build(), clientName);
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("workspace.subscribe.ok")),
                                      clientName);
    }

    @Given("{string} cannot subscribe to the workspace {string} via a {string} client due to missing permission level")
    public void cannotSubscribeToTheWorkspaceViaAClientDueToMissingPermissionLevel(String accountName,
                                                                                   String workspaceName,
                                                                                   String clientName) {

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.subscribe")
                .addField("workspaceId", interpolate(workspaceId))
                .build(), clientName);
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("workspace.subscribe.error")),
                                      clientName);
    }

    @When("{string} unsubscribe to the workspace {string} via a {string} client successfully")
    public void unsubscribeToTheWorkspace(String accountName, String workspaceName, String clientName) {
        authenticationSteps.authenticateUser(accountName);

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.unsubscribe")
                .addField("workspaceId", interpolate(workspaceId))
                .build(), clientName);
        messageOperations.validateResponseType(runner, "workspace.unsubscribe.ok", clientName, action -> {
        });
    }

    @And("{string} cannot unsubscribe to the workspace {string} via a {string} client due to missing permission level")
    public void cannotUnsubscribeToTheWorkspaceViaAClientDueToMissingPermissionLevel(String user,
                                                                                     String workspaceName,
                                                                                     String clientName) {

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.unsubscribe")
                .addField("workspaceId", interpolate(workspaceId))
                .build(), clientName);
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("workspace.unsubscribe.error")),
                                      clientName);
    }

    @Then("{string} should receive an action {string} for project {string} and workspace {string}")
    public void shouldReceiveAnActionForProjectAndWorkspace(String clientName,
                                                            String actionName,
                                                            String projectName,
                                                            String workspaceName) {

        String workspaceId = Strings.isNullOrEmpty(workspaceName) ? WORKSPACE_ID_VAR : getWorkspaceIdVar(workspaceName);
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.projectId", interpolate(nameFrom(projectName, "id")))
                .jsonPath("$.response.action", actionName)
                .jsonPath("$.response.workspaceId", interpolate(workspaceId)), clientName);
    }
}
