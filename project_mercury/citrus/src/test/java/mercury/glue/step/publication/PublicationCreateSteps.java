package mercury.glue.step.publication;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

public class PublicationCreateSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} requests to create a publication {string}")
    public void createPublication(String accountName, String publicationTitle) throws JsonProcessingException {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.create.request")
                .addField("accountId", UUIDs.timeBased())
                .addField("activityId", UUIDs.timeBased())
                .addField("exportId", UUIDs.timeBased())
                .addField("publicationTitle", publicationTitle)
                .addField("author", "Hibbeler")
                .addField("description", "test title")
                .addField("version", "1.0")
                .build());
    }

    @Then("^the publication is successfully created$")
    public void thePublicationIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.create.request.ok"));
    }

    @Given("{string} has created publication {string}")
    public void hasCreatePublication(String accountName, String publicationTitle) throws JsonProcessingException {
        createPublication(accountName, publicationTitle);
        thePublicationIsSuccessfullyCreated();
    }

    @When("{string} requests to create a publication {string} with Output Type {string}")
    public void createPublicationWithOutputType(String accountName, String publicationTitle, String outputType) throws JsonProcessingException {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.create.request")
                .addField("accountId", UUIDs.timeBased())
                .addField("activityId", UUIDs.timeBased())
                .addField("exportId", UUIDs.timeBased())
                .addField("publicationTitle", publicationTitle)
                .addField("author", "Hibbeler")
                .addField("description", "test title")
                .addField("version", "1.0")
                .addField("outputType", outputType)
                .build());
    }

    @When("{string} can update published course title {string} with {string} with activityId {string}")
    public void canUpdatePublishedCourseTitleWith(String accountName, String publicationTitle, String updatedPublicationTitle, String activityId) {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.title.update.request")
                .addField("activityId", UUIDs.timeBased())
                .addField("title", updatedPublicationTitle)
                .addField("version", "1.0")
                .build());
    }


    @Then("the publication title is successfully updated")
    public void thePublicationTitleIsSuccessfullyUpdated() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "publication.title.update.request.ok"));
    }

    @When("{string} requests to create a publication {string} with Output Type {string} with activityId {string}")
    public void createPublicationWithOutputType(String accountName, String publicationTitle, String outputType, String activityId) throws JsonProcessingException {
        if(accountName != null && accountName.equalsIgnoreCase("Alice")) {
            authenticationSteps.authenticateUser(accountName);
        }
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.create.request")
                .addField("accountId", UUIDs.timeBased())
                .addField("activityId", activityId)
                .addField("exportId", UUIDs.timeBased())
                .addField("publicationTitle", publicationTitle)
                .addField("author", "Hibbeler")
                .addField("description", "test title")
                .addField("version", "1.0")
                .addField("outputType", outputType)
                .build());
    }
}
