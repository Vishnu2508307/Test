package mercury.glue.step.competency;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.competency.DocumentHelper.competencyDocumentDeleteMutation;
import static mercury.helpers.competency.DocumentHelper.competencyDocumentUpdateMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class DocumentSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} has deleted a document {string} in workspace {string}")
    public void hasDeletedACompetencyDocumentInWorkspace(String user, String documentName, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, competencyDocumentDeleteMutation(
                interpolate(nameFrom(workspaceName, "workspace_id")),
                interpolate(nameFrom(documentName, "id"))
        ));
    }

    @Then("document {string} in workspace {string} is successfully deleted")
    public void documentInWorkspaceIsSuccessfullyDeleted(String documentName, String workspaceName) {
        String documentPath = "$.response.data.competencyDocumentDelete.document.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(documentPath + "workspaceId", interpolate(nameFrom(workspaceName, "workspace_id")))
                .jsonPath(documentPath + "documentId", interpolate(nameFrom(documentName, "id"))));
    }

    @When("{string} updates the document {string} in workspace {string} with")
    public void updatesDocumentWith(String user, String documentName, String workspaceName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(user);

        List<String> selectedFields = new ArrayList<>();
        selectedFields.addAll(fields.keySet());

        messageOperations.sendGraphQL(runner, competencyDocumentUpdateMutation(
                interpolate(nameFrom(workspaceName, "workspace_id")),
                interpolate(nameFrom(documentName, "id")),
                fields,
                selectedFields
        ));
    }

    @Then("document {string} in workspace {string} is successfully updated with")
    public void documentInWorkspaceIsSuccessfullyUpdated(String documentName, String workspaceName, Map<String , String> fields) {
        String documentPath = "$.response.data.competencyDocumentUpdate.document.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(documentPath + "workspaceId", interpolate(nameFrom(workspaceName, "workspace_id")))
                .jsonPath(documentPath + "documentId", interpolate(nameFrom(documentName, "id")))
                .jsonPath(documentPath + "title", fields.get("title")));
    }

    @Then("^\"([^\"]*)\" will receive document \"([^\"]*)\" payload via \"([^\"]*)\" client$")
    public void willReceiveDocumentPayloadViaClient(String user, String documentName, String clientName) {
        messageOperations.receiveJSON(runner, action -> action
                        .jsonPath("$.type", "workspace.competency.document.broadcast")
                        .jsonPath("$.response.data.documentId", interpolate(nameFrom(documentName, "id"))),
                clientName);
    }
}
