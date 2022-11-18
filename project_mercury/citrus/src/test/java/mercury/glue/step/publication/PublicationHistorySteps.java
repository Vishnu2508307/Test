package mercury.glue.step.publication;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.datastax.driver.core.utils.UUIDs;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertTrue;

public class PublicationHistorySteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("user requests a list of publications for a activity")
    public void userRequestsAListOfPublicationsForAActivity() {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.history.fetch");
        payload.addField("activityId", "b2e905d0-91eb-11eb-9a79-2d567e2fc555");
        messageOperations.sendJSON(runner, payload.build());
    }

    @Given("{string} has created publication {string} for activity {string} with version {string}")
    public void hasCreatedPublicationForActivity(String accountName, String title, String activityId, String version) {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.create.request")
                .addField("accountId", UUIDs.timeBased())
                .addField("activityId", activityId)
                .addField("exportId", UUIDs.timeBased())
                .addField("publicationTitle", title)
                .addField("author", "Hibbeler")
                .addField("description", "test title")
                .addField("version", version)
                .build());
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.create.request.ok")
                .extractFromPayload("$.response.publicationId",  nameFrom(title, "id")));
    }

    @Then("{string} should fetch a list of publications of the activity {string} including")
    public void shouldFetchAListOfPublicationsOfTheActivityIncluding(String accountName, String activityId, List<String> publications) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "publication.history.fetch")
                .addField("activityId", activityId);
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "publications";
                    }

                    @Override
                    public String getType() {
                        return "publication.history.fetch.ok";
                    }

                    @Override
                    public void validate(ArrayList publicationResults, Map<String, Object> headers, TestContext context) {

                        Set<Map> expectedPublications = new HashSet<>();
                        for (String publication : publications) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("publicationTitle", publication);
                            expectedPublications.add(expected);
                        }

                        Set<Map> actualPublications = new HashSet<>();
                        for (Object publication : publicationResults) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("publicationTitle", (String) ((Map) publication).get("title"));
                            actualPublications.add(actual);
                        }

                        assertTrue(actualPublications.containsAll(expectedPublications));

                    }
                }));
    }

    @When("{string} has deleted publication {string} for activity {string} with version {string}")
    public void hasDeletedPublicationForActivity(String accountName, String title, String activityId, String version) {

        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.history.delete")
                .addField("publicationId",  Variables.interpolate(Variables.nameFrom(title, "id")))
                .addField("activityId",  activityId)
                .addField("version",  version)
                .build());
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.history.delete.ok"));

    }
}
