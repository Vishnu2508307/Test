package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.EventSteps.ACTIVITY_BROADCAST_ID;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.step.courseware.ScenarioSteps.SCENARIO_ID;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;

public class ActivityEventSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Then("^the activity config changes should be notified to \"([^\"]*)\"$")
    public void theActivityChangesShouldBeNotifiedTo(String client) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{"
                                + "\"type\":\"author.activity.broadcast\","
                                + "\"replyTo\":\"" + Variables.interpolate(client + "_" + ACTIVITY_BROADCAST_ID) + "\","
                                + "\"response\":{"
                                    + "\"activityId\":\"" + interpolate(ACTIVITY_ID_VAR)+"\","
                                    + "\"action\":\"CONFIG_CHANGE\","
                                    + "\"rtmEvent\":\"ACTIVITY_CONFIG_CHANGE\","
                                    + "\"config\":\"{\\\"title\\\":\\\"Demo Course\\\", \\\"desc\\\":\\\"Some description\\\"}\"}}"),
                client);
    }

    @Then("^the activity scenario \"([^\"]*)\" creation should be notified to \"([^\"]*)\"$")
    public void theActivityScenarioCreationShouldBeNotifiedTo(String scenarioKey, String client) {
        messageOperations.receiveJSON(runner, action ->
                        action.payload("{"
                                    + "\"type\":\"author.activity.broadcast\","
                                    + "\"replyTo\":\"@notEmpty()@\","
                                    + "\"response\":{"
                                        + "\"elementId\":\"" + interpolate(SCENARIO_ID + scenarioKey) +"\","
                                        + "\"parentElementType\":\"@notEmpty()@\","
                                        + "\"lifecycle\":\"@notEmpty()@\","
                                        + "\"parentElementId\":\"@notEmpty()@\","
                                        + "\"action\":\"CREATED\","
                                        + "\"rtmEvent\":\"SCENARIO_CREATED\","
                                        + "\"elementType\":\"SCENARIO\""
                                    + "}"
                                + "}"),
                client);
    }

    @When("^\"([^\"]*)\" unsubscribes to activity events via a \"([^\"]*)\" client$")
    public void unsubscribesToActivityEventsViaAClient(String user, String client) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.unsubscribe")
                .addField("activityId", interpolate(ACTIVITY_ID_VAR))
                .build(), client);
        messageOperations.validateResponseType(runner, "author.activity.unsubscribe.ok", client, action -> {});
    }


    @When("^\"([^\"]*)\" unsubscribes to activity events for \"([^\"]*)\" via a \"([^\"]*)\" client$")
    public void unsubscribesToActivityEventsViaAClient(String user, String elementName ,String client) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.unsubscribe")
                .addField("activityId", interpolate(nameFrom(elementName, "id")))
                .build(), client);
        messageOperations.validateResponseType(runner, "author.activity.unsubscribe.ok", client, action -> {});
    }

    @And("^the activity theme changes are notified to \"([^\"]*)\"$")
    public void theActivityThemeChangesAreNotifiedTo(String client) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"author.activity.broadcast\"," +
                                "\"replyTo\":\"" + Variables.interpolate(client + "_" + ACTIVITY_BROADCAST_ID) + "\"," +
                                "\"response\":{" +
                                    "\"action\":\"THEME_CHANGE\"," +
                                    "\"theme\":\"{color=blue}\"," +
                                    "\"rtmEvent\":\"ACTIVITY_THEME_CHANGE\"," +
                                    "\"activityId\":\"" + interpolate(ACTIVITY_ID_VAR) + "\"}}"), client);
    }

    @Then("^\"([^\"]*)\" can not subscribe to \"([^\"]*)\" activity events due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSubscribeToActivityEventsDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        subscribesToActivity(activityName);
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "  \"type\": \"author.activity.subscribe.error\"," +
                "  \"code\": " + code + "," +
                "  \"message\": \"" + message + "\"" +
                "}"));
    }

    @Then("^\"([^\"]*)\" can not unsubscribe from \"([^\"]*)\" activity events due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotUnsubscribeFromActivityEventsDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        unsubscribesFromActivity(activityName);
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "  \"type\": \"author.activity.unsubscribe.error\"," +
                "  \"code\": " + code + "," +
                "  \"message\": \"" + message + "\"" +
                "}"));
    }

    @Then("^\"([^\"]*)\" can subscribe to \"([^\"]*)\" activity events successfully$")
    public void canSubscribeToActivityEventsSuccessfully(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        subscribesToActivity(activityName);
        messageOperations.validateResponseType(runner, "author.activity.subscribe.ok");
    }

    @Then("^\"([^\"]*)\" can unsubscribe from \"([^\"]*)\" activity events successfully$")
    public void canUnsubscribeFromActivityEventsSuccessfully(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        unsubscribesFromActivity(activityName);
        messageOperations.validateResponseType(runner, "author.activity.unsubscribe.ok");
    }

    private void subscribesToActivity(String activityName) {
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.subscribe\"," +
                "    \"activityId\" : \"" + interpolate(nameFrom(activityName, "id")) + "\"" +
                "}");
    }

    private void unsubscribesFromActivity(String activityName) {
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.unsubscribe\"," +
                "    \"activityId\" : \"" + interpolate(nameFrom(activityName, "id")) + "\"" +
                "}");
    }

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" activity$")
    public void shouldReceiveAMessageForTheActivity(String clientName, String activityAction, String activityName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.elementId", interpolate(nameFrom(activityName, "id")))
                        .jsonPath("$.response.action", activityAction)
                        .jsonPath("$.response.rtmEvent", "ACTIVITY_" + activityAction)
                        .jsonPath("$.response.elementType", "ACTIVITY"), clientName);
    }

    @And("{string} should receive an action {string} message for the {string} activity and {string} parent pathway")
    public void shouldReceiveAnActionMessageForTheActivityAndParentPathway(final String clientName,
                                                                           final String activityAction,
                                                                           final String activityName,
                                                                           final String parentPathwayName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.elementId", interpolate(nameFrom(activityName, "id")))
                .jsonPath("$.response.action", activityAction)
                .jsonPath("$.response.rtmEvent", "ACTIVITY_" + activityAction)
                .jsonPath("$.response.parentPathwayId", interpolate(nameFrom(parentPathwayName, "id")))
                .jsonPath("$.response.elementType", "ACTIVITY"), clientName);
    }
}
