package mercury.glue.step.competency;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.competency.DocumentItemHelper.competencyDocumentCreateMutation;
import static mercury.helpers.competency.DocumentItemHelper.competencyDocumentItemCreateMutation;
import static mercury.helpers.competency.DocumentItemHelper.competencyDocumentItemDeleteMutation;
import static mercury.helpers.competency.DocumentItemHelper.competencyDocumentItemUpdateMutation;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class DocumentItemSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @And("^\"([^\"]*)\" has created a competency document \"([^\"]*)\" in workspace \"([^\"]*)\"$")
    public void hasCreatedACompetencyDocumentInWorkspace(String user, String documentName, String workspaceName) {

        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, competencyDocumentCreateMutation(
                interpolate(nameFrom(workspaceName, "workspace_id")),
                documentName,
                Lists.newArrayList("documentId")
        ));

        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.data.competencyDocumentCreate.document.documentId",
                        nameFrom(documentName, "id")));
    }



    @When("^\"([^\"]*)\" creates a document item \"([^\"]*)\" for \"([^\"]*)\" document with$")
    public void createsADocumentItemForDocumentWith(String user, String itemName, String documentName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(user);

        List<String> selectedFields = Lists.newArrayList("id");
        selectedFields.addAll(fields.keySet());

        messageOperations.sendGraphQL(runner, competencyDocumentItemCreateMutation(
                interpolate(nameFrom(documentName, "id")),
                fields,
                selectedFields
        ));
    }

    @Then("^the document item \"([^\"]*)\" is created successfully$")
    public void theDocumentItemIsCreatedSuccessfully(String itemName) {
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload("$.response.data.competencyDocumentItemCreate.documentItem.id",
                        nameFrom(itemName, "id")));
    }

    @Given("^\"([^\"]*)\" has created a document item \"([^\"]*)\" for \"([^\"]*)\" document with$")
    public void hasCreatedADocumentItemForDocumentWith(String user, String itemName, String documentName,
                                                       Map<String, String> fields) {
        createsADocumentItemForDocumentWith(user, itemName, documentName, fields);
        theDocumentItemIsCreatedSuccessfully(itemName);
    }

    @Given("^\"([^\"]*)\" has created a document item \"([^\"]*)\" for \"([^\"]*)\" document$")
    public void hasCreatedADocumentItemForDocument(String user, String itemName, String documentName) {
        Map<String, String> fields = Maps.newHashMap();
        fields.put("fullStatement", itemName);

        createsADocumentItemForDocumentWith(user, itemName, documentName, fields);
        theDocumentItemIsCreatedSuccessfully(itemName);
    }

    @When("^\"([^\"]*)\" updates document item \"([^\"]*)\" for \"([^\"]*)\" document with$")
    public void updatesDocumentItemForDocumentWith(String user, String itemName, String documentName,
                                                   Map<String, String> fields) {

        authenticationSteps.authenticateUser(user);

        List<String> selectedFields = Lists.newArrayList("id");
        selectedFields.addAll(fields.keySet());

        messageOperations.sendGraphQL(runner, competencyDocumentItemUpdateMutation(
                interpolate(nameFrom(itemName, "id")),
                interpolate(nameFrom(documentName, "id")),
                fields,
                selectedFields
        ));
    }

    @Then("^the document item \"([^\"]*)\" is updated successfully with$")
    public void theDocumentItemIsUpdatedSuccessfullyWith(String itemName, Map<String, String> fields) {
        String documentItemPath = "$.response.data.CompetencyDocumentItemUpdate.documentItem.";
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(documentItemPath + "id", interpolate(nameFrom(itemName, "id")))
                        .jsonPath(documentItemPath + "fullStatement", fields.get("fullStatement"))
                        .jsonPath(documentItemPath + "abbreviatedStatement", fields.get("abbreviatedStatement"))
                        .jsonPath(documentItemPath + "humanCodingScheme", fields.get("humanCodingScheme")));
    }

    @When("^\"([^\"]*)\" deletes document item \"([^\"]*)\" for \"([^\"]*)\" document$")
    public void deletesDocumentItemForDocument(String user, String itemName, String documentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, competencyDocumentItemDeleteMutation(
                interpolate(nameFrom(itemName, "id")),
                interpolate(nameFrom(documentName, "id"))
        ));
    }

    @Then("^document item \"([^\"]*)\" for \"([^\"]*)\" document is successfully deleted$")
    public void documentItemForDocumentIsSuccessfullyDeleted(String itemName, String documentName) {
        String documentItemPath = "$.response.data.competencyDocumentItemDelete.documentItem.";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(documentItemPath + "id", interpolate(nameFrom(itemName, "id")))
                .jsonPath(documentItemPath + "documentId", interpolate(nameFrom(documentName, "id"))));
    }

    @Then("^the document item is not \"([^\"]*)\" due to missing permission level$")
    public void theDocumentItemIsNotDueToMissingPermissionLevel(String actionTaken) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.errors[0].extensions.type", "FORBIDDEN")
                        .jsonPath("$.response.errors[0].extensions.code", HttpStatus.SC_FORBIDDEN));
    }

    @Given("^\"([^\"]*)\" has subscribed to document \"([^\"]*)\" via \"([^\"]*)\" client$")
    public void hasSubscribedToDocumentViaClient(String user, String documentName, String clientName) {

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.competency.document.subscribe")
                .addField("documentId", interpolate(nameFrom(documentName, "id")))
                .build(), clientName);

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "workspace.competency.document.subscribe.ok"),
                clientName);
    }

    @Then("^\"([^\"]*)\" will receive document item \"([^\"]*)\" payload via \"([^\"]*)\" client$")
    public void willReceiveDocumentItemPayloadViaClient(String user, String itemName, String clientName) {
        messageOperations.receiveJSON(runner, action -> action
                        .jsonPath("$.type", "workspace.competency.document.broadcast")
                        .jsonPath("$.response.data.id", interpolate(nameFrom(itemName, "id"))),
                clientName);
    }

    @When("^\"([^\"]*)\" has updated document item \"([^\"]*)\" for document \"([^\"]*)\" with$")
    public void hasUpdatedDocumentItemForDocumentWith(String user, String itemName, String documentName,
                                                      Map<String, String> fields) {
        updatesDocumentItemForDocumentWith(user, itemName, documentName, fields);
        theDocumentItemIsUpdatedSuccessfullyWith(itemName, fields);
    }

    @When("^\"([^\"]*)\" has deleted document item \"([^\"]*)\" for \"([^\"]*)\" document$")
    public void hasDeletedDocumentItemForDocument(String user, String itemName, String documentName) {
        deletesDocumentItemForDocument(user, itemName, documentName);
        documentItemForDocumentIsSuccessfullyDeleted(itemName, documentName);
    }

    @Then("^\"([^\"]*)\" received association \"([^\"]*)\" payload via \"([^\"]*)\" client$")
    public void receivedAssociationPayloadViaClient(String user, String associationName, String clientName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.competency.document.broadcast")
                .jsonPath("$.response.data.id", interpolate(nameFrom(associationName, "id"))),
                clientName);
    }

    @Then("^\"([^\"]*)\" received association \"([^\"]*)\" id and \"([^\"]*)\" document id via \"([^\"]*)\" client$")
    public void receivedAssociationIdAndDocumentIdViaClient(String user, String associationName, String documentName,
                                                            String clientName) {
        messageOperations.receiveJSON(runner, action -> action
                        .jsonPath("$.type", "workspace.competency.document.broadcast")
                        .jsonPath("$.response.data.id", interpolate(nameFrom(associationName, "id")))
                        .jsonPath("$.response.data.documentId", interpolate(nameFrom(documentName, "id"))),
                clientName);
    }
}
