package mercury.glue.step.competency;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.collaborator.CollaboratorHelper.validateCollaboratorsResponse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class DocumentPermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @And("^\"([^\"]*)\" has shared the \"([^\"]*)\" document with \"([^\"]*)\" as ([^\"]*)$")
    public void hasSharedTheDocumentWithAs(String user, String documentName, String userToGrant, String permissionLevel) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, documentGrantPermissionMessage(
                interpolate(documentName, "id"),
                permissionLevel.toUpperCase(),
                interpolate(getAccountIdVar(userToGrant))
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(
                documentGrantPermissionResponse(
                        interpolate(documentName, "id"),
                        permissionLevel,
                        interpolate(getAccountIdVar(userToGrant))
                )));
    }

    public static String documentGrantPermissionMessage(String documentId, String permissionLevel, String... accountIds) {
        return new PayloadBuilder()
                .addField("type", "workspace.competency.permission.grant")
                .addField("documentId", documentId)
                .addField("accountIds", accountIds)
                .addField("permissionLevel", permissionLevel)
                .build();
    }

    public static String documentGrantPermissionResponse(String documentId, String permissionLevel, String... accountIds) {
        String accountArray = String.join(",", Lists.newArrayList(accountIds));
        return "{" +
                "\"type\":\"workspace.competency.permission.grant.ok\"," +
                "\"response\":{" +
                "\"accountIds\":\"@assertThat(containsInAnyOrder(" + accountArray + "))@\"," +
                "\"documentId\":\"" + documentId + "\"," +
                "\"permissionLevel\":\"" + permissionLevel + "\"" +
                "}," +
                "\"replyTo\":\"@notEmpty()@\"" +
                "}";
    }

    @And("^\"([^\"]*)\" has shared the \"([^\"]*)\" document with team \"([^\"]*)\" as ([^\"]*)$")
    public void hasSharedTheDocumentWithTeamAsCONTRIBUTOR(String user, String documentName, String teamName, String permissionLevel) {
        authenticationSteps.authenticateUser(user);

        String request = new PayloadBuilder()
                .addField("type", "workspace.competency.permission.grant")
                .addField("documentId", interpolate(nameFrom(documentName, "id")))
                .addField("teamIds", Lists.newArrayList(interpolate(nameFrom(teamName, "id"))))
                .addField("permissionLevel", permissionLevel)
                .build();

        messageOperations.sendJSON(runner, request);

        messageOperations.validateResponseType(runner, "workspace.competency.permission.grant.ok");
    }

    @When("^\"([^\"]*)\" lists all the collaborators for document \"([^\"]*)\"$")
    public void listsAllTheCollaboratorsForDocument(String account, String documentName) {
        authenticationSteps.authenticateUser(account);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.competency.collaborator.summary")
                .addField("documentId", interpolate(nameFrom(documentName, "id")))
                .build());
    }

    @Then("^the following document collaborators are returned$")
    public void theFollowingDocumentCollaboratorsAreReturned(Map<String, String> collaborators) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                validateCollaboratorsResponse(collaborators, "workspace.competency.collaborator.summary.ok")
        ));
    }
}
