package mercury.glue.step.publication;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class PublicationStatusSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} requests to fetch the publication status for {string}")
    public void createPublication(String accountName, String bookId) throws JsonProcessingException {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.oculus.status")
                .addField("bookId", bookId)
                .build());
    }

    @Then("^the publication status has been fetched successfully$")
    public void thePublicationIsSuccessfullyFetched() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.oculus.status.ok"));
    }
}
