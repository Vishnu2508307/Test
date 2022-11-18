package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class EvaluableSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} creates evaluable for {string} {string} with evaluation mode {string}")
    public void createsEvaluableForWithEvaluationMode(final String accountName,
                                                      final String elementName,
                                                      final String elementType,
                                                      final String evaluationMode) {
        authenticationSteps.authenticateUser(accountName);

        String payload = new PayloadBuilder()
                .addField("type", "author.evaluable.set")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("evaluationMode", evaluationMode)
                .build();

        messageOperations.sendJSON(runner, payload);
    }

    @Then("the evaluable has been created successfully")
    public void theEvaluableHasBeenCreatedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.evaluable.set.ok\"," +
                                       "\"response\":{" +
                                       "\"evaluable\":{" +
                                       "\"elementId\":\"@notEmpty()@\"," +
                                       "\"elementType\":\"@notEmpty()@\"," +
                                       "\"evaluationMode\":\"@notEmpty()@\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
        );

    }

    @Then("the evaluable is not created due to permission issue")
    public void theEvaluableIsNotCreatedDueToPermissionIssue() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.evaluable.set.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @And("{string} has created evaluable for {string} {string} with evaluation mode {string}")
    public void hasCreatedEvaluableForWithEvaluationMode(final String accountName,
                                                         final String elementName,
                                                         final String elementType,
                                                         final String evaluationMode) {
        createsEvaluableForWithEvaluationMode(accountName, elementName, elementType, evaluationMode);
        theEvaluableHasBeenCreatedSuccessfully();
    }

    @When("{string} tries to fetch evaluable for {string} {string}")
    public void triesToFetchEvaluableFor(final String accountName, final String elementName, final String elementType) {
        authenticationSteps.authenticateUser(accountName);

        String payload = new PayloadBuilder()
                .addField("type", "author.evaluable.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .build();

        messageOperations.sendJSON(runner, payload);
    }

    @Then("the evaluable fetch failed due to permission issue")
    public void theEvaluableFetchFailedDueToPermissionIssue() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.evaluable.get.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can fetch following data from {string} evaluable")
    public void canFetchFollowingDataFromEvaluable(final String user, final String elementName, final Map<String, String> expected) {
        authenticationSteps.authenticateUser(user);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.evaluable.get.ok\"," +
                                       "\"response\":{" +
                                       "\"evaluable\":{" +
                                       "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                                       "\"elementType\":\"" + expected.get("elementType") + "\"," +
                                       "\"evaluationMode\":\"" + expected.get("evaluationMode") + "\"" +
                                       "}" +
                                       "},\"replyTo\":\"@notEmpty()@\"}")
        );
    }

}
