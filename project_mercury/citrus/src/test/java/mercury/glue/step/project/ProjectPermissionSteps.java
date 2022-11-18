package mercury.glue.step.project;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class ProjectPermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Autowired
    private ProjectCreateSteps projectCreateSteps;

    @Given("{string} has granted {string} permission level to {string} {string} over project {string}")
    public void hasGrantedPermissionLevelToOverProject(final String accountName, final String permissionLevel,
                                                       final String entityType, final String entityName,
                                                       final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        final String entityId = getEntityId(entityType, entityName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.permission.grant")
                .addField(String.format("%sIds", entityType), Lists.newArrayList(interpolate(entityId)))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("permissionLevel", permissionLevel)
                .build());

        messageOperations.validateResponseType(runner, "workspace.project.permission.grant.ok");
    }

    @Then("{string} is not allowed to replace project {string} name with {string}")
    public void isNotAllowedToReplaceProjectNameWith(final String accountName, final String projectName,
                                                     final String newProjectName) {
        projectCreateSteps.replacesProjectNameWith(accountName, projectName, newProjectName);
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.replace.error")
                .jsonPath("$.code", 401)
        );
    }

    @Then("{string} is not allowed to delete project {string}")
    public void isNotAllowedToDeleteProject(final String accountName, final String projectName) {
        projectCreateSteps.deletesProject(accountName, projectName);
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.project.delete.error")
                .jsonPath("$.code", 401)
        );
    }

    @Given("{string} has revoked {string} {string} permission level over project {string}")
    public void hasRevokedPermissionLevelOverProject(final String accountName, final String entityType,
                                                     final String entityName, final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.permission.revoke")
                .addField(String.format("%sId", entityType), interpolate(getEntityId(entityType, entityName)))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());

        messageOperations.validateResponseType(runner, "workspace.project.permission.revoke.ok");
    }

    private String getEntityId(final String entityType, final String entityName) {
        return entityType.equals("account") ? getAccountIdVar(entityName): nameFrom(entityName, "id");
    }
}
