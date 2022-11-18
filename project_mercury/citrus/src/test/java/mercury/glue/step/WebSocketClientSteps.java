package mercury.glue.step;

import static mercury.glue.step.ProvisionSteps.DEFAULT_PASSWORD;
import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.glue.wiring.CitrusConfiguration.WEB_SOCKET_CLIENT_REGISTRY;
import static mercury.glue.wiring.CitrusConfiguration.WEB_SOCKET_REQUEST_URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;
import mercury.glue.wiring.WebSocketClientRegistry;

public class WebSocketClientSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(WEB_SOCKET_CLIENT_REGISTRY)
    private WebSocketClientRegistry webSocketClientRegistry;

    @Autowired
    @Qualifier(WEB_SOCKET_REQUEST_URL)
    private String requestUrl;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Given("^\"([^\"]*)\" is logged in to the default client$")
    public void isLoggedInToTheDefaultClient(String user) throws Throwable {
        messageOperations.sendJSON(runner, buildAuthenticationPayload(user).build());
        messageOperations.validateResponseType(runner, "authenticate.ok");
    }

    @And("^\"([^\"]*)\" is logged via a \"([^\"]*)\" client$")
    public void isLoggedViaAClient(String user, String client) throws Throwable {
        webSocketClientRegistry.add(runner, client, requestUrl);
        messageOperations.sendJSON(runner, buildAuthenticationPayload(user).build(), client);
        messageOperations.validateResponseType(runner, "authenticate.ok", client, action -> {});
    }

    @Then("^the default client returns \"([^\"]*)\" account details$")
    public void theDefaultClientReturnsAccountDetails(String user) throws Throwable {
        messageOperations.sendJSON(runner, new PayloadBuilder()
        .addField("type", "me.get").build());

        assertAccountDetailsMatch(user, WebSocketClientRegistry.DEFAULT_WEB_SOCKET_CLIENT);
    }

    private void assertAccountDetailsMatch(String user, String client) {
        messageOperations.receiveJSON(runner, action -> action.payload("{\"type\":\"me.get.ok\"," +
                "\"response\":{\"account\":{" +
                "\"accountId\":\"" + Variables.interpolate(getAccountIdVar(user)) + "\"," +
                "\"subscriptionId\":\"" + Variables.interpolate(getSubscriptionIdVar(user)) + "\"," +
                "\"iamRegion\":\"@notEmpty()@\"," +
                "\"primaryEmail\":\"" + Variables.interpolate(getAccountEmailVar(user)) + "\"," +
                "\"roles\":\"@notEmpty()@\"," +
                "\"email\":\"@notEmpty()@\"," +
                "\"authenticationType\":\"@notEmpty()@\"}}," +
                "\"replyTo\":\"@notEmpty()@\"}"), client);
    }

    @And("^the \"([^\"]*)\" client returns \"([^\"]*)\" account details$")
    public void theClientReturnsAccountDetails(String client, String user) throws Throwable {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "me.get").build(), client);

        assertAccountDetailsMatch(user, client);
    }

    private PayloadBuilder buildAuthenticationPayload(String user) {
        return new PayloadBuilder()
                .addField("type", "authenticate")
                .addField("email", Variables.interpolate(getAccountEmailVar(user)))
                .addField("password", "password");
    }

    private PayloadBuilder buildProvisionPayload() {
        return new PayloadBuilder()
                .addField("type", "iam.instructor.provision")
                .addField("email", "mercury:randomEmail()")
                .addField("password", DEFAULT_PASSWORD);
    }

    @Given("^an account \"([^\"]*)\" is created$")
    public void anAccountIsCreated(String user) throws Throwable {
        messageOperations.sendJSON(runner, buildProvisionPayload().build());
        messageOperations.receiveJSON(runner, action-> action
                .extractFromPayload("$.response.account.primaryEmail", getAccountEmailVar(user))
                .extractFromPayload("$.response.account.accountId", getAccountIdVar(user))
                .extractFromPayload("$.response.account.subscriptionId", getSubscriptionIdVar(user)));
    }
}
