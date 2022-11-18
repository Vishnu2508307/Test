package mercury.glue.step.publication;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.smartsparrow.util.UUIDs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class PublicationJobSubscribeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static final UUID PUBLICATION_ID = UUIDs.timeBased();

    @When("{string} subscribe to the publication {string} successfully")
    public void subscribeToThePublication(String accountName, String publication) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.job.subscribe")
                .addField("publicationId", PUBLICATION_ID)
                .build());
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("publication.job.subscribe.ok")));
    }

    @Given("{string} cannot subscribe to the missing publication {string}")
    public void cannotSubscribeToMissingPublication(String accountName,
                                                                                   String publication) {

        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.job.subscribe")
                .addField("publicationId", null)
                .build());
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("publication.job.subscribe.error")));
    }

    @When("{string} unsubscribe to the publication {string} successfully")
    public void unsubscribeToThePublication(String accountName, String publication) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.job.unsubscribe")
                .addField("publicationId", PUBLICATION_ID)
                .build());
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("publication.job.unsubscribe.ok")));
    }

    @Given("{string} cannot unsubscribe to the missing publication {string}")
    public void cannotUnsubscribeToMissingPublication(String accountName,
                                                    String publication) {

        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "publication.job.unsubscribe")
                .addField("publicationId", null)
                .build());
        messageOperations.receiveJSON(runner,
                                      action -> action.jsonPath("$.type",
                                                                Matchers.containsString("publication.job.unsubscribe.error")));
    }
}
