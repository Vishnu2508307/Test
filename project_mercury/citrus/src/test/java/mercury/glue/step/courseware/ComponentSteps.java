package mercury.glue.step.courseware;

import static junit.framework.TestCase.assertTrue;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.courseware.ActivityDuplicateSteps.replaceByIds;
import static mercury.glue.step.courseware.ActivitySteps.ACTIVITY_ID_VAR;
import static mercury.glue.step.courseware.InteractiveSteps.INTERACTIVE_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.ComponentHelper.componentGetErrorResponse;
import static mercury.helpers.courseware.ComponentHelper.createComponentErrorResponse;
import static mercury.helpers.courseware.ComponentHelper.createComponentResponse;
import static mercury.helpers.courseware.ComponentHelper.createComponentResponseWithConfig;
import static mercury.helpers.courseware.ComponentHelper.deleteComponentErrorResponse;
import static mercury.helpers.courseware.ComponentHelper.replaceConfigErrorResponse;
import static mercury.helpers.courseware.ComponentHelper.replaceConfigRequest;
import static mercury.helpers.courseware.ComponentHelper.replaceConfigResponse;
import static mercury.helpers.courseware.ComponentHelper.validateComponentGetResponse;
import static mercury.helpers.courseware.InteractiveHelper.validateInteractiveGetResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.courseware.ComponentHelper;

public class ComponentSteps {

    public static final String COMPONENT_ID_VAR = "component_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates a component for random interactive$")
    public void createsAComponentForRandomInteractive(String accountName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                "interactive",
                UUIDs.timeBased().toString(),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));
    }

    @Then("^([^\"]*) component creation fails with message \"([^\"]*)\" and code (\\d+)$")
    public void componentCreationFailsWithMessageAndCode(String type, String errorMessage, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentErrorResponse(
                type,
                code,
                errorMessage
        )));
    }

    @When("^\"([^\"]*)\" saves configuration for the component$")
    public void savesConfigurationForTheComponent(String accountName, String config) {
        authenticationSteps.authenticateUser(accountName);
        runner.variable("component_config", config);
        messageOperations.sendJSON(runner, replaceConfigRequest(
                interpolate(COMPONENT_ID_VAR),
                config
        ));
    }

    @Then("^the component configuration is successfully saved$")
    public void theComponentConfigurationIsSuccessfullySaved() {
        messageOperations.receiveJSON(runner, action -> action.payload(replaceConfigResponse(
                interpolate(COMPONENT_ID_VAR),
                interpolate("component_config")
        )));
    }

    @When("{string} saves {string} asset configuration for the {string} component")
    public void savesAssetConfigurationForTheComponent(String accountName, String assetName, String componentName, String config) {
        authenticationSteps.authenticateUser(accountName);
        String assetUrn = interpolate(nameFrom(assetName, "urn"));

        if(config.contains("AssetURN")){
            config = config.replace("AssetURN", assetUrn);
        }
        runner.variable("component_config", config);
        messageOperations.sendJSON(runner, replaceConfigRequest(
                interpolate(nameFrom(componentName, "id")),
                config
        ));
    }

    @Then("the {string} component configuration is successfully saved")
    public void componentConfigurationIsSuccessfullySaved(String componentName) {
        messageOperations.receiveJSON(runner, action -> action.payload(replaceConfigResponse(
                interpolate(nameFrom(componentName, "id")),
                interpolate("component_config")
        )));
    }

    @And("^\"([^\"]*)\" has added a \"([^\"]*)\" component to the \"([^\"]*)\" activity$")
    public void hasAddedAComponentToTheActivity(String user, String componentName, String activityName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                "activity",
                interpolate(Variables.nameFrom(activityName, "id")),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                "activity",
                Variables.nameFrom(componentName, "id"),
                interpolate(PLUGIN_ID_VAR)
        )));
    }

    @When("{string} creates a component for the {string}")
    public void createsAComponentForThe(String user, String type) {
        createsAComponentForThe(user, type, null);
    }

    @When("{string} creates a component for {string} with a supplied id")
    public void createsAComponentForWithSuppliedId(String user, String type) {
        authenticationSteps.authenticateUser(user);

        UUID componentId = runner.variable("SUPPLIED_COMPONENT_ID", com.smartsparrow.util.UUIDs.timeBased());
        String varName = type.equals("interactive") ? INTERACTIVE_ID_VAR : ACTIVITY_ID_VAR;

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequestWithSuppliedId(
                type,
                interpolate(varName),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                componentId.toString()
        ));
    }

    @When("{string} creates a component for {string} with the same supplied id as before")
    public void createsAComponentForWithSameSuppliedId(String user, String type) {
        authenticationSteps.authenticateUser(user);

        UUID componentId = com.smartsparrow.util.UUIDs.fromString(getVariableValue("SUPPLIED_COMPONENT_ID"));
        String varName = type.equals("interactive") ? INTERACTIVE_ID_VAR : ACTIVITY_ID_VAR;

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequestWithSuppliedId(
                type,
                interpolate(varName),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                componentId.toString()
        ));
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    public void createsAComponentForThe(String user, String type, String parentName) {
        authenticationSteps.authenticateUser(user);

        String varName = parentName == null ? (type.equals("interactive") ? INTERACTIVE_ID_VAR : ACTIVITY_ID_VAR) : nameFrom(parentName, "id");
        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                type,
                interpolate(varName),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));
    }

    public void createsAComponentWithConfig(String user, String type, String parentName, String config) {
        authenticationSteps.authenticateUser(user);

        String varName = parentName == null ? (type.equals("interactive") ? INTERACTIVE_ID_VAR : ACTIVITY_ID_VAR) : nameFrom(parentName, "id");
        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                type,
                interpolate(varName),
                interpolate(PLUGIN_ID_VAR),
                "1.*", config));
    }

    @Then("the {string} component is successfully created")
    public void theComponentIsSuccessfullyCreated(String type) {
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                type,
                COMPONENT_ID_VAR,
                interpolate(PLUGIN_ID_VAR)
        )));
    }

    @Then("the {string} component is successfully created with the supplied id")
    public void theComponentIsSuccessfullyCreatedWithTheSuppliedId(String type) {
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                type,
                COMPONENT_ID_VAR,
                interpolate(PLUGIN_ID_VAR)
        )).validate("$.response.component.componentId", "${SUPPLIED_COMPONENT_ID}"));
    }

    @Then("the {string} component is not created due to conflict")
    public void theComponentIsNotCreatedDueToConflict(String type) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<>(
                ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                Assertions.assertEquals("author." + type + ".component.create.error", payload.getType());
                Assertions.assertEquals(409, payload.getCode().intValue());
                Assertions.assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Then("^the \"([^\"]*)\" component is successfully created with config \"([^\"]*)\"$")
    public void theComponentIsSuccessfullyCreatedWithConfig(String type, String config) {
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponseWithConfig(
                type,
                COMPONENT_ID_VAR,
                interpolate(PLUGIN_ID_VAR),
                config
        )));
    }

    @And("^\"([^\"]*)\" has created a component for the \"([^\"]*)\" \"([^\"]*)\"$")
    public void hasCreatedAComponentForThe(String user, String elementName, String type) {
        createsAComponentForThe(user, type, elementName);
        theComponentIsSuccessfullyCreated(type);
    }

    @Then("{string} has created an {string} component with config")
    public void hasCreatedAnComponentWithConfig(String user, String type, String config) {
        createsAComponentWithConfig(user, type, null, config);
        theComponentIsSuccessfullyCreatedWithConfig(type, config);
    }

    @Then("{string} has created an {string} {string} component with config")
    public void hasCreatedAnComponentWithConfig(String user,String parentName, String type, String config) {
        createsAComponentWithConfig(user, type, parentName, config);
        theComponentIsSuccessfullyCreatedWithConfig(type, config);
    }

    @When("^\"([^\"]*)\" deletes the \"([^\"]*)\" \"([^\"]*)\" component$")
    public void deletesTheComponent(String user, String elementName, String type) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, ComponentHelper.deleteComponentRequest(
                type,
                interpolate(COMPONENT_ID_VAR),
                interpolate(Variables.nameFrom(elementName, "id"))
        ));
    }

    @Then("^the \"([^\"]*)\" \"([^\"]*)\" component is successfully deleted$")
    public void theComponentIsSuccessfullyDeleted(String elementName, String type) {

        messageOperations.receiveJSON(runner, action -> action.payload(ComponentHelper.deleteComponentResponse(
                type,
                interpolate(COMPONENT_ID_VAR),
                interpolate(Variables.nameFrom(elementName, "id"))
        )));
    }

    @And("^\"([^\"]*)\" has deleted the \"([^\"]*)\" \"([^\"]*)\" component$")
    public void hasDeletedTheComponent(String user,String elementName, String type) {
        deletesTheComponent(user,elementName, type);
        theComponentIsSuccessfullyDeleted(elementName, type);
    }

    @Then("^the component cannot be fetched anymore due to code (\\d+) and message \"([^\"]*)\"$")
    public void theComponentCannotBeFetchedAnymoreDueToCodeAndMessage(int code, String message) {
        messageOperations.sendJSON(runner, ComponentHelper.componentGetRequest(
                interpolate(COMPONENT_ID_VAR)
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(componentGetErrorResponse(code, message)));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" component for the \"([^\"]*)\" activity$")
    public void hasCreatedAComponentForTheActivity(String user, String componentName, String activityName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                "activity",
                interpolate(nameFrom(activityName, "id")),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                "activity",
                nameFrom(componentName, "id"),
                interpolate(PLUGIN_ID_VAR)
        )));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" component for the \"([^\"]*)\" interactive$")
    public void hasCreatedAComponentForTheInteractive(String user, String componentName, String interactiveName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                "interactive",
                interpolate(nameFrom(interactiveName, "id")),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                "interactive",
                nameFrom(componentName, "id"),
                interpolate(PLUGIN_ID_VAR)
        )));
    }

    @And("{string} has created {int} components for the {string} interactive with config")
    public void hasCreatedComponentsForTheInteractiveWithConfig(String user, int count, String interactiveName, String config) {
        authenticationSteps.authenticateUser(user);

        for (int i = 0; i < count; i++) {
            String componentName = "Comp" + i;
            messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                    "interactive",
                    interpolate(nameFrom(interactiveName, "id")),
                    interpolate(PLUGIN_ID_VAR),
                    "1.*",
                    config
            ));

            messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponseWithConfig(
                    "interactive",
                    nameFrom(componentName, "id"),
                    interpolate(PLUGIN_ID_VAR),
                    config
            )));
        }
    }

    @Then("^\"([^\"]*)\" can not create a component for \"([^\"]*)\" ([^\"]*) due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotCreateAComponentDueToErrorCodeMessage(String accountName, String parentName, String type, int code, String message) {
        createsAComponentForThe(accountName, type, parentName);
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentErrorResponse(type, code, message)));
    }

    @Then("^\"([^\"]*)\" can not delete \"([^\"]*)\" component from \"([^\"]*)\" ([^\"]*) due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDeleteComponentFromInteractiveDueToErrorCodeMessage(String accountName, String componentName,
                                                                          String parentName, String type, int code, String message) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, ComponentHelper.deleteComponentRequest(
                type,
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(parentName, "id"))
        ));
        messageOperations.receiveJSON(runner, action -> action.payload(deleteComponentErrorResponse(type, code, message)));
    }

    @Then("^\"([^\"]*)\" can not fetch \"([^\"]*)\" component due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchComponentDueToErrorCodeMessage(String accountName, String componentName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, ComponentHelper.componentGetRequest(interpolate(nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.payload(componentGetErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can not save config for \"([^\"]*)\" component due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSaveConfigForComponentDueToErrorCodeMessage(String accountName, String componentName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, replaceConfigRequest(interpolate(nameFrom(componentName, "id")), "any config"));
        messageOperations.receiveJSON(runner, action -> action.payload(replaceConfigErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" component successfully$")
    public void canFetchComponentSuccessfully(String accountName, String componentName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, ComponentHelper.componentGetRequest(interpolate(nameFrom(componentName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.component.get.ok"));
    }

    @Then("^\"([^\"]*)\" can save config for \"([^\"]*)\" component successfully$")
    public void canSaveConfigForComponentSuccessfully(String accountName, String componentName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, replaceConfigRequest(interpolate(nameFrom(componentName, "id")), "any config"));
        messageOperations.receiveJSON(runner, action -> action.payload(
                replaceConfigResponse(interpolate(nameFrom(componentName, "id")), "any config")));
    }

    @Then("^\"([^\"]*)\" can create a component for \"([^\"]*)\" ([^\"]*) successfully$")
    public void canCreateAComponentSuccessfully(String accountName, String parentName, String type) {
        createsAComponentForThe(accountName, type, parentName);
        theComponentIsSuccessfullyCreated(type);
    }

    @Then("^\"([^\"]*)\" can delete \"([^\"]*)\" component from \"([^\"]*)\" ([^\"]*) successfully$")
    public void canDeleteComponentFromActivitySuccessfully(String accountName, String componentName, String parentName, String type) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, ComponentHelper.deleteComponentRequest(
                type,
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(parentName, "id"))
        ));
        messageOperations.receiveJSON(runner, action -> action.payload(ComponentHelper.deleteComponentResponse(
                type,
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(parentName, "id"))
        )));
    }

    @When("^\"([^\"]*)\" has deleted the \"([^\"]*)\" component for the \"([^\"]*)\" activity$")
    public void hasDeletedTheComponentForTheActivity(String user, String componentName, String activityName) {
        hasDeletedAComponentForEntity(user, "activity", componentName, activityName);
    }

    @When("^\"([^\"]*)\" has deleted the \"([^\"]*)\" component for the \"([^\"]*)\" interactive$")
    public void hasDeletedTheComponentForTheInteractive(String user, String componentName, String interactiveName) {
        hasDeletedAComponentForEntity(user, "interactive", componentName, interactiveName);

    }

    @When("^\"([^\"]*)\" replace the \"([^\"]*)\" component config with$")
    public void replaceTheComponentConfigWith(String user, String componentName, String config) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, ComponentHelper.replaceConfigRequest(
                interpolate(nameFrom(componentName, "id")),
                config
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(ComponentHelper.replaceConfigResponse(
                interpolate(nameFrom(componentName, "id")),
                config
        )));
    }

    private void hasDeletedAComponentForEntity(String user, String type, String componentName, String entityName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, ComponentHelper.deleteComponentRequest(
                type,
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(entityName, "id"))
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(ComponentHelper.deleteComponentResponse(
                type,
                interpolate(nameFrom(componentName, "id")),
                interpolate(nameFrom(entityName, "id"))
        )));
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" component with references to \"([^\"]*)\"$")
    public void hasSavedConfigForComponentWithReferencesTo(String user, String componentName, String list) {
        String config = replaceByIds(runner, list);
        hasSavedConfigForComponent(user, componentName, config);
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" component \"([^\"]*)\"$")
    public void hasSavedConfigForComponent(String user, String componentName, String config) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, replaceConfigRequest(interpolate(nameFrom(componentName, "id")), config));
        messageOperations.receiveJSON(runner, action -> action.payload(
                replaceConfigResponse(interpolate(nameFrom(componentName, "id")), config)));
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" component$")
    public void hasSavedConfigForComponentText(String user, String componentName, String config) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, replaceConfigRequest(interpolate(nameFrom(componentName, "id")), config));
        messageOperations.receiveJSON(runner, action -> action.payload(
                replaceConfigResponse(interpolate(nameFrom(componentName, "id")), config)));
    }

    @When("^\"([^\"]*)\" fetches the \"([^\"]*)\" component")
    public void fetchesTheComponent(String user, String componentName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, ComponentHelper.componentGetRequest(interpolate(nameFrom(componentName, "id"))));
    }

    @When("{string} set {string} manual grading configurations with")
    public void setManualGradingConfigurationsWith(String user, String componentName, Map<String, String> args) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.component.manual.grading.configuration.set")
                .addField("componentId", interpolate(nameFrom(componentName, "id")))
                .addField("maxScore", Double.valueOf(args.get("maxScore")))
                .build());
    }

    @Then("the manual grading configuration is not {string} due to missing permission level")
    public void theManualGradingConfigurationIsNotDueToMissingPermissionLevel(String type) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.component.manual.grading.configuration." + type + ".error")
                        .jsonPath("$.code", 401));
    }

    @Then("the manual grading configuration for {string} is set with")
    public void theManualGradingConfigurationForIsSetWith(String componentOne, Map<String, String> args) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.manualGradingConfiguration.componentId", interpolate(nameFrom(componentOne, "id")))
                        .jsonPath("$.type", "author.component.manual.grading.configuration.set.ok")
                        .jsonPath("$.response.manualGradingConfiguration.maxScore", Double.valueOf(args.get("maxScore"))));
    }

    @Given("{string} has set {string} manual grading configurations with")
    public void hasSetManualGradingConfigurationsWith(String user, String componentName, Map<String, String> args) {
        setManualGradingConfigurationsWith(user, componentName, args);
        theManualGradingConfigurationForIsSetWith(componentName, args);
    }

    @When("{string} deletes {string} manual grading configurations")
    public void deletesManualGradingConfigurations(String user, String componentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.component.manual.grading.configuration.delete")
                .addField("componentId", interpolate(nameFrom(componentName, "id")))
                .build());
    }

    @Then("the manual grading configuration is successfully deleted")
    public void theManualGradingConfigurationIsSuccessfullyDeleted() {
        messageOperations.validateResponseType(runner, "author.component.manual.grading.configuration.delete.ok");
    }

    @When("{string} fetches the manual grading configurations for {string}")
    public void fetchesTheManualGradingConfigurationsFor(String user, String componentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.component.manual.grading.configuration.get")
                .addField("componentId", interpolate(nameFrom(componentName, "id")))
                .build());
    }

    @Then("the {string} manual grading configuration is returned with")
    public void theManualGradingConfigurationIsReturnedWith(String componentName, Map<String, String> args) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.manualGradingConfiguration.componentId", interpolate(nameFrom(componentName, "id")))
                        .jsonPath("$.type", "author.component.manual.grading.configuration.get.ok")
                        .jsonPath("$.response.manualGradingConfiguration.maxScore", Double.valueOf(args.get("maxScore"))));
    }

    @Given("{string} has deleted {string} manual grading configurations")
    public void hasDeletedManualGradingConfigurations(String user, String componentName) {
        deletesManualGradingConfigurations(user, componentName);
        theManualGradingConfigurationIsSuccessfullyDeleted();
    }

    @Then("the {string} manual grading configuration is empty")
    public void theManualGradingConfigurationIsEmpty(String componentName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.manualGradingConfiguration", "{}"));
    }

    @Then("the {string} component includes the {string} description")
    public void theComponentIncludesTheDescription(String componentName, String description) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validateComponentGetResponse(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(componentName, "id")));
                    assertEquals(activityId, payload.getComponentId().toString());
                    assertEquals(description, payload.getDescription());
                })));
    }

    @And("{string} has saved configuration for {string} component with config")
    public void hasSavedConfigurationForComponentWithConfig(String accountName, String componentName, String config) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, replaceConfigRequest(interpolate(nameFrom(componentName, "id")), config));
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.component.replace.ok"));

    }

    @And("{string} has created a component {string} for the {string} {string}")
    public void hasCreatedAComponentForThe(String user, String componentName, String elementName, String type) {
        authenticationSteps.authenticateUser(user);
        String varName = elementName == null ? (type.equals("interactive") ? INTERACTIVE_ID_VAR : ACTIVITY_ID_VAR) : nameFrom(
                elementName,
                "id");
        messageOperations.sendJSON(runner, ComponentHelper.createComponentRequest(
                type,
                interpolate(varName),
                interpolate(PLUGIN_ID_VAR),
                "1.*",
                null
        ));
        messageOperations.receiveJSON(runner, action -> action.payload(createComponentResponse(
                type,
                componentName,
                interpolate(PLUGIN_ID_VAR)
        )));
    }

    @When("{string} deleted the {string} {string} component {string}")
    public void deletedTheComponent(String user, String elementName, String type, String componentName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, ComponentHelper.deleteComponentRequest(
                type,
                interpolate(componentName),
                interpolate(Variables.nameFrom(elementName, "id"))
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(ComponentHelper.deleteComponentResponse(
                type,
                interpolate(componentName),
                interpolate(Variables.nameFrom(elementName, "id"))
        )));
    }

    @And("the {string} interactive does not contain the components {string}")
    public void theInteractiveDoesNotContainTheComponents(String elementName, String componentName) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(Variables.nameFrom(elementName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<UUID> components = payload.getComponents();
                    UUID componentId = UUID.fromString(context.getVariable(componentName));

                    if (components != null) {
                        assertTrue(components.stream().noneMatch(id -> id.equals(componentId)));
                    }
                })));
    }

    @Then("{string} restored deleted components for the interactive {string}")
    public void restoredDeletedComponentsForTheInteractive(String user,
                                                           String elementName,
                                                           List<String> componentNames) {
        authenticationSteps.authenticateUser(user);

        List<String> componentIds = componentNames.stream()
                .map(one -> Variables.interpolate(one))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.interactive.component.restore")
                .addField("interactiveId", interpolate(Variables.nameFrom(elementName, "id")))
                .addField("componentIds", componentIds)
                .build();
        messageOperations.sendJSON(runner, message);

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "components";
                    }

                    @Override
                    public String getType() {
                        return "author.interactive.component.restore.ok";
                    }

                    @Override
                    public void validate(ArrayList payloads, Map<String, Object> headers, TestContext context) {
                        assertNotNull(payloads);
                        for (int i = 0; i < payloads.size(); i++) {
                            final Map componetPayload = (Map) payloads.get(i);
                            assertNotNull(componetPayload);
                        }
                    }
                }));
    }

    @And("the interactive {string} contains following components")
    public void theInteractiveContainsFollowingComponent(String elementName, List<String> componentNames) {
        List<String> componentIds = componentNames.stream()
                .map(one -> Variables.interpolate(one))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.interactive.get")
                .addField("interactiveId", interpolate(Variables.nameFrom(elementName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(validateInteractiveGetResponse((payload, context) -> {
                    List<UUID> components = payload.getComponents();
                    componentIds.stream().forEach(component ->
                                                          assertTrue(components.contains(UUID.fromString(context.getVariable(
                                                                  component)))));
                })));
    }

    @When("{string} restore deleted components for the interactive {string}")
    public void restoreDeletedComponentsForTheInteractive(String user,
                                                          String elementName,
                                                          List<String> componentNames) {
        authenticationSteps.authenticateUser(user);

        List<String> componentIds = componentNames.stream()
                .map(one -> Variables.interpolate(one))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.interactive.component.restore")
                .addField("interactiveId", interpolate(Variables.nameFrom(elementName, "id")))
                .addField("componentIds", componentIds)
                .build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("{string} not able to restore components due to missing permission level")
    public void notAbleToRestoreComponentsDueToMissingPermissionLevel(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.interactive.component.restore.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} deleted the {string} {string} components")
    public void deletedTheComponents(String user, String elementName, String type, List<String> componentNames) {
        authenticationSteps.authenticateUser(user);

        List<String> componentIds = componentNames.stream()
                .map(one -> Variables.interpolate(one))
                .collect(Collectors.toList());
        messageOperations.sendJSON(runner, ComponentHelper.multiDeleteComponentRequest(
                type,
                componentIds,
                interpolate(Variables.nameFrom(elementName, "id"))
        ));

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.interactive.components.delete.ok\"," +
                                       "\"response\":{" +
                                       "\"componentIds\":\"@notEmpty()@\"," +
                                       "\"interactiveId\":\"@notEmpty()@\"" +
                                       "},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the component {string} cannot be fetched anymore due to code {int} and message {string}")
    public void theComponentCannotBeFetchedAnymoreDueToCodeAndMessage(String componetName, int code, String message) {
        messageOperations.sendJSON(runner, ComponentHelper.componentGetRequest(
                interpolate(componetName)
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(componentGetErrorResponse(code, message)));
    }

    @When("{string} moves the {string} component to {string} {string}")
    public void movesTheComponentTo(String user, String sourceElementName,
                                    String destinationElementName, String destinationElementType) {
        authenticationSteps.authenticateUser(user);

        List<String> list = new ArrayList<>();
        list.add(interpolate(COMPONENT_ID_VAR));

        messageOperations.sendJSON(runner, ComponentHelper.moveComponentsRequest(list,
                interpolate(Variables.nameFrom(destinationElementName, "id")), destinationElementType)
        );
    }

    @Then("the {string} components are moved successfully")
    public void theComponentsAreSuccessfullyMoved(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.components.move.ok\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} moves {string} components to {string} {string}")
    public void movesTheComponentsTo(String user, String sourceElementName, String destinationElementName,
                                    String destinationElementType, List<String> componentNames) {
        authenticationSteps.authenticateUser(user);

        List<String> componentIds = componentNames.stream()
                    .map(one -> Variables.interpolate(one))
                    .collect(Collectors.toList());

        messageOperations.sendJSON(runner, ComponentHelper.moveComponentsRequest(componentIds,
                interpolate(Variables.nameFrom(destinationElementName, "id")), destinationElementType)
        );
    }

    @Then("{string} not able to move components due to missing permission level")
    public void notAbleToMoveComponentsDueToMissingPermissionLevel(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.components.move.error\"," +
                        "\"code\":401," +
                        "\"message\":\"@notEmpty()@\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} has moved the {string} component to {string} {string}")
    public void hasMovedTheComponentToInteractive(String user, String sourceElement, String elementName,
                                                  String elementType, List<String> componentNames) {
        movesTheComponentsTo(user, sourceElement, elementName, elementType, componentNames);
        theComponentsAreSuccessfullyMoved(elementName);
    }
}
