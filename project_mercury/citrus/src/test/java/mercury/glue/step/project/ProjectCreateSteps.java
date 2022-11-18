package mercury.glue.step.project;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.workspace.WorkspaceHelper.getWorkspaceIdVar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class ProjectCreateSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates a project named {string} in workspace {string}")
    public void createsAProjectNamedInWorkspace(String accountName, String projectName, String workspaceName) {

        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.create")
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .addField("name", projectName)
                .build());
    }

    @Then("project {string} is created successfully")
    public void projectIsCreatedSuccessfully(String projectName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.create.ok")
                .jsonPath("$.response.project.name", projectName)
                .jsonPath("$.response.project.createdAt", "@notEmpty()@")
                .jsonPath("$.response.project.workspaceId", "@notEmpty()@")
                .extractFromPayload("$.response.project.id", nameFrom(projectName, "id"))
        );
    }

    @Given("{string} has created project {string} in workspace {string}")
    public void hasCreatedProjectInWorkspace(String accountName, String projectName, String workspaceName) {
        createsAProjectNamedInWorkspace(accountName, projectName, workspaceName);
        projectIsCreatedSuccessfully(projectName);
    }

    @When("{string} replaces project {string} name with {string}")
    public void replacesProjectNameWith(String accountName, String projectName, String newProjectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.replace")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("name", newProjectName)
                .build());
    }

    @Then("the project name is replaced with {string}")
    public void theProjectNameIsReplacedWith(String projectName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.replace.ok")
                .extractFromPayload("$.response.projectId", nameFrom(projectName, "id"))
        );    }

    @When("{string} deletes project {string}")
    public void deletesProject(String accountName, String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.delete")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @Then("project {string} is deleted successfully")
    public void projectIsDeletedSuccessfully(String projectName) {
        messageOperations.validateResponseType(runner, "workspace.project.delete.ok");
    }

    @Then("{string} can replace project {string} name with {string}")
    public void canReplaceProjectNameWith(final String accountName, final String projectName, final String newProjectName) {
        replacesProjectNameWith(accountName, projectName, newProjectName);
        theProjectNameIsReplacedWith(newProjectName);
    }

    @Then("{string} can delete project {string}")
    public void canDeleteProject(String accountName, String projectName) {
        deletesProject(accountName, projectName);
        projectIsDeletedSuccessfully(projectName);
    }

    @And("{string} has deleted project {string}")
    public void hasDeletedProjectFromWorkspace(final String accountName, final String projectName) {
        deletesProject(accountName, projectName);
        projectIsDeletedSuccessfully(projectName);
    }

    @When("{string} move project {string} to workspace {string}")
    public void moveProjectToWorkspace(String accountName, String projectName, String workspaceName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.move")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("workspaceId", interpolate(getWorkspaceIdVar(workspaceName)))
                .build());
    }

    @Then("project {string} is moved successfully")
    public void projectIsMovedSuccessfully(String projectName) {
        messageOperations.validateResponseType(runner, "workspace.project.move.ok");
    }
}
