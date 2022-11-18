package mercury.glue.step.publication;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.core.JsonProcessingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PublicationListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^user requests a list of publication$")
    public void userRequestsAListOfPublication() throws JsonProcessingException {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.list.request");
        messageOperations.sendJSON(runner, payload.build());
    }

    @Then("^\"([^\"]*)\" should fetch a list of publications including$")
    public void shouldFetchListOfPublications(String accountName, List<String> publications) {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "publication.list.request");
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "publications";
                    }

                    @Override
                    public String getType() {
                        return "publication.list.request.ok";
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
}
