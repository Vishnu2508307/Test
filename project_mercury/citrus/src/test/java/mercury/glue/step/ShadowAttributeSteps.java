package mercury.glue.step;

import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;

public class ShadowAttributeSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" has added \"([^\"]*)\" shadow and value as \"([^\"]*)\"$")
    public void addedShadowAttribute(String loggedAccount, String shadowAttribute, String value) {
        authenticationSteps.authenticateUser(loggedAccount);
        String message = new PayloadBuilder()
                .addField("type", "iam.shadow.attribute.add")
                .addField("accountId", Variables.interpolate(getAccountIdVar(loggedAccount)))
                .addField("value", value)
                .addField("accountShadowAttributeName", shadowAttribute).build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("shadow attribute is successfully created to \"([^\"]*)\"$")
    public void addedShadowAttributeSuccess(String loggedAccount) {
        messageOperations.receiveJSON(runner, action -> {
                action.payload("{" +
                               "\"type\":\"iam.shadow.attribute.add.ok\"," +
                               "\"replyTo\":\"@notEmpty()@\"}");});
    }

    @When("^\"([^\"]*)\" has added \"([^\"]*)\" shadow attribute source \"([^\"]*)\" and value as \"([^\"]*)\"$")
    public void addedShadowAttributeFailure(String loggedAccount, String shadowAttribute, String shadowSource, String value) {
        String message = new PayloadBuilder()
                .addField("type", "iam.shadow.attribute.add")
                .addField("accountId", Variables.interpolate(getAccountIdVar(loggedAccount)))
                .addField("value", value)
                .addField("accountShadowAttributeName", shadowAttribute)
                .addField("accountShadowAttributeSource", shadowSource).build();
        messageOperations.sendJSON(runner, message);
    }
    @Then("failed to add shadow attribute \"([^\"]*)\"$")
    public void addShadowAttributeFailureResult(String loggedAccount) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                   "\"type\":\"iam.shadow.attribute.add.error\"," +
                                   "\"code\":401," +
                                   "\"message\":\"@notEmpty()@\"," +
                                   "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("^\"([^\"]*)\" tries to delete shadow attribute as \"([^\"]*)\"$")
    public void removeShadowAttribute(String loggedAccount, String shadowAttribute) {
        authenticationSteps.authenticateUser(loggedAccount);
        String message = new PayloadBuilder()
                .addField("type", "iam.shadow.attribute.remove")
                .addField("accountId", Variables.interpolate(getAccountIdVar(loggedAccount)))
                .addField("accountShadowAttributeName", shadowAttribute).build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("shadow attribute is successfully removed to \"([^\"]*)\"$")
    public void removeShadowAttributeSuccess(String loggedAccount) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"iam.shadow.attribute.remove.ok\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("failed to remove shadow attribute for \"([^\"]*)\"$")
    public void removeShadowAttributeFailure(String loggedAccount) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"iam.shadow.attribute.remove.ok\"," +
                                       "\"code\":404," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
    @Then("failed to remove shadow attribute \"([^\"]*)\"$")
    public void removeShadowAttributeFailureResult(String loggedAccount) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"iam.shadow.attribute.remove.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
