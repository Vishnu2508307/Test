package mercury.glue.step.courseware;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.courseware.ActivityDuplicateSteps.replaceByIds;
import static mercury.glue.step.courseware.ComponentSteps.COMPONENT_ID_VAR;
import static mercury.glue.step.courseware.FeedbackSteps.FEEDBACK_ID_VAR;
import static mercury.glue.step.courseware.PathwaySteps.PATHWAY_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.InteractiveHelper.getInteractiveErrorResponse;
import static mercury.helpers.courseware.InteractiveHelper.validateInteractiveGetResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.courseware.InteractiveHelper;

public class InteractiveSteps {

    public static final String INTERACTIVE_ID_VAR = "interactive_id";
    private UUID INTERACTIVE_ID_PREGENERATED;

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates an interactive for this pathway(?: with config '(.*)')?$")
    public void createsAnInteractiveForPathway(String accountName, String config) {
        createsAnInteractiveForPathway(accountName, null, config, null);
    }

    private void createsAnInteractiveForPathway(String accountName, String pathwayName, String config, String interactiveId) {
        authenticationSteps.authenticateUser(accountName);
        String pathwayVar = pathwayName == null ? PATHWAY_ID_VAR : nameFrom(pathwayName, "id");
        messageOperations.sendJSON(runner, "{" +
                "  \"type\": \"author.interactive.create\"," +
                "  \"pathwayId\": \"${" + pathwayVar + "}\"," +
                "  \"pluginId\": \"${" + PLUGIN_ID_VAR + "}\"," +
                "  \"pluginVersion\": \"1.*\"" +
                (config != null ? ",\"config\":\"" + StringEscapeUtils.escapeJava(config) + "\"" : "") +
                (interactiveId != null ? ",\"interactiveId\": \"" + interactiveId + "\"" : "") +
                "}");
    }

    @When("^\"([^\"]*)\" creates an interactive for this pathway with provided id$")
    public void createsAnInteractiveForPathwayWithProvidedId(String accountName) {
        INTERACTIVE_ID_PREGENERATED = UUIDs.timeBased();
        createsAnInteractiveForPathway(accountName, null, null, INTERACTIVE_ID_PREGENERATED.toString());
    }

    @When("^\"([^\"]*)\" creates an interactive for this pathway with the same id$")
    public void createsAnInteractiveForPathwayWithSameId(String accountName) {
        createsAnInteractiveForPathway(accountName, null, null, INTERACTIVE_ID_PREGENERATED.toString());
    }

    @When("^\"([^\"]*)\" creates an interactive for random pathway$")
    public void createsAnInteractiveForRandomPathway(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, "{" +
                "  \"type\": \"author.interactive.create\"," +
                "  \"pathwayId\": \"" + UUIDs.timeBased() + "\"," +
                "  \"pluginId\": \"${" + PLUGIN_ID_VAR + "}\"," +
                "  \"pluginVersion\": \"1.*\"" +
                "}");
    }

    @Then("^the interactive is successfully created(?: with config '(.*)')?$")
    public void theInteractiveIsSuccessfullyCreated(String config) {
        String expectedInteractive = "{" +
                "  \"type\": \"author.interactive.create.ok\"," +
                "  \"response\": {" +
                "      \"interactive\": {" +
                "          \"interactiveId\": \"@variable('" + INTERACTIVE_ID_VAR + "')@\"," +
                "          \"plugin\": {" +
                "              \"pluginId\": \"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "              \"name\": \"Course Citrus plugin\"," +
                "              \"type\": \"course\"," +
                "              \"version\": \"1.*\"," +
                "               \"pluginFilters\":[{" +
                "                     \"pluginId\":\"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "                      \"version\":\"1.2.0\"," +
                "                       \"filterType\":\"ID\"," +
                "                        \"filterValues\":\"@notEmpty()@\"" +
                "                 }]" +
                "          }," +
                "          \"parentPathwayId\": \"" + interpolate(PATHWAY_ID_VAR) + "\"," +
                "          \"studentScopeURN\": \"@notEmpty()@\"" +
                (config != null ? ",\"config\":\"" + StringEscapeUtils.escapeJava(config) + "\"" : "") +
                "      }" +
                "  }" +
                "}";
        messageOperations.receiveJSON(runner, action -> action.payload(expectedInteractive));
    }

    @Then("^the interactive is successfully created with provided id?$")
    public void theInteractiveIsSuccessfullyCreatedWithProvidedId() {
        String expectedInteractive = "{" +
                "  \"type\": \"author.interactive.create.ok\"," +
                "  \"response\": {" +
                "      \"interactive\": {" +
                "          \"interactiveId\": \"@variable('" + INTERACTIVE_ID_PREGENERATED + "')@\"," +
                "          \"plugin\": {" +
                "              \"pluginId\": \"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "              \"name\": \"Course Citrus plugin\"," +
                "              \"type\": \"course\"," +
                "              \"version\": \"1.*\"," +
                "               \"pluginFilters\":[{" +
                "                     \"pluginId\":\"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "                      \"version\":\"1.2.0\"," +
                "                       \"filterType\":\"ID\"," +
                "                        \"filterValues\":\"@notEmpty()@\"" +
                "                 }]" +
                "          }," +
                "          \"parentPathwayId\": \"" + interpolate(PATHWAY_ID_VAR) + "\"," +
                "          \"studentScopeURN\": \"@notEmpty()@\"" +
                "      }" +
                "  }" +
                "}";
        messageOperations.receiveJSON(runner, action -> action.payload(expectedInteractive));
    }

    @Then("the interactive is not created due to conflict")
    public void theProjectActivityIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("author.interactive.create.error", payload.getType());
                assertEquals(409, payload.getCode().intValue());
            }
        }));
    }

    @Then("^interactive creation fails with message \"([^\"]*)\" and code (\\d+)$")
    public void interactiveCreationFailsWithMessageAndCode(String errorMessage, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "    \"type\": \"author.interactive.create.error\"," +
                "    \"code\": " + code + "," +
                "    \"message\": \"" + errorMessage + "\"" +
                "}"));
    }

    @When("^\"([^\"]*)\" saves configuration for the interactive$")
    public void savesConfigurationForTheInteractive(String accountName, String config) {
        savesConfigurationForTheInteractive(accountName, null, config);
    }

    @When("{string} saves {string} asset configuration for the {string} interactive")
    public void savesConfigurationForTheInteractive(String accountName, String assetName, String interactiveName, String config) {
        String assetUrn = interpolate(nameFrom(assetName, "urn"));

        if(config.contains("AssetURN")){
            config = config.replace("AssetURN", assetUrn);
        }
        savesConfigurationForTheInteractive(accountName, interactiveName, config);
    }

    public void savesConfigurationForTheInteractive(String accountName, String interactiveName, String config) {
        authenticationSteps.authenticateUser(accountName);
        runner.variable("interactive_config", config);
        String interactiveVar = interactiveName == null ? INTERACTIVE_ID_VAR : nameFrom(interactiveName, "id");
        messageOperations.sendJSON(runner, "{" +
                "  \"type\": \"author.interactive.config.replace\"," +
                "  \"interactiveId\": \"${" + interactiveVar + "}\"," +
                "  \"config\": \"" + StringEscapeUtils.escapeJava(config) + "\"" +
                "}");
    }

    @When("^\"([^\"]*)\" saves configuration for the \"([^\"]*)\" interactive$")
    public void savesConfigurationForTheNamedInteractive(String accountName, String interactiveName, String config) {
        authenticationSteps.authenticateUser(accountName);
        runner.variable("interactive_config", config);
        messageOperations.sendJSON(runner, "{" +
                "  \"type\": \"author.interactive.config.replace\"," +
                "  \"interactiveId\": \"${" + nameFrom(interactiveName, "id") + "}\"," +
                "  \"config\": \"" + StringEscapeUtils.escapeJava(config) + "\"" +
                "}");
    }

    @Then("^the interactive configuration is successfully saved$")
    public void theInteractiveConfigurationIsSuccessfullySaved() {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "  \"type\": \"author.interactive.config.replace.ok\"," +
                "  \"response\": {" +
                "    \"config\": {" +
                "      \"id\": \"@notEmpty()@\"," +
                "      \"interactiveId\": \"${" + INTERACTIVE_ID_VAR + "}\"," +
                "      \"config\" : \"mercury:escapeJson('${interactive_config}')\"" +
                "    }" +
                "  }" +
                "}"));
    }

    @Then("^the \"([^\"]*)\" interactive configuration is successfully saved$")
    public void theNamedInteractiveConfigurationIsSuccessfullySaved(String interactiveName) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "  \"type\": \"author.interactive.config.replace.ok\"," +
                "  \"response\": {" +
                "    \"config\": {" +
                "      \"id\": \"@notEmpty()@\"," +
                "      \"interactiveId\": \"${" + nameFrom(interactiveName, "id") + "}\"," +
                "      \"config\" : \"mercury:escapeJson('${interactive_config}')\"" +
                "    }" +
                "  }" +
                "}"));
    }

    @And("^\"([^\"]*)\" has created an interactive$")
    public void hasCreatedAnInteractive(String accountName) {
        createsAnInteractiveForPathway(accountName, null, null, null);
        String expectedInteractive = "{" +
                "  \"type\": \"author.interactive.create.ok\"," +
                "  \"response\": {" +
                "      \"interactive\": {" +
                "      \"interactiveId\": \"@variable('" + INTERACTIVE_ID_VAR + "')@\"," +
                "          \"plugin\": {" +
                "              \"pluginId\": \"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "              \"name\": \"Course Citrus plugin\"," +
                "              \"type\": \"course\"," +
                "              \"version\": \"1.*\"," +
                "               \"pluginFilters\":[{" +
                "                     \"pluginId\":\"" + interpolate(PLUGIN_ID_VAR) + "\"," +
                "                      \"version\":\"1.2.0\"," +
                "                       \"filterType\":\"ID\"," +
                "                        \"filterValues\":\"@notEmpty()@\"" +
                "                 }]" +
                "          }," +
                "          \"parentPathwayId\": \"" + interpolate(PATHWAY_ID_VAR) + "\"," +
                "          \"studentScopeURN\": \"@notEmpty()@\"}" +
                "      }" +
                "}";

        messageOperations.receiveJSON(runner, action -> action.payload(expectedInteractive));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" interactive for the \"([^\"]*)\" pathway$")
    public void hasCreatedAInteractiveForThePathway(String accountName,
                                                    String interactiveName, String pathwayName) {
        createsAnInteractiveForPathway(accountName, pathwayName, null, null);
        messageOperations.receiveJSON(runner,
                action -> action
                        .validate("$.type", "author.interactive.create.ok")
                        .jsonPath("$.response.interactive.interactiveId", "@variable('" + nameFrom(interactiveName, "id") + "')@")
                        .jsonPath("$.response.interactive.studentScopeURN", "@variable('" + nameFrom(interactiveName, "studentScope") + "')@"));
    }

    @When("^\"([^\"]*)\" deletes the \"([^\"]*)\" interactive for the \"([^\"]*)\" pathway$")
    public void deletesTheInteractiveForThePathway(String accountName, String interactiveName, String pathwayName) {
        String payload = new PayloadBuilder()
                .addField("type", "author.interactive.delete")
                .addField("interactiveId", interpolate(nameFrom(interactiveName, "id")))
                .addField("parentPathwayId", interpolate(nameFrom(pathwayName, "id")))
                .build();
        messageOperations.sendJSON(runner, payload);
    }

    @Then("^the \"([^\"]*)\" interactive is deleted$")
    public void theInteractiveIsDeleted(String interactiveName) {
        String expected = "{" +
                "     \"type\":\"author.interactive.delete.ok\"," +
                "     \"response\":{" +
                "         \"interactiveId\":\"" + interpolate(nameFrom(interactiveName, "id")) + "\"," +
                "         \"parentPathwayId\":\"@notEmpty()@\"" +
                "     },\"replyTo\":\"@notEmpty()@\"" +
                "}";
        messageOperations.receiveJSON(runner, action -> action.payload(expected));
    }

    @Then("^the \"([^\"]*)\" delete fails due to code (\\d+) and message \"([^\"]*)\"$")
    public void theDeleteFailsDueToCodeAndMessage(String interactiveName, int code, String message) {
        String expected = "{"
                + "\"type\":\"author.interactive.delete.error\","
                + "\"code\":" + code + ","
                + "\"message\":\"" + message + "\","
                + "\"replyTo\":\"@notEmpty()@\""
                + "}";
        messageOperations.receiveJSON(runner, action -> action.payload(expected));
    }

    @Then("^the interactive does not have this feedback as a child$")
    public void theInteractiveDoesNotHaveThisFeedbackAsAChild() {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(INTERACTIVE_ID_VAR))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<UUID> feedBacks = payload.getFeedbacks();
                    UUID feedbackId = UUID.fromString(context.getVariable(FEEDBACK_ID_VAR));

                    if (feedBacks != null) {
                        assertTrue(feedBacks.stream().noneMatch(id -> id.equals(feedbackId)));
                    }
                })));
    }

    @Then("^the interactive has this feedback as a child$")
    public void theInteractiveHasThisFeedbackAsAChild() {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(INTERACTIVE_ID_VAR))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<UUID> feedBacks = payload.getFeedbacks();
                    UUID feedbackId = UUID.fromString(context.getVariable(FEEDBACK_ID_VAR));
                    assertNotNull(feedBacks);
                    assertTrue(feedBacks.stream().anyMatch(id -> id.equals(feedbackId)));
                })));
    }

    @And("^the \"([^\"]*)\" interactive does not contain the component$")
    public void theInteractiveDoesNotContainTheComponent(String elementName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(Variables.nameFrom(elementName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<UUID> components = payload.getComponents();
                    UUID componentId = UUID.fromString(context.getVariable(COMPONENT_ID_VAR));

                    if (components != null) {
                        assertTrue(components.stream().noneMatch(id -> id.equals(componentId)));
                    }
                })));
    }

    @When("^\"([^\"]*)\" fetches the \"([^\"]*)\" interactive$")
    public void fetchesTheInteractive(String accountName, String interactiveName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(nameFrom(interactiveName, "id")))
                .build());
    }

    @Then("^\"([^\"]*)\" interactive has (\\d+) feedbacks$")
    public void interactiveHasFeedbacks(String interactiveName, int count) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
                    @Override
                    public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(count, payload.getFeedbacks().size());
                    }

                    @Override
                    public String getRootElementName() {
                        return "interactive";
                    }

                    @Override
                    public String getType() {
                        return "author.interactive.get.ok";
                    }
                }));
    }

    @Then("^\"([^\"]*)\" interactive has (\\d+) components$")
    public void interactiveHasComponents(String interactiveName, int count) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
                    @Override
                    public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(count, payload.getComponents().size());
                    }

                    @Override
                    public String getRootElementName() {
                        return "interactive";
                    }

                    @Override
                    public String getType() {
                        return "author.interactive.get.ok";
                    }
                }));
    }

    @Then("^\"([^\"]*)\" interactive has \"([^\"]*)\" feedback(?: and \"([^\"]*)\" component)?$")
    public void interactiveHasFeedbackAndComponent(String interactiveName, String feedbackName, String componentName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
                    @Override
                    public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                        assertEquals(1, payload.getFeedbacks().size());
                        context.setVariable(nameFrom(feedbackName, "id"), payload.getFeedbacks().get(0));
                        if (componentName != null) {
                            assertEquals(1, payload.getComponents().size());
                            context.setVariable(nameFrom(componentName, "id"), payload.getComponents().get(0));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "interactive";
                    }

                    @Override
                    public String getType() {
                        return "author.interactive.get.ok";
                    }
                }));
    }

    @Then("^the interactive payload is successfully fetched$")
    public void theInteractivePayloadIsSuccessfullyFetched() {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    assertEquals(context.getVariable(INTERACTIVE_ID_VAR), payload.getInteractiveId().toString());
                    assertEquals(context.getVariable("interactive_config"), payload.getConfig());
                    assertNotNull(payload.getPlugin());
                    assertEquals(context.getVariable(PATHWAY_ID_VAR), payload.getParentPathwayId().toString());
                    if (payload.getComponents() != null) {
                        assertEquals(1, payload.getComponents().size());
                        assertEquals(context.getVariable(COMPONENT_ID_VAR), payload.getComponents().get(0).toString());
                    }
                    if (payload.getComponents() != null) {
                        assertEquals(1, payload.getFeedbacks().size());
                        assertEquals(context.getVariable(FEEDBACK_ID_VAR), payload.getFeedbacks().get(0).toString());
                    }
                })));
    }

    @Then("^\"([^\"]*)\" can not create an interactive for \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotCreateAnInteractiveForPathwayDueToErrorCodeMessage(String accountName, String pathwayName, int code, String message) {
        createsAnInteractiveForPathway(accountName, pathwayName, null, null);
        interactiveCreationFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can create an interactive for \"([^\"]*)\" pathway successfully$")
    public void canCreateAnInteractiveForPathwaySuccessfully(String accountName, String pathwayName) {
        createsAnInteractiveForPathway(accountName, pathwayName, null, null);
        messageOperations.receiveJSON(runner,
                action -> action.jsonPath("$.type", "author.interactive.create.ok")); //todo validate all fields
    }

    @Then("^\"([^\"]*)\" can not delete \"([^\"]*)\" interactive for \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDeleteInteractiveDueToErrorCodeMessage(String accountName, String interactiveName, String pathwayName,
                                                             int code, String message) {
        deletesTheInteractiveForThePathway(accountName, interactiveName, pathwayName);
        theDeleteFailsDueToCodeAndMessage(interactiveName, code, message);
    }

    @Then("^\"([^\"]*)\" can delete \"([^\"]*)\" interactive for \"([^\"]*)\" pathway successfully$")
    public void canDeleteInteractiveSuccessfully(String accountName, String interactiveName, String pathwayName) {
        deletesTheInteractiveForThePathway(accountName, interactiveName, pathwayName);
        theInteractiveIsDeleted(interactiveName);
    }

    @Then("^\"([^\"]*)\" can not fetch \"([^\"]*)\" interactive due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchInteractiveDueToErrorCodeMessage(String accountName, String interactiveName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, InteractiveHelper.getInteractiveRequest(
                interpolate(nameFrom(interactiveName, "id"))
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(getInteractiveErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can not save config for \"([^\"]*)\" interactive due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSaveConfigForInteractiveDueToErrorCodeMessage(String accountName, String interactiveName, int code, String message) {
        savesConfigurationForTheInteractive(accountName, interactiveName, "{\"foo\":\"bar\"}");
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\": \"author.interactive.config.replace.error\"," +
                        "  \"code\": " + code + "," +
                        "  \"message\": \"" + message + "\"" +
                        "}"));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" interactive successfully$")
    public void canFetchInteractiveSuccessfully(String accountName, String interactiveName) {
        fetchesTheInteractive(accountName, interactiveName);
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.interactive.get.ok")); //todo validate all fields
    }

    @Then("^\"([^\"]*)\" can save config for \"([^\"]*)\" interactive successfully$")
    public void canSaveConfigForInteractiveSuccessfully(String accountName, String interactiveName) {
        savesConfigurationForTheInteractive(accountName, interactiveName, "{\"foo\":\"bar\"}");
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.interactive.config.replace.ok")); //todo validate all fields
    }


    @When("^\"([^\"]*)\" has deleted the \"([^\"]*)\" interactive for the \"([^\"]*)\" pathway$")
    public void hasDeletedTheInteractiveForThePathway(String accountName, String interactiveName, String pathwayName) {
        deletesTheInteractiveForThePathway(accountName, interactiveName, pathwayName);
        theInteractiveIsDeleted(interactiveName);
    }

    @When("^\"([^\"]*)\" has updated the \"([^\"]*)\" interactive config with \"([^\"]*)\"$")
    public void hasUpdatedTheInteractiveConfig(String accountName, String interactiveName, String config) {
        savesConfigurationForTheNamedInteractive(accountName, interactiveName, "{\"foo\":\"" + config + "\"}");
        theNamedInteractiveConfigurationIsSuccessfullySaved(interactiveName);
    }

    @And("^\"([^\"]*)\" has saved configuration \"([^\"]*)\" for the interactive$")
    public void hasSavedConfigurationForTheInteractive(String user, String config) {
        savesConfigurationForTheInteractive(user, null, "{\"foo\":\"" + config + "\"}");
        theInteractiveConfigurationIsSuccessfullySaved();
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" interactive with references to \"([^\"]*)\"$")
    public void hasSavedConfigForInteractiveWithReferencesTo(String user, String interactiveName, String list) {
        String config = replaceByIds(runner, list);
        savesConfigurationForTheInteractive(user, interactiveName, config);
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.interactive.config.replace.ok"));
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" interactive \"([^\"]*)\"$")
    public void hasSavedConfigForInteractive(String user, String interactiveName, String config) {
        savesConfigurationForTheInteractive(user, interactiveName, "{\"foo\":\"" + config + "\"}");
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.interactive.config.replace.ok"));
    }

    @Then("^the interactive payload contains linked items$")
    public void theInteractivePayloadContainsLinkedItems(List<String> expectedItemNames) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<DocumentItemPayload> items = payload.getLinkedDocumentItems();
                    Set<UUID> expectedItemsId = expectedItemNames
                            .stream()
                            .map(name -> UUID.fromString(context.getVariable(nameFrom(name, "id"))))
                            .collect(Collectors.toSet());
                    Set<UUID> actualItemsId = items
                            .stream()
                            .map(DocumentItemPayload::getId)
                            .collect(Collectors.toSet());

                    assertEquals(expectedItemsId, actualItemsId);
                })));
    }

    @Then("{string} interactive has components")
    public void interactiveHasComponents(String interactiveName, List<String> componentNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<InteractivePayload>(InteractivePayload.class) {
                    @Override
                    public void validate(InteractivePayload payload, Map<String, Object> headers, TestContext context) {
                        assertNotNull(payload);
                        String expectedInteractiveId = context.getVariable(nameFrom(interactiveName, "id"));
                        assertEquals(expectedInteractiveId, payload.getInteractiveId().toString());
                        List<UUID> componentIds = payload.getComponents();

                        context.setVariable(nameFrom(interactiveName, "studentScope"), payload.getStudentScopeURN());

                        for (int i = 0; i < componentNames.size(); i++) {
                            String componentName = componentNames.get(i);
                            context.setVariable(nameFrom(componentName, "id"), componentIds.get(i));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "interactive";
                    }

                    @Override
                    public String getType() {
                        return "author.interactive.get.ok";
                    }
                }));
    }

    @Then("the {string} interactive includes the {string} description")
    public void theInteractiveIncludesTheDescription(String interactiveName, String description) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateInteractiveGetResponse(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(interactiveName, "id")));
                    Assertions.assertEquals(activityId, payload.getInteractiveId().toString());
                    Assertions.assertEquals(description, payload.getDescription());
                })));
    }

    @When("{string} test evaluate scenarios for interactive {string} with data")
    public void testEvaluateScenariosForInteractiveWithData(final String accountName, final String interactiveName,
                                                            final String scopeData) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.scenarios.test")
                .addField("interactiveId", interpolate(nameFrom(interactiveName, "id")))
                .addField("scopeData", scopeData)
                .build());
    }

    @Then("the interactive test evaluation result has")
    public void theInteractiveTestEvaluationResultHas(final Map<String, String> fields) {
        messageOperations.receiveJSON(runner, action -> {
            action.jsonPath("$.type", "author.interactive.scenarios.test");
            action.jsonPath("$.response.results", "@notEmpty()@");

           action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
               @Override
               public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                   Map response = (Map) payload.get("response");
                   List<Map> evaluationResults = (List) response.get("results");

                   int expectedFiredScenarios = fields.containsKey("fired_scenarios") ?
                           Integer.parseInt(fields.get("fired_scenarios")) : 0;

                   assertEquals(expectedFiredScenarios, evaluationResults.size());

                   if (fields.containsKey("is_true")) {
                       String scenarioId = context.getVariable(interpolate(nameFrom(fields.get("is_true"), "id")));
                       List<Map> trueScenarios = evaluationResults.stream()
                               .filter(evaluationResult -> evaluationResult.get("scenarioId").toString().equals(scenarioId))
                               .collect(Collectors.toList());

                       assertNotNull(trueScenarios);
                       assertEquals(1, trueScenarios.size());
                        assertTrue((Boolean) trueScenarios.get(0).get("evaluationResult"));

                   }

                   if (fields.containsKey("is_false")) {
                       String scenarioId = context.getVariable(interpolate(nameFrom(fields.get("is_false"), "id")));
                       List<Map> trueScenarios = evaluationResults.stream()
                               .filter(evaluationResult -> evaluationResult.get("scenarioId").toString().equals(scenarioId))
                               .collect(Collectors.toList());

                       assertNotNull(trueScenarios);
                       assertEquals(1, trueScenarios.size());
                       assertNull(trueScenarios.get(0).get("evaluationResult"));
                   }
               }
           });
        });
    }

    @And("{string} has saved configuration for {string} interactive with config")
    public void hasSavedConfigurationForInteractiveWithConfig(String accountName, String activityName, String config) {
        savesConfigurationForTheInteractive(accountName, activityName, config);
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.interactive.config.replace.ok"));
    }
}
