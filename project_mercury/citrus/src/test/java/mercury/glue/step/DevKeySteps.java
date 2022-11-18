package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class DevKeySteps {

    static final String DEFAULT_DEV_KEY = "dev_key";

    private static final String RESPONSE = "{" +
            "\"type\":\"iam.developerKey.create.ok\", " +
            "\"response\":{" +
            "\"developerKey\":{" +
            "\"key\":\"@ignore@\", " +
            "\"subscriptionId\":\"@ignore@\"," +
            "\"accountId\":\"@ignore@\"," +
            "\"createdTs\":\"@ignore@\"}}," +
            "\"replyTo\":\"@notEmpty()@\"}";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    /**
     * this is hack to initialize @CitrusResource Runner, so this step class can be injected with @Autowired annotation
     * to other steps classes
     */
    @Before
    public void initializeCitrusResources() {
    }

    @Then("^mercury should send generated dev key$")
    public void itShouldSendGeneratedDevKey() {
        messageOperations.receiveJSON(runner, action -> action.payload(RESPONSE));
    }

    @Then("^mercury should store generated dev key as \"([^\"]*)\"$")
    public void mercuryShouldStoreGeneratedDevKeyAs(String variableName)  {
        messageOperations.receiveJSON(runner, action -> action.payload(RESPONSE)
                .extractFromPayload("$.response.developerKey.key", variableName));
    }

    @And("^a valid developer key is provided$")
    public void aValidDeveloperKeyIsProvided() {
        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", "iam.developerKey.create");

        messageOperations.sendJSON(runner, payloadBuilder.build());
        mercuryShouldStoreGeneratedDevKeyAs(DEFAULT_DEV_KEY);
    }
}
