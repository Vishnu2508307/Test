package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.AuthenticationSteps.CURRENT_ACCOUNT;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.courseware.ActivityDuplicateSteps.replaceByIds;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.ActivityHelper.activityOk;
import static mercury.helpers.courseware.ActivityHelper.activityResponseOk;
import static mercury.helpers.courseware.ActivityHelper.createChildActivity;
import static mercury.helpers.courseware.ActivityHelper.createChildActivityOk;
import static mercury.helpers.courseware.ActivityHelper.createChildActivityOkWithPluginName;
import static mercury.helpers.courseware.ActivityHelper.createChildActivityWithId;
import static mercury.helpers.courseware.ActivityHelper.deleteChildActivity;
import static mercury.helpers.courseware.ActivityHelper.deleteChildActivityOk;
import static mercury.helpers.courseware.ActivityHelper.getActivityRequest;
import static mercury.helpers.courseware.ActivityHelper.getProgressQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import junit.framework.TestCase;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.glue.step.PluginSteps;
import mercury.glue.step.ProvisionSteps;

public class ActivitySteps {

    public static final String ACTIVITY_ID_VAR = "activity_id";
    private UUID ACTIVITY_ID_PREGENERATED;

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Autowired
    private PluginSteps pluginSteps;

    @Autowired
    private ObjectMapper objectMapper;

    String config = "{\n" +
            "    \"title\": {\n" +
            "        \"type\": \"text\"\n" +
            "    },\n" +
            "    \"description\": {\n" +
            "        \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"listType\": \"text\",\n" +
            "        \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"listType\": \"text\",\n" +
            "        \"learnerEditable\": true,\n" +
            "        \"label\": \"selection\"\n" +
            "    },\n" +
            "    \"options\": {\n" +
            "        \"type\": \"group\",\n" +
            "        \"flat\": true,\n" +
            "        \"properties\": {\n" +
            "            \"allowMultipleSelections\": {\n" +
            "                \"type\": \"boolean\",\n" +
            "                \"default\": false,\n" +
            "                \"label\": \"allow mulit-select\"\n" +
            "            },\n" +
            "            \"layout\": {\n" +
            "                \"type\": \"enum\",\n" +
            "                \"items\": [\n" +
            "                    \"vertical\",\n" +
            "                    \"horizontal\"\n" +
            "                ],\n" +
            "                \"default\": \"vertical\"\n" +
            "            },\n" +
            "            \"foo\": {\n" +
            "                \"type\": \"text\",\n" +
            "                \"learnerEditable\": true,\n" +
            "                \"default\": \"default\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"cards\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"label\": \"Cards\",\n" +
            "        \"description\": \"The images in the component.\",\n" +
            "        \"listType\": {\n" +
            "            \"type\": \"group\",\n" +
            "            \"properties\": {\n" +
            "                \"front-image\": {\n" +
            "                    \"type\": \"image\",\n" +
            "                    \"label\": \"Front Image\",\n" +
            "                    \"description\": \"The image (optional) on the front (default side) of the card\"\n" +
            "                },\n" +
            "                \"front-text\": {\n" +
            "                    \"type\": \"rich-text\",\n" +
            "                    \"label\": \"Front Text\",\n" +
            "                    \"default\": \"Front text\",\n" +
            "                    \"description\": \"The text on the front (default side) of the card\"\n" +
            "                },\n" +
            "                \"back-image\": {\n" +
            "                    \"type\": \"image\",\n" +
            "                    \"label\": \"Back Image\",\n" +
            "                    \"description\": \"The image (optional) on the back (default side) of the card\"\n" +
            "                },\n" +
            "                \"back-text\": {\n" +
            "                    \"type\": \"rich-text\",\n" +
            "                    \"label\": \"Back Text\",\n" +
            "                    \"default\": \"Back text\",\n" +
            "                    \"description\": \"The text on the back (default side) of the card\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"default\": [\n" +
            "            {\n" +
            "                \"label\": \"Card 1\",\n" +
            "                \"front-image\": \"\",\n" +
            "                \"front-text\": \"Front text\",\n" +
            "                \"back-text\": \"Back text\",\n" +
            "                \"back-image\": \"\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"label\": \"Card 2\",\n" +
            "                \"front-image\": \"\",\n" +
            "                \"front-text\": \"Front text\",\n" +
            "                \"back-text\": \"Back text\",\n" +
            "                \"back-image\": \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"stage\": {\n" +
            "        \"type\": \"group\",\n" +
            "        \"properties\": {},\n" +
            "        \"hidden\": true\n" +
            "    }\n" +
            "}\n";


    @Then("^the activity is successfully created$")
    public void theActivityIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                activityOk("author.activity.create.ok",
                        "@variable('" + ACTIVITY_ID_VAR + "')@",
                        "${" + PLUGIN_ID_VAR + "}",
                        "@notEmpty()@",
                        "@notEmpty()@")));
    }


    @When("^\"([^\"]*)\" creates a \"([^\"]*)\" activity for the \"([^\"]*)\" pathway with some random pluginId$")
    public void createsAActivityForThePathwayWithRandomPluginId(String user, String activityName, String pathwayName) throws Throwable {
        authenticationSteps.authenticateUser(user);

        //pluginSteps.coursePluginShouldExist(user);
        messageOperations.sendJSON(runner, createChildActivity(
                "${" + PLUGIN_ID_VAR + "}",
                "2.*",
                Variables.interpolate(Variables.nameFrom(pathwayName, "id"))
        ));
    }


    @Then("^the activity creation fails with message that version doesn't exist$")
    public void theActivityCreationFails() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("author.activity.create.error", payload.getType());
                assertEquals(404, payload.getCode().intValue());
                assertTrue(payload.getMessage().matches("Plugin not found"));
                assertNotNull(payload.getReplyTo());
            }
        }));
    }

    /*@And("^\"([^\"]*)\" has created an activity$")
    public void hasCreatedAnActivity(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);
        //create a plugin
        pluginSteps.coursePluginShouldExist(accountName);
        createsAnActivityWithThisPlugin(accountName);
        messageOperations.receiveJSON(runner, action ->
                action.payload(activityOk("author.activity.create.ok",
                        "@variable('" + ACTIVITY_ID_VAR + "')@",
                        "${" + PLUGIN_ID_VAR + "}",
                        "@notEmpty()@",
                        "@notEmpty()@")));
    }*/

    @When("^(?:|she|he) saves configuration for the \"([^\"]*)\" activity$")
    public void savesConfigurationForTheActivity(String activityName, String config) {
        //authenticate current user if needed
        runner.variable("activity_config", config);
        String escaped = StringEscapeUtils.escapeJava(config);
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.config.replace\"," +
                "    \"activityId\":\"" + interpolate(nameFrom(activityName, "id")) + "\"," +
                "    \"config\" : \"" + escaped + "\"" +
                "}");
    }


    @When("^\"([^\"]*)\" saves configuration \"([^\"]*)\" for \"([^\"]*)\" activity$")
    public void savesConfigurationForTheActivity(String accountName, String config, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        runner.variable("activity_config", config);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.config.replace")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("config", config)
                .build());
    }

    @Then("^the activity configuration is successfully saved$")
    public void theConfigurationIsSuccessfullySaved() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.activity.config.replace.ok")
                        .jsonPath("$.response.activity.config", "${activity_config}")
                        .jsonPath("$.response.activity.updatedAt", "@notEmpty()@"));
    }


    @Then("^the configuration replacing fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theConfigurationReplacingFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.activity.config.replace.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"
        ));
    }

    @And("^(?:|she|he) has saved configuration for the activity$")
    public void sheHasSavedConfigurationForTheActivity(String config) {
        savesConfigurationForTheActivity(config, null);
        theConfigurationIsSuccessfullySaved();
    }

    @And("^\"([^\"]*)\" has saved configuration for \"([^\"]*)\" activity$")
    public void hasSavedConfigurationForActivity(String accountName, String activityName) {
       savesConfigurationForTheActivity(accountName, config, activityName);
        theConfigurationIsSuccessfullySaved();
    }

    @And("{string} has saved configuration for {string} activity with config")
    public void hasSavedConfigurationForActivityWithConfig(String accountName, String activityName, String config) {
        savesConfigurationForTheActivity(accountName, config, activityName);
        theConfigurationIsSuccessfullySaved();
    }

    @When("^(?:|she|he) fetches the activity$")
    public void sheFetchesAnActivity() {
        //authenticate current user if needed
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.get\"," +
                "    \"activityId\": \"${" + ACTIVITY_ID_VAR + "}\"" +
                "}");
    }

    @When("^(?:|she|he) fetches the \"([^\"]*)\" activity$")
    public void sheFetchesAnActivity(String activityName) {
        //authenticate current user if needed
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.get\"," +
                "    \"activityId\": \"" + interpolate(nameFrom(activityName, "id")) + "\"" +
                "}");
    }

    @Then("^the \"([^\"]*)\" activity is successfully fetched$")
    public void theActivityIsSuccessfullyFetched(String elementName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ActivityPayload activity, Map<String, Object> headers, TestContext context) {
                        assertActivity(activity, context, elementName);
                        assertActivityConfig(activity, context);
                        assertActivityTheme(activity, context);
                    }
                }));
    }

    static void assertActivity(ActivityPayload activity, TestContext context, String elementName) {
        assertEquals(context.getVariable(nameFrom(elementName, "id")), activity.getActivityId().toString());
        assertEquals(context.getVariable(PLUGIN_ID_VAR), activity.getPlugin().getPluginId().toString());
        assertEquals("Course Citrus plugin", activity.getPlugin().getName());
        assertEquals("course", activity.getPlugin().getType().getLabel());
        assertEquals("1.*", activity.getPlugin().getVersionExpr());
        String accountName = context.getVariable(CURRENT_ACCOUNT);
        assertEquals(context.getVariable(ProvisionSteps.getAccountIdVar(accountName)),
                activity.getCreator().getAccountId().toString());
        assertEquals(context.getVariable(ProvisionSteps.getSubscriptionIdVar(accountName)),
                activity.getCreator().getSubscriptionId().toString());
        assertEquals(context.getVariable(ProvisionSteps.getAccountEmailVar(accountName)), activity.getCreator().getPrimaryEmail());
        assertNotNull(activity.getCreatedAt());
    }

    static void assertActivityConfig(ActivityPayload activity, TestContext context) {
        try {
            assertEquals(context.getVariable("activity_config"), activity.getConfig());
            assertNotNull(activity.getUpdatedAt());
        } catch (CitrusRuntimeException ex) {
            if (ex.getMessage().equalsIgnoreCase("Unknown variable 'activity_config'")) {
                assertNull(activity.getConfig());
                assertNull(activity.getUpdatedAt());
            } else {
                throw ex;
            }
        }
    }

    static void assertActivityTheme(ActivityPayload activity, TestContext context) {
        try {
            assertEquals(context.getVariable("activity_theme_config"), activity.getActivityTheme());
        } catch (CitrusRuntimeException ex) {
            if (ex.getMessage().equalsIgnoreCase("Unknown variable 'activity_theme_config'")) {
                assertNull(activity.getActivityTheme());
            } else {
                throw ex;
            }
        }
    }

    @When("^\"([^\"]*)\" replace the activity \"([^\"]*)\" theme with$")
    public void replaceTheActivityThemeWith(String user, String activityName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(user);
        replaceTheActivityThemeWith(activityName, fields.toString());
    }

    private void replaceTheActivityThemeWith(String activityName, String theme) {
        runner.variable("activity_theme_config", theme);

        String activityVar = activityName == null ? ACTIVITY_ID_VAR : nameFrom(activityName, "id");

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.theme.replace")
                .addField("activityId", interpolate(activityVar))
                .addField("config", theme).build());
    }

    @Then("^the activity theme is replaced successfully$")
    public void theActivityThemeIsReplacedSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.activity.theme.replace.ok\"," +
                        "\"response\":{" +
                        "\"activityTheme\":{" +
                        "\"id\":\"@notEmpty()@\"," +
                        "\"activityId\":\"@notEmpty()@\"," +
                        "\"config\":\"mercury:escapeJson('${activity_theme_config}')\"}}," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    private void theActivityThemeIsReplacedSuccessfully(@Nullable final String activityName) {

        final String activityId = (activityName != null) ? nameFrom(activityName, "id") : ACTIVITY_ID_VAR;

        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.activity.theme.replace.ok\"," +
                        "\"response\":{" +
                        "\"activityTheme\":{" +
                        "\"id\":\"@notEmpty()@\"," +
                        "\"activityId\":\"" + interpolate(activityId) + "\"," +
                        "\"config\":\"mercury:escapeJson('${activity_theme_config}')\"}}," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @And("^(?:|she|he) has saved theme for the activity$")
    public void sheHasSavedThemeForTheActivity(String theme) {
        //authenticate current user if needed
        replaceTheActivityThemeWith(null, theme);
        theActivityThemeIsReplacedSuccessfully();
    }

    @When("^\"([^\"]*)\" creates a \"([^\"]*)\" activity for the \"([^\"]*)\" pathway$")
    public void createsAActivityForThePathway(String user, String activityName, String pathwayName) throws Throwable {
        authenticationSteps.authenticateUser(user);

        pluginSteps.coursePluginShouldExist(user);
        messageOperations.sendJSON(runner, createChildActivity(
                "${" + PLUGIN_ID_VAR + "}",
                "1.*",
                Variables.interpolate(Variables.nameFrom(pathwayName, "id"))
        ));
    }

    @When("^\"([^\"]*)\" creates a \"([^\"]*)\" activity for the \"([^\"]*)\" pathway with id$")
    public void createsAActivityForThePathwayWithId(String user, String activityName, String pathwayName) throws Throwable {
        ACTIVITY_ID_PREGENERATED = UUIDs.timeBased();
        authenticationSteps.authenticateUser(user);

        pluginSteps.coursePluginShouldExist(user);
        messageOperations.sendJSON(runner, createChildActivityWithId(
                "${" + PLUGIN_ID_VAR + "}",
                "1.*",
                Variables.interpolate(Variables.nameFrom(pathwayName, "id")),
                ACTIVITY_ID_PREGENERATED.toString()
        ));
    }

    @When("^\"([^\"]*)\" creates a \"([^\"]*)\" activity for the \"([^\"]*)\" pathway with the same id$")
    public void createsAActivityForThePathwayWithSameId(String user, String activityName, String pathwayName) throws Throwable {
        authenticationSteps.authenticateUser(user);

        pluginSteps.coursePluginShouldExist(user);
        messageOperations.sendJSON(runner, createChildActivityWithId(
                "${" + PLUGIN_ID_VAR + "}",
                "1.*",
                Variables.interpolate(Variables.nameFrom(pathwayName, "id")),
                ACTIVITY_ID_PREGENERATED.toString()
        ));
    }

    @And("{string} has created activity {string} inside pathway {string}")
    public void createsActivityForThePathway(final String user, final String activityName, final String pathwayName) throws Throwable {
        createsAActivityForThePathway(user, activityName, pathwayName);
        theActivityForThePathwayIsSuccessfullyCreated(activityName, pathwayName);
    }

    @Then("^the \"([^\"]*)\" activity for the \"([^\"]*)\" pathway is successfully created$")
    public void theActivityForThePathwayIsSuccessfullyCreated(String activityName, String pathwayName) {
        String activityVar = activityName == null ? ACTIVITY_ID_VAR : nameFrom(activityName, "id");
        messageOperations.receiveJSON(runner, action ->
                action.payload(createChildActivityOk("author.activity.create.ok",
                        "@variable('" + activityVar + "')@",
                        "${" + PLUGIN_ID_VAR + "}",
                        "@notEmpty()@",
                        "@notEmpty()@",
                        Variables.interpolate(Variables.nameFrom(pathwayName, "id"))))
                        .extractFromPayload("$.response.activity.studentScopeURN", nameFrom(activityName, "studentScope")));
    }

    @When("^\"([^\"]*)\" fetches the \"([^\"]*)\" activity$")
    public void fetchesTheActivity(String user,String  elementName) {

        if (user != null) {
            authenticationSteps.authenticateUser(user);
        }

        messageOperations.sendJSON(runner,
                                   getActivityRequest(Variables.interpolate(Variables.nameFrom(elementName, "id"))));
    }

    @And("^\"([^\"]*)\" has created a \"([^\"]*)\" activity for the \"([^\"]*)\" pathway$")
    public void hasCreatedAActivityForThePathway(String user, String activityName, String pathwayName) throws Throwable {
        createsAActivityForThePathway(user, activityName, pathwayName);
        theActivityForThePathwayIsSuccessfullyCreated(activityName, pathwayName);
    }

    @When("^\"([^\"]*)\" deletes the \"([^\"]*)\" activity for the \"([^\"]*)\" pathway$")
    public void deletesTheActivityForThePathway(String user, String activityName, String pathwayName) {
        authenticationSteps.authenticateUser(user);

        String parentPathwayId = (pathwayName != null ?
                Variables.interpolate(Variables.nameFrom(pathwayName, "id")) : UUID.randomUUID().toString());

        messageOperations.sendJSON(runner, deleteChildActivity(
                Variables.interpolate(Variables.nameFrom(activityName, "id")),
                parentPathwayId
        ));
    }

    @Then("^the \"([^\"]*)\" activity is deleted$")
    public void theActivityIsDeleted(String activityName) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                deleteChildActivityOk(Variables.interpolate(Variables.nameFrom(activityName, "id")))
        ));
    }

    @And("^the \"([^\"]*)\" activity does not have a parent pathway$")
    public void theActivityDoesNotHaveAParentPathway(String activityName) {
        fetchesTheActivity(null, activityName);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.activity.get.error\"," +
                                       "\"code\":" + 404 + "," +
                                       "\"message\":\"" + "@notEmpty()@" + "\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("^\"([^\"]*)\" deletes the \"([^\"]*)\" activity$")
    public void deletesTheActivity(String user, String activityName) {
        deletesTheActivityForThePathway(user, activityName, null);
    }

    @Then("^the activity deleting fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theActivityDeletingFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.activity.delete.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @And("^the \"([^\"]*)\" activity does not have the \"([^\"]*)\" pathway as child$")
    public void theActivityDoesNotHaveThePathwayAsChild(String activityName, String pathwayName) {
        fetchesTheActivity(null, activityName);
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                        UUID childPathwayId = UUID.fromString(context.getVariable(Variables.nameFrom(pathwayName, "id")));

                        if (payload.getChildrenPathways() != null) {
                            assertEquals(0, payload.getChildrenPathways()
                                    .stream()
                                    .filter(childPathwayId::equals)
                                    .collect(Collectors.toList()).size());
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }
                }));
    }

    @Then("^the \"([^\"]*)\" activity has (\\d+) component and (\\d+) pathway as children$")
    public void theActivityHasComponentAndPathwayAsChildren(String activityName, int componentsChildrenCount,
                                                            int pathwayChildrenCount) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                        UUID activityId = UUID.fromString(context.getVariable(Variables.nameFrom(activityName, "id")));

                        assertNotNull(payload);
                        assertEquals(activityId, payload.getActivityId());
                        if (componentsChildrenCount > 0) {
                            assertEquals(componentsChildrenCount, payload.getComponents().size());
                        } else {
                            assertNull(payload.getComponents());
                        }
                        if (pathwayChildrenCount > 0) {
                            assertEquals(pathwayChildrenCount, payload.getChildrenPathways().size());
                        } else
                            assertNull(payload.getChildrenPathways());
                    }

                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }
                }));
    }

    @Then("^the \"([^\"]*)\" \"([^\"]*)\" activity has (\\d+) component and (\\d+) pathway as children$")
    public void theActivityHasComponentAndPathwayAsChildren(String elementName, String activityName, int componentsChildrenCount,
                                                            int pathwayChildrenCount) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                        UUID activityId = UUID.fromString(context.getVariable(Variables.nameFrom(elementName, "id")));

                        assertNotNull(payload);
                        assertEquals(activityId, payload.getActivityId());
                        if (componentsChildrenCount > 0) {
                            assertEquals(componentsChildrenCount, payload.getComponents().size());
                        } else {
                            assertNull(payload.getComponents());
                        }
                        if (pathwayChildrenCount > 0) {
                            assertEquals(pathwayChildrenCount, payload.getChildrenPathways().size());
                        } else
                            assertNull(payload.getChildrenPathways());
                    }

                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }
                }));
    }

    @Then("^\"([^\"]*)\" activity has a \"([^\"]*)\" pathway$")
    public void activityHasAPathway(String activityName, String pathwayName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(activityName, "id")));
                    assertNotNull(payload);
                    assertEquals(activityId, payload.getActivityId().toString());
                    assertNotNull(payload.getChildrenPathways());
                    assertEquals(1, payload.getChildrenPathways().size());
                    context.setVariable(nameFrom(pathwayName, "id"), payload.getChildrenPathways().get(0).toString());
                },
                "activity",
                "author.activity.get.ok"
        )));
    }

    @Then("^\"([^\"]*)\" activity has a \"([^\"]*)\" component$")
    public void activityHasAComponent(String activityName, String componentName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(activityName, "id")));
                    assertNotNull(payload);
                    assertEquals(activityId, payload.getActivityId().toString());
                    assertNotNull(payload.getComponents());
                    assertEquals(1, payload.getComponents().size());
                    context.setVariable(nameFrom(componentName, "id"), payload.getComponents().get(0).toString());
                },
                "activity",
                "author.activity.get.ok"
        )));
    }

    @Then("^\"([^\"]*)\" can not create an activity for \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotCreateAnActivityDueToTheAuthorizationError(String accountName, String pathwayName, int code, String errorMessage) throws Throwable {
        createsAActivityForThePathway(accountName, "", pathwayName);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.activity.create.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + errorMessage + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^\"([^\"]*)\" can create an activity for \"([^\"]*)\" pathway successfully")
    public void canCreateAnActivityForPathwaySuccessfully(String accountName, String pathwayName) throws Throwable {
        createsAActivityForThePathway(accountName, "", pathwayName);
        theActivityForThePathwayIsSuccessfullyCreated("", pathwayName);
    }

    @Then("^the activity theme replacing fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theActivityThemeReplacingFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.activity.theme.replace.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^\"([^\"]*)\" can not save config for \"([^\"]*)\" activity due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSaveConfigForActivityDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        savesConfigurationForTheActivity(accountName, "{\"foo\":\"bar\"}", activityName);
        theConfigurationReplacingFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can save config for \"([^\"]*)\" activity successfully$")
    public void canSaveConfigForActivitySuccessfully(String accountName, String activityName) {
        savesConfigurationForTheActivity(accountName, "{\"foo\":\"bar\"}", activityName);
        theConfigurationIsSuccessfullySaved();
    }

    @Then("^\"([^\"]*)\" can not save theme for \"([^\"]*)\" activity due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotSaveThemeForActivityDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        replaceTheActivityThemeWith(activityName, "some theme");
        theActivityThemeReplacingFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can save theme for \"([^\"]*)\" activity successfully$")
    public void canSaveThemeForActivitySuccessfully(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);
        replaceTheActivityThemeWith(activityName, "some theme");
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.activity.theme.replace.ok"));
    }

    @Then("^\"([^\"]*)\" can not delete \"([^\"]*)\" activity for \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDeleteActivityForPathwayDueToErrorCodeMessage(String accountName, String activityName, String pathwayName, int code, String message) {
        deletesTheActivityForThePathway(accountName, activityName, pathwayName);
        theActivityDeletingFailsWithMessageAndCode(message, code);
    }

    @Then("^\"([^\"]*)\" can delete \"([^\"]*)\" activity for \"([^\"]*)\" pathway successfully$")
    public void canDeleteActivityForPathwaySuccessfully(String accountName, String activityName, String pathwayName) {
        deletesTheActivityForThePathway(accountName, activityName, pathwayName);
        theActivityIsDeleted(activityName);
    }

    @Then("^\"([^\"]*)\" can not fetch \"([^\"]*)\" activity due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotFetchActivityDueToErrorCodeMessage(String accountName, String activityName, int code, String message) {
        fetchesTheActivity(accountName, activityName);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"author.activity.get.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^\"([^\"]*)\" can not fetch deleted \"([^\"]*)\" activity$")
    public void canNotFetchDeletedActivity(String accountName, String activityName) {
        fetchesTheActivity(accountName, activityName);
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                               "\"type\":\"author.activity.get.error\"," +
                               "\"code\": 404," +
                               "\"message\":\"Activity not found\"}"));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" activity successfully$")
    public void canFetchActivitySuccessfully(String accountName, String activityName) {
        fetchesTheActivity(accountName, activityName);
        //todo can be refactored later to use some theActivityIsCreatedSuccessfully method
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "author.activity.get.ok"));
    }

    @When("^\"([^\"]*)\" has updated the config for activity \"([^\"]*)\" with$")
    public void updatesTheConfigForActivityWith(String user, String activityName, String config) {

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.config.replace")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("config", config)
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.activity.config.replace.ok")
                        .jsonPath("$.response.activity.config", config)
                        .jsonPath("$.response.activity.updatedAt", "@notEmpty()@"));
    }

    @When("^\"([^\"]*)\" has updated the theme config for activity \"([^\"]*)\" with$")
    public void hasUpdatedTheThemeConfigForActivityWith(String user, String activityName, String config) {
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "author.activity.theme.replace")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("config", config)
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "author.activity.theme.replace.ok")
                        .jsonPath("$.response.activityTheme.config", config));
    }

    @When("^\"([^\"]*)\" has deleted the \"([^\"]*)\" activity for pathway \"([^\"]*)\"$")
    public void hasDeletedTheActivityForPathway(String user, String activityName, String pathwayName) {
        deletesTheActivityForThePathway(user, activityName, pathwayName);
        theActivityIsDeleted(activityName);
    }

    @And("^\"([^\"]*)\" has saved config for \"([^\"]*)\" activity with references to \"([^\"]*)\"$")
    public void hasSavedConfigForActivityWithReferencesTo(String user, String activityName, String list) {
        String config = replaceByIds(runner, list);
        savesConfigurationForTheActivity(user, config, activityName);
        theConfigurationIsSuccessfullySaved();
    }

    @Then("^\"([^\"]*)\" ([^\"]*) config has no references to \"([^\"]*)\"$")
    public void activityConfigHasNoReferencesTo(String name, String elementType, String list) {
        String expectedConfig = replaceByIds(runner, list);
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage payload, Map headers, TestContext context) {
                String actualConfig = (String) ((Map) payload.getResponse().get(elementType)).get("config");
                assertNotEquals(expectedConfig, actualConfig);
            }
        }));
    }

    @Then("^\"([^\"]*)\" ([^\"]*) config has references to \"([^\"]*)\"$")
    public void activityConfigHasReferencesTo(String name, String elementType, String list) {
        String expectedConfig = replaceByIds(runner, list);
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage payload, Map headers, TestContext context) {
                String actualConfig = (String) ((Map) payload.getResponse().get(elementType)).get("config");
                assertEquals(expectedConfig, actualConfig);
            }
        }));
    }

    @Then("^the activity payload contains linked items$")
    public void theActivityPayloadContainsLinkedItems(List<String> expectedItemNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> {
                    List<DocumentItemPayload> items = payload.getLinkedDocumentItems();
                    Set<UUID> expectedItemsId = expectedItemNames
                            .stream()
                            .map(name -> UUID.fromString(context.getVariable(nameFrom(name, "id"))))
                            .collect(Collectors.toSet());
                    Set<UUID> actualItemsId = items
                            .stream()
                            .map(DocumentItemPayload::getId)
                            .collect(Collectors.toSet());

                    TestCase.assertEquals(expectedItemsId, actualItemsId);
                },
                "activity",
                "author.activity.get.ok"
        )));
    }

    @Then("^the activity payload does not contain linked items$")
    public void theActivityPayloadDoesNotContainLinkedItems() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.payload.response.activity.linkedDocumentItems.exists()", false));
    }

    @When("{string} move activity {string} to workspace {string}")
    public void moveActivityToWorkspace(String accountName, String activityName, String workspaceName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
        .addField("type", "workspace.activity.move")
        .addField("activityId", interpolate(nameFrom(activityName, "id")))
        .addField("workspaceId", interpolate(nameFrom(workspaceName, "workspace_id")))
                .build());

    }

    @Then("the list of activities is empty")
    public void theListOfActivitiesIsEmpty() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.activities", "[]"));
    }

    @SuppressWarnings("unchecked")
    @Then("the following activities are returned")
    public void theFollowingActivitiesAreReturned(List<String> expectedActivityNames) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                List<Map> activities = (List) response.get("activities");

                List<String> expectedActivityIds = expectedActivityNames.stream()
                        .map(expectedActivityName -> context.getVariable(interpolate(nameFrom(expectedActivityName, "id"))))
                        .collect(Collectors.toList());

                List<String> actualActivityIds = activities.stream()
                        .map(activity -> String.valueOf(activity.get("activityId")))
                        .collect(Collectors.toList());

                assertTrue(actualActivityIds.containsAll(expectedActivityIds));
            }
        }));
    }

    @When("{string} fetches the progress for the root activity {string} for cohort {string} deployment {string}")
    public void fetchesTheProgressForTheRootActivityForCohortDeployment(final String user,
                                                                        final String rootActivityName,
                                                                        final String cohortName,
                                                                        final String deploymentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, getProgressQuery(
                interpolate(nameFrom(cohortName, "id")),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(rootActivityName, "id"))
        ));
    }

    @Then("the root activity has completion")
    public void theRootActivityHasCompletion(Map<String, String> expectedCompletion) {

        final Float expectedValue = Float.valueOf(expectedCompletion.get("value"));
        final Float expectedConfidence = Float.valueOf(expectedCompletion.get("confidence"));

        String completionPath = "$.response.data.learn.cohort.deployment[0].activity.progress.completion";
        messageOperations.receiveJSON(runner, action -> action
                        .jsonPath(completionPath + ".value", expectedValue)
                        .jsonPath(completionPath + ".confidence", expectedConfidence));
    }

    @When("{string} creates activity {string} inside project {string}")
    public void createsActivityInsideProject(final String accountName, final String activityName, final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.create")
                .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                .addField("pluginVersion", "1.*")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @When("{string} creates activity {string} inside project {string} with pluginname {string}")
    public void createsActivityInsideProjectWithPluginName(final String accountName, final String activityName, final String projectName,String pluginName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.create")
                .addField("pluginId", interpolate(nameFrom("plugin_id", pluginName)))
                .addField("pluginVersion", "1.*")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @When("{string} creates activity {string} inside project {string} with id")
    public void createsActivityInsideProjectWithId(final String accountName, final String activityName, final String projectName) {
        ACTIVITY_ID_PREGENERATED = UUIDs.timeBased();
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.create")
                .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                .addField("pluginVersion", "1.*")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("activityId", ACTIVITY_ID_PREGENERATED)
                .build());
    }

    @When("{string} creates activity {string} inside project {string} with the same id")
    public void createsActivityInsideProjectWithSameId(final String accountName, final String activityName, final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.create")
                .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                .addField("pluginVersion", "1.*")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("activityId", ACTIVITY_ID_PREGENERATED)
                .build());
    }

    @Then("activity {string} is successfully created inside project {string} with provided id")
    public void activityIsSuccessfullyCreatedInsideProjectWithId(String activityName, String projectName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.activity.create.ok")
                .extractFromPayload("$.response.activity.activityId", ACTIVITY_ID_PREGENERATED.toString())
                .jsonPath("$.response.activity.plugin", "@notEmpty()@")
                .jsonPath("$.response.activity.creator", "@notEmpty()@")
                .jsonPath("$.response.activity.createdAt", "@notEmpty()@")
                .jsonPath("$.response.activity.studentScopeURN",  "@notEmpty()@")

        );
    }

    @Then("activity {string} is successfully created with provided id")
    public void activityIsSuccessfullyCreatedWithId(String activityName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.activity.create.ok")
                .extractFromPayload("$.response.activity.activityId", ACTIVITY_ID_PREGENERATED.toString())
                .jsonPath("$.response.activity.plugin", "@notEmpty()@")
                .jsonPath("$.response.activity.creator", "@notEmpty()@")
                .jsonPath("$.response.activity.createdAt", "@notEmpty()@")
                .jsonPath("$.response.activity.studentScopeURN",  "@notEmpty()@")

        );
    }

    @Then("the activity is not created due to conflict")
    public void theActivityIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("author.activity.create.error", payload.getType());
                assertEquals(409, payload.getCode().intValue());
                assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Then("the project activity is not created due to conflict")
    public void theProjectActivityIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("project.activity.create.error", payload.getType());
                assertEquals(409, payload.getCode().intValue());
                assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Then("activity {string} is successfully created inside project {string}")
    public void activityIsSuccessfullyCreatedInsideProject(String activityName, String projectName) {
        String activityIdVar = activityName == null ? ACTIVITY_ID_VAR : nameFrom(activityName, "id");
        messageOperations.receiveJSON(runner, action -> action
            .jsonPath("$.type", "project.activity.create.ok")
                .extractFromPayload("$.response.activity.activityId", activityIdVar)
                .jsonPath("$.response.activity.plugin", "@notEmpty()@")
                .jsonPath("$.response.activity.creator", "@notEmpty()@")
                .jsonPath("$.response.activity.createdAt", "@notEmpty()@")
                .extractFromPayload("$.response.activity.studentScopeURN",  nameFrom(activityName, "studentScope"))

        );
    }

    @And("{string} has created activity {string} inside project {string}")
    public void hasCreatedActivityInsideProject(final String accountName, final String activityName, final String projectName) {
        createsActivityInsideProject(accountName, activityName, projectName);
        activityIsSuccessfullyCreatedInsideProject(activityName, projectName);
    }
    @And("{string} has created activity {string} inside project {string} with plugninName {string}")
    public void hasCreatedActivityInsideProjectWithPluginName(final String accountName, final String activityName, final String projectName,String pluginName) {
        createsActivityInsideProjectWithPluginName(accountName, activityName, projectName,pluginName);
        activityIsSuccessfullyCreatedInsideProject(activityName, projectName);
    }

    @Then("{string} can list the following activities from project {string}")
    public void canListTheFollowingActivitiesFromProject(final String accountName, final String projectName, final List<String> expectedActivityNames) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.list")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList activities, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedActivityNames.size(), activities.size());
                        Set<String> actualActivities = new HashSet<>(activities.size());

                        for (Object activity : activities) {
                            actualActivities.add((String) ((Map) activity).get("activityId"));
                        }

                        Set<String> expectedActivities = expectedActivityNames.stream()
                                .map(activityName -> context.getVariable(nameFrom(activityName, "id")))
                                .collect(Collectors.toSet());

                        assertEquals(expectedActivities, actualActivities);
                    }

                    @Override
                    public String getRootElementName() {
                        return "activities";
                    }

                    @Override
                    public String getType() {
                        return "project.activity.list.ok";
                    }
                }));
    }

    @When("{string} deletes activity {string} from project {string}")
    public void deletesActivityFromProject(final String accountName, final String activityName, final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.delete")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .build());
    }

    @Then("activity {string} is successfully deleted from project {string}")
    public void activityIsSuccessfullyDeletedFromProject(final String activityName, final String projectName) {
        messageOperations.validateResponseType(runner, "project.activity.delete.ok");
    }

    @Then("activity {string} is not deleted from project {string}")
    public void activityIsNotDeletedFromProject(final String activityName, final String projectName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("project.activity.delete.error", payload.getType());
                assertEquals(404, payload.getCode().intValue());
                assertTrue(payload.getMessage().matches("Activity not found"));
                assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @And("{string} has created configuration for {string} with field name {string} and field value {string}")
    public void hasCreatedConfigurationForWithFieldNameAndFieldValue(String accountName, String activityName, String fieldName, String fieldValue) {
        savesConfigurationForTheActivity(accountName, "{\"" + fieldName + "\":\"" + fieldValue + "\"}", activityName);
        theConfigurationIsSuccessfullySaved();
    }

    @Then("{string} can list the following activities from project {string} with field {string} {string}")
    public void canListTheFollowingActivitiesFromProjectWithField(final String accountName, final String projectName,
                                                                  final String fieldName, final String fieldFound,
                                                                  final List<String> expectedActivityNames) {

        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "project.activity.list")
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("fieldNames", Arrays.asList(fieldName))
                .build());

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList activities, final Map<String, Object> headers, final TestContext context) {
                        assertEquals(expectedActivityNames.size(), activities.size());

                        Set<String> actualActivities = new HashSet<>(activities.size());
                        List<List> fieldNames = new ArrayList<>();
                        for (Object activity : activities) {
                            actualActivities.add((String) ((Map) activity).get("activityId"));
                            fieldNames.add((List) ((Map) activity).get("configFields"));
                        }
                        Set<String> expectedActivities = expectedActivityNames.stream()
                                .map(activityName -> context.getVariable(nameFrom(activityName, "id")))
                                .collect(Collectors.toSet());
                        Map configurationField = (Map) fieldNames.get(0).get(0);

                        assertEquals(expectedActivities, actualActivities);
                        if (fieldFound.toUpperCase().equals("FOUND")) {
                            assertNotNull(configurationField.get("fieldValue"));
                            assertEquals(fieldName, configurationField.get("fieldName"));
                        } else {
                            assertNull(configurationField.get("fieldValue"));
                            assertEquals(fieldName, configurationField.get("fieldName"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "activities";
                    }

                    @Override
                    public String getType() {
                        return "project.activity.list.ok";
                    }
                }));
    }

    @Then("the {string} activity includes the {string} description")
    public void theActivityIncludesTheDescription(String activityName, String description) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(activityResponseOk(
                (payload, context) -> {
                    String activityId = context.getVariable(interpolate(nameFrom(activityName, "id")));
                    assertEquals(activityId, payload.getActivityId().toString());
                    assertEquals(description, payload.getDescription());
                },
                "activity",
                "author.activity.get.ok"
        )));
    }

    @And("{string} has saved theme config for {string} activity successfully")
    public void hasSavedThemeConfigForActivitySuccessfully(String accountName, String activityName) {
        authenticationSteps.authenticateUser(accountName);

        replaceTheActivityThemeWith(activityName, "{\"foo\":\"bar\"}");
        theActivityThemeIsReplacedSuccessfully(activityName);
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @And("{string} has created an activity {string} for the {string} pathway with plugin {string} with version {string}")
    public void hasCreatedActivityForPluginOfType(String accountName,String activityName,String pathwayName, String pluginName, String pluginVersion) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, createChildActivity(
                Variables.interpolate(nameFrom("plugin_id", pluginName)),
                pluginVersion,
                Variables.interpolate(Variables.nameFrom(pathwayName, "id"))));
        String activityVar = activityName == null ? ACTIVITY_ID_VAR : nameFrom(activityName, "id");
        messageOperations.receiveJSON(runner, action ->
                action.payload(createChildActivityOkWithPluginName("author.activity.create.ok",
                                                     "@variable('" + activityVar + "')@",
                                                     "${" + interpolate(nameFrom("plugin_id", pluginName)) + "}",
                                                     "@notEmpty()@",
                                                     "@notEmpty()@",
                                                     Variables.interpolate(Variables.nameFrom(pathwayName, "id")), pluginName,pluginVersion))
                        .extractFromPayload("$.response.activity.studentScopeURN", nameFrom(activityName, "studentScope")));
    }

    @Then("the {string} activity is successfully fetched with theme {string} information")
    public void theActivityIsSuccessfullyFetchedWithThemeInformation(String elementName, String themeName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ActivityPayload activity, Map<String, Object> headers, TestContext context) {
                        assertActivity(activity, context, elementName);
                        assertActivityConfig(activity, context);
                        assertActivityTheme(activity, context);
                        assertNotNull(activity.getThemePayload());
                        assertNotNull(activity.getThemePayload().getId());
                        assertEquals(activity.getThemePayload().getId().toString(), context.getVariable(nameFrom(themeName, "id")));
                        assertNotNull(activity.getThemePayload().getName());
                        assertNotNull(activity.getThemePayload().getThemeVariants().get(0).getVariantName());
                        assertNotNull(activity.getThemePayload().getThemeVariants().get(0).getConfig());
                    }
                }));
    }

    @Then("the theme {string} information is deleted from unit {string}")
    public void theThemeInformationIsDeletedFromUnit(String themeName, String elementName) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void validate(ActivityPayload activity, Map<String, Object> headers, TestContext context) {
                        assertActivity(activity, context, elementName);
                        assertActivityConfig(activity, context);
                        assertActivityTheme(activity, context);
                        assertNotNull(activity.getThemePayload());
                        assertNull(activity.getThemePayload().getId());
                        assertNull(activity.getThemePayload().getThemeVariants());
                    }
                }));
    }

    @When("{string} saves {string} asset configuration for the {string} activity")
    public void savesAssetConfigurationForTheActivity(String accountName, String assetName, String activityName, String config) {
        authenticationSteps.authenticateUser(accountName);
        String assetUrn = interpolate(nameFrom(assetName, "urn"));

        if(config.contains("AssetURN")){
            config = config.replace("AssetURN", assetUrn);
        }

        runner.variable("activity_config", config);
        String escaped = StringEscapeUtils.escapeJava(config);
        messageOperations.sendJSON(runner, "{" +
                "    \"type\": \"author.activity.config.replace\"," +
                "    \"activityId\":\"" + interpolate(nameFrom(activityName, "id")) + "\"," +
                "    \"config\" : \"" + escaped + "\"" +
                "}");
    }


    @Then("the {string} activity has activity theme icon libraries")
    public void theActivityHasActivityThemeIconLibraries(String activityName) {
        fetchesTheActivity(null, activityName);
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(new ResponseMessageValidationCallback<ActivityPayload>(ActivityPayload.class) {
                    @Override
                    public void validate(ActivityPayload payload, Map<String, Object> headers, TestContext context) {
                        assertNotNull(payload);
                        assertNotNull(payload.getActivityThemeIconLibraries());
                        assertTrue(payload.getActivityThemeIconLibraries().size()>0);
                    }

                    @Override
                    public String getRootElementName() {
                        return "activity";
                    }

                    @Override
                    public String getType() {
                        return "author.activity.get.ok";
                    }
                }));
    }
}
