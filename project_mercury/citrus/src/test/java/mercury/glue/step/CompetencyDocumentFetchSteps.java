package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.competency.DocumentHelper.fetchCompetencyMet;
import static mercury.helpers.competency.DocumentHelper.fetchDocumentInWorkspaceQuery;
import static mercury.helpers.competency.DocumentHelper.fetchDocumentInWorkspaceValidationCallback;
import static mercury.helpers.competency.DocumentHelper.fetchDocumentQuery;
import static mercury.helpers.competency.DocumentHelper.fetchDocumentValidationCallback;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class CompetencyDocumentFetchSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" fetches competency documents for workspace \"([^\"]*)\"$")
    public void fetchesCompetencyDocumentsForWorkspace(String user, String workspaceName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchDocumentInWorkspaceQuery(interpolate(nameFrom(workspaceName, "workspace_id"))));
    }

    @When("^\"([^\"]*)\" fetches competency documents$")
    public void fetchesCompetencyDocuments(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchDocumentQuery());
    }

    @Then("^the following documents are returned in workspace$")
    public void theFollowingDocumentsAreReturnedInWorkspace(Map<String, String> fields) {

        Map<String, String> documentName = fields.keySet().stream()
                .map(key -> new HashMap<String, String>() {{ put(key, key); }
                }).reduce((prev, next) -> {
                    prev.putAll(next);
                    return prev;
                }).orElse(null);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                fetchDocumentInWorkspaceValidationCallback(fields, documentName)));

    }

    @Then("^the following documents are returned$")
    public void theFollowingDocumentsAreReturned(Map<String, String> fields) {

        Map<String, String> documentName = fields.keySet().stream()
                .map(key -> new HashMap<String, String>() {{ put(key, key); }
                }).reduce((prev, next) -> {
                    prev.putAll(next);
                    return prev;
                }).orElse(null);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                fetchDocumentValidationCallback(fields, documentName)));

    }

    @Then("^the no documents are returned for workspace$")
    public void theNoDocumentsAreReturnedForWorkspace() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.workspace.competencyDocuments.edges", Lists.newArrayList()));
    }

    @Then("^the no documents are returned$")
    public void theNoDocumentsAreReturned() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.documents.edges", Lists.newArrayList()));
    }

    @SuppressWarnings("unchecked")
    @And("^\"([^\"]*)\" should have been awarded the following competency items from \"([^\"]*)\" document$")
    public void shouldHaveBeenAwardedTheFollowingCompetencyItemsFromDocument(final String student,
                                                                             final String documentName,
                                                                             final Map<String, String> awarded) {
        authenticationSteps.authenticateUser(student);

        messageOperations.sendGraphQL(runner, fetchCompetencyMet(
                interpolate(getAccountIdVar(student)),
                interpolate(nameFrom(documentName, "id"))
        ));

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                final Map<String, Double> expectedAwardedItems = awarded.entrySet().stream()
                        .map(entry -> {
                            Map<String, Double> map = new HashMap<>();
                            map.put(context.getVariable(interpolate(nameFrom(entry.getKey(), "id"))), Double.valueOf(entry.getValue()));
                            return map;
                        }).reduce((prev, next) -> {
                            prev.putAll(next);
                            return prev;
                        }).orElse(null);

                assertNotNull(expectedAwardedItems);

                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map learn = (Map) data.get("learn");
                Map competencyDocumentMet = (Map) learn.get("competencyDocumentMet");
                List<Map> edges = (List<Map>) competencyDocumentMet.get("edges");

                assertEquals(2, edges.size());

                edges.forEach(edge -> {
                    Map node = (Map) edge.get("node");
                    String documentItemId = (String) node.get("documentItemId");
                    Double value = (Double) node.get("value");

                    Double expectedValue = expectedAwardedItems.get(documentItemId);

                    assertNotNull(expectedValue);
                    assertEquals(expectedValue, value);
                });
            }
        }));
    }
}
