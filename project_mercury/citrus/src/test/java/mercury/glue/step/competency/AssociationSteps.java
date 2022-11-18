package mercury.glue.step.competency;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.competency.AssociationHelper.competencyItemAssociationCreateMutation;
import static mercury.glue.step.competency.AssociationHelper.competencyItemAssociationDeleteMutation;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.competency.DocumentItemHelper.learnerDocumentItemAssociationsQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class AssociationSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates an association \"([^\"]*)\" for \"([^\"]*)\" document with$")
    public void createsAnAssociationForDocumentWith(String user, String assocName, String documentName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(user);

        List<String> selectedFields = Lists.newArrayList("id", "createdAt", "createdById");
        selectedFields.addAll(fields.keySet());

        messageOperations.sendGraphQL(runner, competencyItemAssociationCreateMutation(
                interpolate(nameFrom(documentName, "id")),
                fields,
                selectedFields
        ));
    }

    @Then("^the association \"([^\"]*)\" is created successfully$")
    public void theAssociationIsCreatedSuccessfully(String assocName) {
        String path = "$.response.data.competencyItemAssociationCreate.association";
        messageOperations.receiveJSON(runner, action ->
                action.extractFromPayload(path + ".id", nameFrom(assocName, "id"))
                        .jsonPath(path + ".createdAt", "@notEmpty()@")
                        .jsonPath(path + ".createdById", "@notEmpty()@"));
    }

    @Given("^\"([^\"]*)\" has created an association \"([^\"]*)\" for \"([^\"]*)\" document with$")
    public void hasCreatedAnAssociationForDocumentWith(String user, String assocName, String documentName, Map<String, String> fields) {
        createsAnAssociationForDocumentWith(user, assocName, documentName, fields);
        theAssociationIsCreatedSuccessfully(assocName);
    }

    @When("^\"([^\"]*)\" deletes an association \"([^\"]*)\" for \"([^\"]*)\" document$")
    public void deletesAnAssociationForDocument(String user, String assocName, String documentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, competencyItemAssociationDeleteMutation(
                interpolate(nameFrom(assocName, "id")),
                interpolate(nameFrom(documentName, "id"))
        ));
    }

    @Then("^the association \"([^\"]*)\" is deleted successfully$")
    public void theAssociationIsDeletedSuccessfully(String assocName) {
        String path = "$.response.data.competencyItemAssociationDelete";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(path + ".associationId", interpolate(nameFrom(assocName, "id")))
                .jsonPath(path + ".documentId", "@notEmpty()@"));
    }

    @Then("^the association is not \"([^\"]*)\" due to missing permission level$")
    public void theAssociationIsNotDueToMissingPermissionLevel(String actionTaken) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.errors[0].extensions.type", "FORBIDDEN")
                        .jsonPath("$.response.errors[0].extensions.code", HttpStatus.SC_FORBIDDEN));
    }

    @When("^\"([^\"]*)\" has deleted association \"([^\"]*)\" for \"([^\"]*)\" document$")
    public void hasDeletedAssociationForDocument(String user, String associationName, String documentName) {
        deletesAnAssociationForDocument(user, associationName, documentName);
        theAssociationIsDeletedSuccessfully(associationName);
    }

    @SuppressWarnings("unchecked")
    @And("^published document \"([^\"]*)\" should contain the following associations$")
    public void publishedDocumentShouldContainTheFollowingAssociations(final String documentName, final Map<String, String> associations) {
        messageOperations.sendGraphQL(runner, learnerDocumentItemAssociationsQuery(interpolate(nameFrom(documentName, "id"))));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                final Map response = (Map) payload.get("response");
                final Map data = (Map) response.get("data");
                final Map learn = (Map) data.get("learn");
                final Map learnerDocument = (Map) learn.get("learnerDocument");
                final Map documentItems = (Map) learnerDocument.get("documentItems");
                final List<Map> edges = (List<Map>) documentItems.get("edges");

                final Map<String, String> itemIdAssociationIdMap = associations.entrySet().stream()
                        .map(entry -> {
                            final String key = context.getVariable(nameFrom(entry.getKey(), "id"), String.class);
                            final String value = context.getVariable(nameFrom(entry.getValue(), "id"), String.class);
                            final Map<String, String> current = new HashMap<>();
                            current.put(key, value);
                            return current;
                        }).reduce((prev, next) -> {
                            prev.putAll(next);
                            return prev;
                        }).orElse(null);

                assertNotNull(itemIdAssociationIdMap);

                edges.forEach(edge -> {
                    final Map node = (Map) edge.get("node");
                    final String itemId = (String) node.get("id");

                    final String expectedAssociationId = itemIdAssociationIdMap.get(itemId);

                    if (expectedAssociationId != null) {
                        final Map associationsTo = (Map) node.get("associationsTo");
                        final Map associationsFrom = (Map) node.get("associationsFrom");

                        final List<Map> toEdges = (List<Map>) associationsTo.get("edges");
                        final List<Map> fromEdges = (List<Map>) associationsFrom.get("edges");

                        if (!toEdges.isEmpty()) {
                            verifyAssociation(expectedAssociationId, toEdges);
                        }

                        if (!fromEdges.isEmpty()) {
                            verifyAssociation(expectedAssociationId, fromEdges);
                        }
                    }
                });
            }
        }));
    }

    private void verifyAssociation(String expectedAssociationId, List<Map> edges) {
        final Map edge = edges.get(0);
        final Map node = (Map) edge.get("node");
        final String actualAssociationId = (String) node.get("associationId");

        assertEquals(expectedAssociationId, actualAssociationId);
    }
}
