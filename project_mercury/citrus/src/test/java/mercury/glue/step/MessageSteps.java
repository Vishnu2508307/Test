package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

/**
 * Provide message steps.
 */
public class MessageSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    private PayloadBuilder payloadBuilder = new PayloadBuilder();

    @Given("^A message type (.*)$")
    public void message(String messageType) {
        payloadBuilder.addField("type", messageType);
    }

    @When("^I post the message .*$")
    public void postMessage() {
        messageOperations.sendJSON(runner, payloadBuilder.build());
        clearPayload();
    }

    @Then("^mercury should respond with: \"([^\"]*)\"$")
    public void verifyReturn(final String type) {
        messageOperations.validateResponseType(runner, type);
    }

    @Then("^mercury should respond with: \"([^\"]*)\" and code \"([^\"]*)\"$")
    public void mercuryShouldRespondWithAndCode(String type, String code) {
        messageOperations.validateResponseType(runner, type, action -> action.jsonPath("$.code", code));
    }

    @Then("^mercury should respond with: \"([^\"]*)\" and code \"([^\"]*)\" and reason \"([^\"]*)\"$")
    public void mercuryShouldRespondWithAndCodeAndReason(String type, String code, String reason) {
        messageOperations.validateResponseType(runner, type, action -> action.jsonPath("$.code", code)
                .jsonPath("$.message", reason));
    }

    @And("^message contains \"([^\"]*)\" \"([^\"]*)\"$")
    public void messageContains(String key, String value) {
        payloadBuilder.addField(key, value);
    }

    @And("^message contains$")
    public void messageContains(Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            payloadBuilder.addField(entry.getKey(), entry.getValue());
        }
    }

    @Given("^a message with \"([^\"]*)\" for \"([^\"]*)\"$")
    public void aMessageWithFor(String variableName, String propertyName) {
        payloadBuilder.addField(propertyName, "${"+variableName+"}");
    }

    private void clearPayload() {
        payloadBuilder = new PayloadBuilder();
    }

    @Then("^mercury should respond with an error and id \"([^\"]*)\"$")
    public void mercuryShouldRespondWithAnErrorAndId(String id) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "\"type\":\"error\"," +
                "\"code\":500," +
                "\"message\":\"Unsupported message type unknown\"," +
                "\"replyTo\":\""+id+"\"}"));
    }
}
