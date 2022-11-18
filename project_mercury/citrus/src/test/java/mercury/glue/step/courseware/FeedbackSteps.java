package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.courseware.ActivityDuplicateSteps.replaceByIds;
import static mercury.glue.step.courseware.FeedbackHelper.CREATE_ERROR_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.CREATE_MESSAGE;
import static mercury.glue.step.courseware.FeedbackHelper.CREATE_OK_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.DELETE_ERROR_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.DELETE_MESSAGE;
import static mercury.glue.step.courseware.FeedbackHelper.DELETE_OK_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.GET_ERROR_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.GET_MESSAGE;
import static mercury.glue.step.courseware.FeedbackHelper.GET_OK_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.REPLACE_ERROR_RESPONSE;
import static mercury.glue.step.courseware.FeedbackHelper.REPLACE_MESSAGE;
import static mercury.glue.step.courseware.FeedbackHelper.REPLACE_OK_RESPONSE;
import static mercury.glue.step.courseware.InteractiveSteps.INTERACTIVE_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class FeedbackSteps {

    public static final String FEEDBACK_ID_VAR = "feedback_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates a feedback for random interactive$")
    public void createsAFeedbackForRandomInteractive(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                String.format(CREATE_MESSAGE, "mercury:randomTimeUUID()", "${" + PLUGIN_ID_VAR + "}"));
    }

    @When("^\"([^\"]*)\" creates a feedback for the interactive$")
    public void createsAFeedbackForTheInteractive(String accountName) {
        createsAFeedbackForTheInteractive(accountName, null);
    }

    public void createsAFeedbackForTheInteractive(String accountName, String interactiveName) {
        authenticationSteps.authenticateUser(accountName);
        String interactiveVar = interactiveName == null ? INTERACTIVE_ID_VAR : nameFrom(interactiveName, "id");
        messageOperations.sendJSON(runner, String.format(CREATE_MESSAGE, interpolate(interactiveVar), "${" + PLUGIN_ID_VAR + "}"));

    }

    @Then("^feedback is successfully created$")
    public void feedbackIsSuccessfullyCreated() {
        feedbackIsSuccessfullyCreated(null, null);
    }

    public void feedbackIsSuccessfullyCreated(String feedbackName, String interactiveName) {
        String feedbackVar = feedbackName == null ? FEEDBACK_ID_VAR : nameFrom(feedbackName, "id");
        String interactiveVar = interactiveName == null ? INTERACTIVE_ID_VAR : nameFrom(interactiveName, "id");
        messageOperations.receiveJSON(runner,
                action -> action.payload(String.format(CREATE_OK_RESPONSE, "@variable('" + feedbackVar + "')@", interpolate(interactiveVar))));
    }

    @When("^\"([^\"]*)\" has created a feedback for the interactive$")
    public void hasCreatedAFeedbackForTheInteractive(String accountName) {
        createsAFeedbackForTheInteractive(accountName, null);
        feedbackIsSuccessfullyCreated(null, null);
    }

    @Then("^feedback creation fails with message \"([^\"]*)\" and code (\\d+)$")
    public void feedbackCreationFailsWithMessage(String errorMessage, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                String.format(CREATE_ERROR_RESPONSE, code, "@startsWith('" + errorMessage + "')@")));
    }

    @When("^\"([^\"]*)\" has saved configuration for the feedback$")
    public void hasSavedConfigurationForTheFeedback(String accountName, String config) {
        savesConfigurationForTheFeedback(accountName, null, config);
        feedbackConfigurationIsSuccessfullySaved(null, config);
    }

    @When("{string} saves {string} asset configuration for the {string} feedback")
    public void hasSavedAssetConfigurationForTheFeedback(String accountName, String assetName, String feedbackName, String config) {
        String assetUrn = interpolate(nameFrom(assetName, "urn"));

        if(config.contains("AssetURN")){
            config = config.replace("AssetURN", assetUrn);
        }

        savesConfigurationForTheFeedback(accountName, feedbackName, config);
    }

    @Then("the {string} feedback configuration is successfully saved")
    public void saveConfigForFeedbackSuccessfully(String feedbackName) {
        String expectedFeedback = "{" +
                "  \"type\": \"author.interactive.feedback.replace.ok\"," +
                "  \"response\": {" +
                "    \"feedbackConfig\": {" +
                "      \"id\": \"@notEmpty()@\"," +
                "      \"feedbackId\": \"${" + nameFrom(feedbackName, "id") + "}\"," +
                "      \"config\": \"@notEmpty()@\"" +
                "    }" +
                "  }" +
                "}";

        messageOperations.receiveJSON(runner, action -> action.payload(expectedFeedback));
    }

    public void savesConfigurationForTheFeedback(String accountName, String feedbackName, String config) {
        authenticationSteps.authenticateUser(accountName);
        String feedbackVar = feedbackName == null ? FEEDBACK_ID_VAR : nameFrom(feedbackName, "id");
        messageOperations.sendJSON(runner, String.format(REPLACE_MESSAGE, feedbackVar, StringEscapeUtils.escapeJava(config)));
    }

    public void feedbackConfigurationIsSuccessfullySaved(String feedbackName, String config) {
        String feedbackVar = feedbackName == null ? FEEDBACK_ID_VAR : nameFrom(feedbackName, "id");
        messageOperations.receiveJSON(runner, action ->
                action.payload(String.format(REPLACE_OK_RESPONSE, feedbackVar, StringEscapeUtils.escapeJava(config))));
    }

    @Then("^the feedback can be successfully fetched$")
    public void theFeedbackCanBeSuccessfullyFetched() {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, FEEDBACK_ID_VAR));
        String expectedFeedback = String.format(GET_OK_RESPONSE.apply(null),
                "@variable('" + FEEDBACK_ID_VAR + "')@", interpolate(INTERACTIVE_ID_VAR));
        messageOperations.receiveJSON(runner, action -> action.payload(expectedFeedback));
    }

    @Then("^the feedback can be successfully fetched with config$")
    public void theFeedbackCanBeSuccessfullyFetchedWithConfig(String config) {
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, FEEDBACK_ID_VAR));
        String expectedFeedback = String.format(GET_OK_RESPONSE.apply(config),
                "@variable('" + FEEDBACK_ID_VAR + "')@", interpolate(INTERACTIVE_ID_VAR), StringEscapeUtils.escapeJava(config));
        messageOperations.receiveJSON(runner, action -> action.payload(expectedFeedback));
    }

    @When("^\"([^\"]*)\" has deleted a feedback$")
    public void hasDeletedAFeedback(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, String.format(DELETE_MESSAGE, FEEDBACK_ID_VAR, INTERACTIVE_ID_VAR));
        messageOperations.receiveJSON(runner, action -> action.payload(String.format(DELETE_OK_RESPONSE, FEEDBACK_ID_VAR, INTERACTIVE_ID_VAR)));
    }

    @And("^the feedback can not be successfully fetched$")
    public void theFeedbackCanNotBeSuccessfullyFetched() {
        messageOperations.sendJSON(runner, GET_MESSAGE);
        messageOperations.receiveJSON(runner, action -> action.payload(GET_ERROR_RESPONSE));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" feedback for the \"([^\"]*)\" interactive$")
    public void hasCreatedAFeedbackForTheInteractive(String accountName, String feedbackName, String interactiveName) {
        createsAFeedbackForTheInteractive(accountName, interactiveName);
        feedbackIsSuccessfullyCreated(feedbackName, interactiveName);
    }

    @Then("^\"([^\"]*)\" can not create a feedback for \"([^\"]*)\" interactive due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotCreateAFeedbackForInteractiveDueToErrorCodeMessage(String accountName, String interactiveName,
                                                                         int code, String message) {
        createsAFeedbackForTheInteractive(accountName, interactiveName);
        feedbackCreationFailsWithMessage(message, code);
    }

    @Then("^\"([^\"]*)\" can not delete \"([^\"]*)\" feedback for \"([^\"]*)\" interactive due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDeleteFeedbackDueToErrorCodeMessage(String accountName, String feedbackName, String interactiveName,
                                                          int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                String.format(DELETE_MESSAGE, nameFrom(feedbackName, "id"), nameFrom(interactiveName, "id")));
        messageOperations.receiveJSON(runner, action -> action.payload(
                String.format(DELETE_ERROR_RESPONSE, code, message)));
    }

    @Then("^\"([^\"]*)\" can not fetch \"([^\"]*)\" feedback due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchFeedbackDueToErrorCodeMessage(String accountName, String feedbackName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        String feedbackVar = feedbackName == null ? FEEDBACK_ID_VAR : nameFrom(feedbackName, "id");
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, feedbackVar));
        messageOperations.receiveJSON(runner, action -> action.payload(String.format(GET_ERROR_RESPONSE, code, message)));
    }

    @Then("^\"([^\"]*)\" can not fetch feedback due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchFeedbackDueToErrorCodeMessage(String accountName, int code, String message) {
        canNotFetchFeedbackDueToErrorCodeMessage(accountName, null, code, message);
    }

    @Then("^\"([^\"]*)\" can not save config for \"([^\"]*)\" feedback due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSaveConfigForFeedbackDueToErrorCodeMessage(String accountName, String feedbackName, int code, String message) {
        savesConfigurationForTheFeedback(accountName, feedbackName, "any config");
        messageOperations.receiveJSON(runner, action -> action.payload(String.format(REPLACE_ERROR_RESPONSE, code, message)));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" feedback successfully$")
    public void canFetchFeedbackSuccessfully(String accountName, String feedbackName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
        String expectedFeedback = String.format(GET_OK_RESPONSE.apply(null),
                interpolate(nameFrom(feedbackName, "id")), "@notEmpty()@");
        messageOperations.receiveJSON(runner, action -> action.payload(expectedFeedback));
    }

    @Then("^\"([^\"]*)\" can create a feedback for \"([^\"]*)\" interactive successfully$")
    public void canCreateAFeedbackForInteractiveSuccessfully(String accountName, String interactiveName) {
        createsAFeedbackForTheInteractive(accountName, interactiveName);
        feedbackIsSuccessfullyCreated(null, interactiveName);
    }

    @Then("^\"([^\"]*)\" can delete \"([^\"]*)\" feedback for \"([^\"]*)\" interactive successfully$")
    public void canDeleteFeedbackSuccessfully(String accountName, String feedbackName, String interactiveName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner,
                String.format(DELETE_MESSAGE, nameFrom(feedbackName, "id"), nameFrom(interactiveName, "id")));
        messageOperations.receiveJSON(runner, action -> action.payload(
                String.format(DELETE_OK_RESPONSE, nameFrom(feedbackName, "id"), nameFrom(interactiveName, "id"))));
    }

    @Then("^\"([^\"]*)\" can save config for \"([^\"]*)\" feedback successfully$")
    public void canSaveConfigForFeedbackSuccessfully(String accountName, String feedbackName) {
        savesConfigurationForTheFeedback(accountName, feedbackName, "any config");
        feedbackConfigurationIsSuccessfullySaved(feedbackName, "any config");
    }

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" feedback$")
    public void shouldReceiveAnActionMessageForTheFeedback(String clientName, String coursewareAction, String feedbackName) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "    \"type\": \"author.activity.broadcast\"," +
                "    \"response\": {" +
                "      \"elementId\" : \"" + interpolate(nameFrom(feedbackName, "id")) + "\"," +
                "      \"elementType\" : \"FEEDBACK\"," +
                "      \"action\" : \"" + coursewareAction.toUpperCase() + "\"," +
                "      \"rtmEvent\" : \"" + "FEEDBACK_" + coursewareAction.toUpperCase() + "\"" +
                "    }," +
                "    \"replyTo\": \"@notEmpty()@\"" +
                "}"), clientName);

    }

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" feedback with \"([^\"]*)\"$")
    public void shouldReceiveAnActionMessageForTheFeedbackWith(String clientName, String coursewareAction,
                                                               String feedbackName, String additionalParameter) {
        String fieldName = additionalParameter.split("=")[0];
        String fieldValue = additionalParameter.split("=")[1];
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "    \"type\": \"author.activity.broadcast\"," +
                "    \"response\": {" +
                "      \"elementId\" : \"" + interpolate(nameFrom(feedbackName, "id")) + "\"," +
                "      \"elementType\" : \"FEEDBACK\"," +
                "      \"action\" : \"" + coursewareAction.toUpperCase() + "\"," +
                "      \"rtmEvent\": \"FEEDBACK_" + coursewareAction.toUpperCase() + "\"," +
                "      \"" + fieldName + "\" : \"" + fieldValue + "\"" +
                "    }," +
                "    \"replyTo\": \"@notEmpty()@\"" +
                "}"), clientName);

    }

    @When("^\"([^\"]*)\" has deleted the \"([^\"]*)\" feedback for the \"([^\"]*)\" interactive$")
    public void hasDeletedTheFeedbackForTheInteractive(String accountName, String feedbackName, String interactiveName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, String.format(DELETE_MESSAGE, nameFrom(feedbackName, "id"),
                nameFrom(interactiveName, "id")));
        messageOperations.receiveJSON(runner, action -> action.payload(String.format(DELETE_OK_RESPONSE,
                nameFrom(feedbackName, "id"), nameFrom(interactiveName, "id"))));
    }

    @When("^\"([^\"]*)\" has updated the \"([^\"]*)\" feedback config with \"([^\"]*)\"$")
    public void hasUpdatedTheFeedbackConfigWith(String accountName, String feedbackName, String config) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, String.format(REPLACE_MESSAGE, nameFrom(feedbackName, "id"),
                StringEscapeUtils.escapeJava(config)));

        messageOperations.receiveJSON(runner, action ->
                action.payload(String.format(REPLACE_OK_RESPONSE, nameFrom(feedbackName, "id"),
                        StringEscapeUtils.escapeJava(config))));
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" feedback with references to \"([^\"]*)\"$")
    public void hasSavedConfigForFeedbackWithReferencesTo(String user, String feedbackName, String list) {
        String config = replaceByIds(runner, list);
        hasUpdatedTheFeedbackConfigWith(user, feedbackName, config);
    }

    @When("^\"([^\"]*)\" fetches the \"([^\"]*)\" feedback")
    public void fetchesTheFeedback(String user, String feedbackName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, String.format(GET_MESSAGE, nameFrom(feedbackName, "id")));
    }
}
