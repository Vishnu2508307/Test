package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.glue.wiring.CitrusConfiguration.PLUGIN_FILES_PUBLIC_URL;
import static mercury.helpers.plugin.PluginHelper.createPluginOkResponseAndExtractId;
import static mercury.helpers.plugin.PluginHelper.createPluginRequest;
import static mercury.helpers.plugin.PluginHelper.deletePluginVersionRequest;
import static mercury.helpers.plugin.PluginHelper.unpublishPluginVersionRequest;
import static mercury.helpers.plugin.PluginHelper.updatePluginRequest;
import static mercury.helpers.plugin.PluginHelper.getPluginAuthorRequest;
import static mercury.helpers.plugin.PluginHelper.getPluginWorkspaceRequest;
import static mercury.helpers.plugin.PluginHelper.listPluginVersionRequest;
import static mercury.helpers.plugin.PluginHelper.listPluginVersionResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.util.UUIDs;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.helpers.plugin.PluginHelper;

public class PluginSteps {

    @CitrusResource
    private TestRunner runner;

    public static final String DEFAULT_PLUGIN_NAME = "Citrus Plugin";
    public static final String DEFAULT_PLUGIN_TYPE = "component";
    public static final String DEFAULT_PLUGIN_VERSION = "1.2.0";

    @Autowired
    @Qualifier(PLUGIN_FILES_PUBLIC_URL)
    private String publicUrl;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Autowired
    private PluginPublishSteps pluginPublishSteps;

    /**
     * this is hack to initialize @CitrusResource Runner, so this step class can be injected with @Autowired annotation
     * to other steps classes
     */
    @Before
    public void initializeCitrusResources() {
    }

    /**
     * this is a hack to create a plugin needed for activity only once for all scenarios and reuse it
     */
    private static String coursePluginId = null;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "This could not be executed by multiple instances. May be improved later.")
    public void coursePluginShouldExist(String user) {
        if (coursePluginId == null) {
            hasCreatedAndPublishedPluginWithVersionAndType(user, "Course Citrus plugin", "1.2.0", "course");
            coursePluginId = getVariableValue(nameFrom("plugin_id", "Course Citrus plugin"));
        }
        runner.createVariable(PLUGIN_ID_VAR, coursePluginId);
    }

    @When("{string} creates a plugin with a supplied id")
    public void createsAPluginWithId(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        UUID pluginId = runner.variable("SUPPLIED_PLUGIN_ID", UUIDs.timeBased());

        messageOperations.sendJSON(runner,
                createPluginRequest(DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, pluginId, null));
    }

    @When("{string} creates a plugin with the same supplied id as before")
    public void createsAPluginWithSameId(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        UUID pluginId = UUIDs.fromString(getVariableValue("SUPPLIED_PLUGIN_ID"));
        messageOperations.sendJSON(runner, createPluginRequest(DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, pluginId, null));
    }

    @When("^\"([^\"]*)\" has created a plugin$")
    public void hasCreatedAPlugin(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, createPluginRequest(DEFAULT_PLUGIN_NAME, null));
        messageOperations.receiveJSON(runner,
                action -> action.payload(createPluginOkResponseAndExtractId(DEFAULT_PLUGIN_NAME, PLUGIN_ID_VAR, null, null)));
    }

    @Given("{string} has created a plugin with a supplied id")
    public void hasCreatedAPluginWithId(String accountName) {
        createsAPluginWithId(accountName);
        thePluginIsSuccessfullyCreatedWithId();
    }

    @And("^\"([^\"]*)\" has created and published the plugin$")
    public void hasCreatedAndPublishedThePlugin(String accountName) {
        hasCreatedAPlugin(accountName);
        pluginPublishSteps.hasPublishedThePlugin(accountName);
    }

    @Given("^\"([^\"]*)\" has created and published \"([^\"]*)\" plugin with version \"([^\"]*)\"$")
    public void hasCreatedAndPublishedPluginWithVersion(String user, String pluginName, String pluginVersion) {
        hasCreatedAPluginWithNameAndType(user, pluginName, null);
        pluginPublishSteps.hasPublishedVersionForPlugin(user, pluginVersion, pluginName);
    }

    @When("^\"([^\"]*)\" has created and published \"([^\"]*)\" plugin with version \"([^\"]*)\" and type \"([^\"]*)\"$")
    public void hasCreatedAndPublishedPluginWithVersionAndType(String user, String pluginName, String pluginVersion,
                                                               String pluginType) {
        hasCreatedAPluginWithNameAndType(user, pluginName, null);

        Map<String, String> values = new HashMap<>();
        values.put("name", pluginName);
        values.put("type", pluginType.toLowerCase());
        values.put("version", pluginVersion);
        values.put("description", "");

        pluginPublishSteps.hasPublishedVersionForPluginWithValues(user, pluginName, values);
    }

    @And("^\"([^\"]*)\" has created and published course plugin$")
    public void hasCreatedAndPublishedCoursePlugin(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        coursePluginShouldExist(accountName);
    }

    @Given("^\"([^\"]*)\" has changed the schema and published course plugin$")
    public void hasChangedPluginSchemaAndPublished(String user) {
        authenticationSteps.authenticateUser(user);

        hasCreatedAPluginWithNameAndType(user, "Course Citrus plugin", null);

        Map<String, String> values = new HashMap<>();
        values.put("name", "Course Citrus plugin");
        values.put("type", "course");
        values.put("version", "1.3.1");
        values.put("description", "");
        values.put("pluginId", coursePluginId);

        pluginPublishSteps.hasPublishedVersionForPluginWithValuesUpdateSchema(user, "Course Citrus plugin", values);
    }

    @Given("^\"([^\"]*)\" has created a plugin with name \"([^\"]*)\"(?: and type \"([^\"]*)\")?$")
    public void hasCreatedAPluginWithNameAndType(String user, String name, String type) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, createPluginRequest(name, type));
        messageOperations.receiveJSON(runner,
                action -> action.payload(createPluginOkResponseAndExtractId(name, nameFrom("plugin_id", name), type, null)));
    }

    @When("^\"([^\"]*)\" deletes a plugin$")
    public void deletesAPlugin(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.plugin.delete")
                .addField("pluginId", interpolate(PLUGIN_ID_VAR))
                .build());
    }

    @Then("the plugin is successfully created with the supplied id")
    public void thePluginIsSuccessfullyCreatedWithId() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "workspace.plugin.create.ok")
                .validate("$.response.plugin.pluginId", "${SUPPLIED_PLUGIN_ID}")
        );
    }


    @Then("^the plugin is successfully deleted$")
    public void thePluginIsSuccessfullyDeleted() {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "workspace.plugin.delete.ok"));
    }

    @When("^\"([^\"]*)\" has deleted a plugin$")
    public void hasDeletedAPlugin(String accountName) {
        deletesAPlugin(accountName);
        thePluginIsSuccessfullyDeleted();
    }

    @When("^\"([^\"]*)\" requests a plugin info(?: in workspace)?$")
    public void requestsAPluginInfo(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, getPluginWorkspaceRequest(interpolate(PLUGIN_ID_VAR)));
    }

    @Then("^\"([^\"]*)\" field is not empty$")
    public void fieldIsNotEmpty(String fieldName) {
        messageOperations.receiveJSON(runner, action ->
                action.validate("$.response.plugin.summary." + fieldName, "@notEmpty()@"));
    }

    @When("^\"([^\"]*)\" requests a plugin(?: in author)?$")
    public void requestsAPluginInAuthor(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, getPluginAuthorRequest(interpolate(PLUGIN_ID_VAR)));
    }

    @Then("^\"([^\"]*)\" can not delete the plugin$")
    public void canNotDeleteThePlugin(String accountName) {
        deletesAPlugin(accountName);
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "workspace.plugin.delete.error")
                        .validate("$.code", 401)
                        .validate("$.message", "Unauthorized: OWNER permission level required"));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" plugin summary in workspace$")
    public void canFetchPluginSummaryInWorkspace(String user, String pluginName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getPluginWorkspaceRequest(interpolate(nameFrom("plugin_id", pluginName))));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<PluginPayload>(PluginPayload.class) {
                    @Override
                    public String getType() {
                        return "workspace.plugin.get.ok";
                    }

                    @Override
                    public String getRootElementName() {
                        return "plugin";
                    }

                    @Override
                    public void validate(PluginPayload plugin, Map<String, Object> headers, TestContext context) {
                        PluginHelper.verifySummary(plugin.getPluginSummaryPayload(), context, pluginName, nameFrom("plugin_id", pluginName));
                    }
                }));
    }

    @Then("^\"([^\"]*)\" can fetch \"([^\"]*)\" plugin info in author with values$")
    public void canFetchPluginInfoForPluginInAuthorWithValues(String user, String pluginName, Map<String, String> values) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getPluginAuthorRequest(interpolate(pluginIdVar)));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<PluginPayload>(PluginPayload.class) {
                    @Override
                    public String getType() {
                        return "author.plugin.get.ok";
                    }

                    @Override
                    public String getRootElementName() {
                        return "plugin";
                    }

                    @Override
                    public void validate(PluginPayload plugin, Map<String, Object> headers, TestContext context) {
                        PluginHelper.verifySummary(plugin.getPluginSummaryPayload(), context, values.get("name"), pluginIdVar);
                        PluginHelper.verifyManifest(plugin.getManifest(), context, pluginIdVar, values.get("version"), values.get("description"));
                        PluginHelper.verifyView(plugin.getEntryPoints().get(0), context, pluginIdVar, values.get("version"));
                        verifyUploadedFiles(plugin);
                        Assert.assertNotNull(plugin.getPluginRepositoryPath());
                    }
                }));
    }

    private void verifyUploadedFiles(PluginPayload plugin) {
        String fullPath = buildFullPath(plugin.getManifest().getPluginId().toString(), plugin.getManifest().getZipHash());
        assertEquals(fullPath + "index.js", plugin.getEntryPoints().get(0).getEntryPointPath());
        assertEquals(fullPath + "img/mercury.jpg", plugin.getManifest().getScreenshots().toArray()[0]);
        assertEquals(fullPath + "img/thumbnail.png", plugin.getManifest().getThumbnail());
    }

    private String buildFullPath(String pluginId, String zipHash) {
        return publicUrl + "/" + pluginId + "/" + zipHash + "/";
    }

    @Then("^\"([^\"]*)\" can list \"([^\"]*)\" plugin versions$")
    public void canListPluginVersions(String user, String pluginName, List<String> expectedVersions) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, listPluginVersionRequest(interpolate(nameFrom("plugin_id", pluginName))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(listPluginVersionResponse(expectedVersions)));
    }

    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @Then("the plugin is not created due to conflict")
    public void thePluginIsNotCreatedDueToConflict() {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<ErrorMessage>(ErrorMessage.class) {
            @Override
            public void validate(ErrorMessage payload, Map<String, Object> headers, TestContext context) {
                assertEquals("workspace.plugin.create.error", payload.getType());
                assertEquals(409, payload.getCode().intValue());
                assertNotNull(payload.getReplyTo());
            }
        }));
    }

    @Given("{string} has created and published {string} plugin with version {string} from package.json file")
    public void hasCreatedAndPublishedPluginWithVersionFromPackageJsonFile(String user, String pluginName, String pluginVersion) {
        hasCreatedAPluginWithNameAndType(user, pluginName, null);
    }

    @Then("{string} can fetch {string} plugin info in author with following field values")
    public void canFetchPluginInfoInAuthorWithFollowingValues(String user, String pluginName, Map<String, String> values) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getPluginAuthorRequest(interpolate(pluginIdVar)));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<PluginPayload>(PluginPayload.class) {
                    @Override
                    public String getType() {
                        return "author.plugin.get.ok";
                    }

                    @Override
                    public String getRootElementName() {
                        return "plugin";
                    }

                    @Override
                    public void validate(PluginPayload plugin, Map<String, Object> headers, TestContext context) {
                        Assert.assertEquals(context.getVariable(pluginIdVar), plugin.getPluginSummaryPayload().getPluginId().toString());
                        Assert.assertEquals(values.get("name"), plugin.getPluginSummaryPayload().getName());
                        Assert.assertTrue(plugin.getPluginSummaryPayload().getLatestGuide().contains(values.get("guide")));
                        Assert.assertTrue(plugin.getPluginSummaryPayload().getDefaultHeight().contains(values.get("defaultHeight")));
                        Assert.assertNotNull(plugin.getPluginRepositoryPath());
                    }
                }));
    }

    @When("{string} has created and published plugin {string}")
    public void createsAPlugin(String accountName, String pluginName) {
        // authenticate the user
        authenticationSteps.authenticateUser(accountName);

        // create the plugin
        messageOperations.sendJSON(runner, createPluginRequest(DEFAULT_PLUGIN_NAME, null));
        messageOperations.receiveJSON(runner,
                action -> action.payload(createPluginOkResponseAndExtractId(DEFAULT_PLUGIN_NAME, nameFrom(pluginName, "id"), null, null)));

        // publish the plugin
        pluginPublishSteps.hasPublishedAPlugin(pluginName);
    }


    @Then("{string} can fetch {string} plugin info with following field values")
    public void canFetchPluginInfoWithFollowingValues(String user, String pluginName, Map<String, String> values) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, getPluginAuthorRequest(interpolate(pluginIdVar)));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<PluginPayload>(PluginPayload.class) {
                    @Override
                    public String getType() {
                        return "author.plugin.get.ok";
                    }

                    @Override
                    public String getRootElementName() {
                        return "plugin";
                    }

                    @Override
                    public void validate(PluginPayload plugin, Map<String, Object> headers, TestContext context) {
                        Assert.assertEquals(context.getVariable(pluginIdVar), plugin.getPluginSummaryPayload().getPluginId().toString());
                        Assert.assertEquals(values.get("name"), plugin.getPluginSummaryPayload().getName());
                        Assert.assertNotNull(plugin.getPluginRepositoryPath());
                    }
                }));
    }

    @Given("{string} has created and published {string} plugin with version {string} in {string} mode")
    public void hasCreatedAndPublishedPluginWithVersionInMode(String user, String pluginName, String pluginVersion, String publishMode) {

        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, createPluginRequest(pluginName, null, null, publishMode));
        messageOperations.receiveJSON(runner,
            action -> action.payload(createPluginOkResponseAndExtractId(pluginName, nameFrom("plugin_id", pluginName), null, publishMode)));
        pluginPublishSteps.hasPublishedVersionForPlugin(user, pluginVersion, pluginName);
    }

    @When("{string} updates publish mode to {string} and publishes {string} plugin with version {string}")
    public void updatesPublishModeToAndPublishesPluginWithVersion(String user, String publishMode, String pluginName, String pluginVersion) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, updatePluginRequest(interpolate(pluginIdVar),publishMode));
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.plugin.update.ok")
                .extractFromPayload("$.response.pluginSummary.id", nameFrom(pluginName, "id"))
                .extractFromPayload("$.response.pluginSummary.publishMode", publishMode));
        pluginPublishSteps.hasPublishedVersionForPlugin(user, pluginVersion, pluginName);
    }

    @When("{string} tries to update publish mode to {string} and publishes {string} plugin with version {string}")
    public void triesToUpdatePublishModeToAndPublishesPluginWithVersion(String user,
                                                                        String publishMode,
                                                                        String pluginName,
                                                                        String pluginversion) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, updatePluginRequest(interpolate(pluginIdVar),publishMode));
    }

    @Then("the update plugin fails due to invalid permission level")
    public void theUpdatePluginFailsDueToInvalidPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"workspace.plugin.update.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the plugin version unpublish fails due to invalid permission level")
    public void thePluginVersionUnpublishFailsDueToInvalidPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.plugin.version.unpublish.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} unpublishes version {string} for {string} plugin")
    public void unpublishesVersionForPlugin(String accountName, String version, String pluginName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, unpublishPluginVersionRequest(interpolate(nameFrom("plugin_id", pluginName)), version));

    }

    @Then("the {string} plugin version unpublished successfully")
    public void unpublishPluginVersion(String pluginName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.plugin.version.unpublish.ok")
                .extractFromPayload("$.response.pluginSummary.id", nameFrom(pluginName, "id")));
    }

    @Then("the {string} plugin version unpublished successfully to latest version {string}")
    public void unpublishPluginVersionToLatestVersion(String pluginName, String version) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.plugin.version.unpublish.ok")
                .extractFromPayload("$.response.pluginSummary.id", nameFrom(pluginName, "id"))
                .extractFromPayload("$.response.pluginSummary.latestVersion", version));
    }

    @And("{string} has unpublished version {string} for {string} plugin")
    public void hasUnpublishedVersionForPlugin(String accountName, String version, String pluginName) {
        unpublishesVersionForPlugin(accountName, version, pluginName);
        unpublishPluginVersion(pluginName);
    }

    @When("{string} tries to delete version {string} for {string} plugin")
    public void triesToDeleteVersionForPlugin(String accountName, String version, String pluginName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, deletePluginVersionRequest(interpolate(nameFrom("plugin_id", pluginName)), version));
    }

    @Then("the plugin version delete fails due to invalid permission level")
    public void thePluginVersionDeleteFailsDueToInvalidPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.plugin.version.delete.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the plugin version delete fails with message {string} and code {string}")
    public void thePluginVersionDeleteFailsWithMessageAndCode(String message, String code) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"author.plugin.version.delete.error\"," +
                                       "\"code\":" + code + "," +
                                       "\"message\":\"" + message + "\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the plugin version deleted successfully")
    public void thePluginVersionDeletedSuccessfully() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.plugin.version.delete.ok"));
    }

    @Given("{string} has created and published {string} plugin of type {string} with version {string}")
    public void hasCreatedAndPublishedPluginOfTypeWithVersion(String user, String pluginName, String pluginType, String pluginVersion) {
        hasCreatedAPluginWithNameAndType(user, pluginName, pluginType);
        pluginPublishSteps.hasPublishedVersionForPluginOfType(user, pluginVersion, pluginName, pluginType);
    }

    @When("^\"([^\"]*)\" has created and published eText \"([^\"]*)\" plugin with version \"([^\"]*)\" and type \"([^\"]*)\"$")
    public void hasCreatedAndPublishedeTextPluginWithVersionAndType(String user, String pluginName, String pluginVersion,
                                                               String pluginType) {
        hasCreatedAPluginWithNameAndType(user, pluginName, null);

        Map<String, String> values = new HashMap<>();
        values.put("name", pluginName);
        values.put("type", pluginType.toLowerCase());
        values.put("version", pluginVersion);
        values.put("description", "");

        pluginPublishSteps.hasPublishedVersionForeTextPluginWithValues(user, pluginName, values);
    }
}
