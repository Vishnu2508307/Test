package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.competency.DocumentItemHelper.learnerDocumentItemLink;
import static mercury.helpers.competency.DocumentItemHelper.learnerDocumentItemsQuery;
import static mercury.helpers.competency.DocumentItemLinkHelper.competencyDocumentItemLinkMutation;
import static mercury.helpers.competency.DocumentItemLinkHelper.competencyDocumentItemUnlinkMutation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class DocumentItemLinkSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Given("^\"([^\"]*)\" has linked to \"([^\"]*)\" \"([^\"]*)\" competency items from document \"([^\"]*)\"$")
    public void hasLinkedToCompetencyItemsFromDocument(String user, String elementType, String elementName,
                                                       String documentName, List<String> documentItemNames) {

        linksToCompetencyItemsFromDocument(user, elementType, elementName, documentName, documentItemNames);

        String basePath = "$.response.data.competencyDocumentItemLink.documentLink";

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(basePath + ".elementId", interpolate(nameFrom(elementName, "id")))
                .jsonPath(basePath + ".elementType", elementType)
                .jsonPath(basePath + ".documentItems", "@notEmpty()@"));
    }

    @When("^\"([^\"]*)\" unlinks from \"([^\"]*)\" \"([^\"]*)\" competency items from document \"([^\"]*)\"$")
    public void unlinksFromCompetencyItemsFromDocument(String user, String elementType, String elementName,
                                                       String documentName, List<String> documentItemNames) {

        authenticationSteps.authenticateUser(user);

        List<String> documentItemIds = documentItemNames.stream()
                .map(one -> interpolate(nameFrom(one, "id")))
                .collect(Collectors.toList());

        messageOperations.sendGraphQL(runner, competencyDocumentItemUnlinkMutation(
                elementType,
                interpolate(nameFrom(elementName, "id")),
                interpolate(nameFrom(documentName, "id")),
                documentItemIds
        ));
    }

    @Then("^the competency item is successfully \"([^\"]*)\"$")
    public void theCompetencyItemIsSuccessfully(String linkAction) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.data.competencyDocumentItem" + linkAction, "@notEmpty()@"));
    }

    @When("^\"([^\"]*)\" links to \"([^\"]*)\" \"([^\"]*)\" competency items from document \"([^\"]*)\"$")
    public void linksToCompetencyItemsFromDocument(String user, String elementType, String elementName,
                                                   String documentName, List<String> documentItemNames) {
        authenticationSteps.authenticateUser(user);

        List<String> documentItemIds = documentItemNames.stream()
                .map(one -> interpolate(nameFrom(one, "id")))
                .collect(Collectors.toList());

        messageOperations.sendGraphQL(runner, competencyDocumentItemLinkMutation(
                elementType,
                interpolate(nameFrom(elementName, "id")),
                interpolate(nameFrom(documentName, "id")),
                documentItemIds
        ));
    }

    @Then("^the competency item is not \"([^\"]*)\" due to missing permission level$")
    public void theCompetencyItemIsNotDueToMissingPermissionLevel(String linkAction) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.errors[0].extensions.type", "FORBIDDEN")
                        .jsonPath("$.response.errors[0].extensions.code", HttpStatus.SC_FORBIDDEN));    }

    @Then("^\"([^\"]*)\" can also unlink from \"([^\"]*)\" \"([^\"]*)\" competency items from document \"([^\"]*)\"$")
    public void canAlsoUnlinkFromCompetencyItemsFromDocument(String user, String elementType, String elementName,
                                                             String documentName, List<String> documentItemNames) {
        unlinksFromCompetencyItemsFromDocument(user, elementType, elementName, documentName, documentItemNames);
        theCompetencyItemIsSuccessfully("Unlink");
    }

    @And("^document item \"([^\"]*)\" is published for deployment \"([^\"]*)\"$")
    public void documentItemIsPublishedForDeployment(String documentItemName, String deploymentName) {
        messageOperations.sendGraphQL(runner, learnerDocumentItemLink(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id"))
        ));

        String linkedItemsPath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.linkedDocumentItems";
        String expectedEdgePath = linkedItemsPath + ".edges[0].node.id";

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(linkedItemsPath, "@notEmpty()@")
                .jsonPath(expectedEdgePath, interpolate(nameFrom(documentItemName, "id"))));
    }

    @SuppressWarnings("unchecked")
    @And("^published document \"([^\"]*)\" should contain the following document items$")
    public void publishedDocumentShouldContainTheFollowingDocumentItems(String documentName, List<String> documentItemNames) {

        messageOperations.sendGraphQL(runner, learnerDocumentItemsQuery(interpolate(nameFrom(documentName, "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                final Map response = (Map) payload.get("response");
                final Map data = (Map) response.get("data");
                final Map learn = (Map) data.get("learn");
                final Map learnerDocument = (Map) learn.get("learnerDocument");
                final Map documentItems = (Map) learnerDocument.get("documentItems");
                final List<Map> edges = (List<Map>) documentItems.get("edges");

                final Set<String> documentItemIds = edges.stream()
                        .map(one -> {
                            Map node = (Map) one.get("node");
                            return (String) node.get("id");
                        })
                        .collect(Collectors.toSet());

                final Set<String> expectedDocumentItemIds = documentItemNames.stream()
                        .map(documentItemName -> context.getVariable(nameFrom(documentItemName, "id"), String.class))
                        .collect(Collectors.toSet());

                assertEquals(expectedDocumentItemIds, documentItemIds);
            }
        }));
    }
}
