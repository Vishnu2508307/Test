package mercury.glue.step;

import static mercury.common.CitrusAssert.assertHttpStatusCode;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.DevKeySteps.DEFAULT_DEV_KEY;
import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.PluginSteps.DEFAULT_PLUGIN_NAME;
import static mercury.glue.step.PluginSteps.DEFAULT_PLUGIN_TYPE;
import static mercury.glue.step.PluginSteps.DEFAULT_PLUGIN_VERSION;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.glue.wiring.CitrusConfiguration.PLUGIN_FILES_ENDPOINT;
import static mercury.glue.wiring.CitrusConfiguration.PLUGIN_ZIP_ENDPOINT;
import static mercury.glue.wiring.CitrusConfiguration.REST_CLIENT;
import static mercury.helpers.plugin.PluginHelper.verifyManifest;
import static mercury.helpers.plugin.PluginHelper.verifyView;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.plugin.service.PublishedPlugin;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class PluginPublishSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @Autowired
    private DevKeySteps devKeySteps;

    /**
     * this is hack to initialize @CitrusResource Runner, so this step class can be injected with @Autowired annotation
     * to other steps classes
     */
    @Before
    public void initializeCitrusResources() {
    }

    @When("^\"([^\"]*)\" publishes the plugin$")
    public void publishesThePlugin(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPlugin(getVariableValue(PLUGIN_ID_VAR), DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, "", DEFAULT_PLUGIN_VERSION);
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" for invalid plugin$")
    public void publishesVersionForInvalidPlugin(String user, String pluginVersion) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPlugin(UUIDs.timeBased().toString(), DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" with invalid schema plugin$")
    public void publishesVersionForPluginWithInvalidSchema(String user, String pluginVersion) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPluginWithInvalidSchema(UUIDs.timeBased().toString(), DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" publishes version for \"([^\"]*)\" plugin with values$")
    public void publishesVersionForPluginWithValues(String user, String pluginName, Map<String, String> values) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        String pluginIdVar = nameFrom("plugin_id", pluginName);
        String pluginType = values.getOrDefault("type", DEFAULT_PLUGIN_TYPE);

        publishPlugin(getVariableValue(pluginIdVar), values.get("name"), pluginType, values.get("description"), values.get("version"));
    }

    public void publishesVersionForPluginWithValuesUpdateSchema(String user, String pluginName, Map<String, String> values) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        String pluginIdVar = nameFrom("plugin_id", pluginName);
        String pluginType = values.getOrDefault("type", DEFAULT_PLUGIN_TYPE);

        publishPluginWithUpdatedSchema(getVariableValue(pluginIdVar), values.get("name"), pluginType, values.get("description"), values.get("version"));
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" for \"([^\"]*)\" plugin without a manifest file$")
    public void publishesVersionForPluginWithoutAManifestFile(String user, String pluginVersion, String pluginName) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPluginWithNoManifest(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" for \"([^\"]*)\" plugin without an id specified$")
    public void publishesVersionForPluginWithoutAnId(String user, String pluginVersion, String pluginName) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPluginWithNoId(pluginName, DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" for \"([^\"]*)\" plugin with invalid dev key$")
    public void publishesVersionForPluginWithInvalidDevKey(String user, String pluginVersion, String pluginName) {
        runner.createVariable(DEFAULT_DEV_KEY, "11111");

        publishPlugin(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" publishes version \"([^\"]*)\" for \"([^\"]*)\" plugin$")
    public void publishesVersionForPlugin(String user, String pluginVersion, String pluginName) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPlugin(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                DEFAULT_PLUGIN_TYPE, "", pluginVersion);
    }

    @When("{string} publishes version {string} for {string} plugin of type {string}")
    public void publishesVersionForPluginOfType(String user, String pluginVersion, String pluginName, String pluginType) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        publishPlugin(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                      pluginType, "", pluginVersion);
    }

    @When("^\"([^\"]*)\" (?:can publish|has published) version for \"([^\"]*)\" plugin with values$")
    public void hasPublishedVersionForPluginWithValues(String user, String pluginName, Map<String, String> values) {
        publishesVersionForPluginWithValues(user, pluginName, values);
        validatePluginPublishOkResponse(values.get("name"), values.get("type"), values.get("description"));
    }

    public void hasPublishedVersionForPluginWithValuesUpdateSchema(String user, String pluginName, Map<String, String> values) {
        publishesVersionForPluginWithValuesUpdateSchema(user, pluginName, values);
        validatePluginPublishOkResponse(values.get("name"), values.get("type"), values.get("description"));
    }

    @When("^\"([^\"]*)\" has published version \"([^\"]*)\" for \"([^\"]*)\" plugin$")
    public void hasPublishedVersionForPlugin(String accountName, String version, String pluginName) {
        publishesVersionForPlugin(accountName, version, pluginName);
        validatePluginPublishOkResponse(pluginName, DEFAULT_PLUGIN_TYPE, "");
    }

    @Given("^\"([^\"]*)\" has published the plugin$")
    public void hasPublishedThePlugin(String accountName) {
        publishesThePlugin(accountName);
        validatePluginPublishOkResponse(DEFAULT_PLUGIN_NAME, DEFAULT_PLUGIN_TYPE, "");
    }

    @Then("^the \"([^\"]*)\" plugin is published and uploaded successfully with values$")
    public void theThePluginIsPublishedAndUploadedSuccessfullyWithValues(String pluginName, Map<String, String> values) {
        String pluginIdVar = nameFrom("plugin_id", pluginName);
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .validationCallback(new JsonMappingValidationCallback<PublishedPlugin>(PublishedPlugin.class) {
                    @Override
                    public void validate(PublishedPlugin plugin, Map<String, Object> headers, TestContext context) {
                        assertHttpStatusCode(HttpStatus.CREATED.value(), headers);
                        verifyManifest(plugin.getPluginManifest(), context, pluginIdVar, values.get("version"), values.getOrDefault("description", ""));
                        verifyView(plugin.getManifestView().get(0), context, pluginIdVar, values.get("version"));
                        verifyPluginFilesAreUploaded(plugin);
                    }
                }));
    }

    public void hasPublishedAPlugin(final String pluginName) {
        // provide the dev key
        devKeySteps.aValidDeveloperKeyIsProvided();
        // publish the plugin
        publishPlugin(getVariableValue(nameFrom(pluginName, "id")), pluginName,
                DEFAULT_PLUGIN_TYPE, "", "1.0.0");
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .validationCallback(new JsonMappingValidationCallback<PublishedPlugin>(PublishedPlugin.class) {
                    @Override
                    public void validate(PublishedPlugin plugin, Map<String, Object> headers, TestContext context) {
                        assertHttpStatusCode(HttpStatus.CREATED.value(), headers);
                        context.setVariable(nameFrom(pluginName, "id"), plugin.getPluginManifest().getPluginId());
                    }
                }));
    }

    @When("^\"([^\"]*)\" publishes version for \"([^\"]*)\" plugin with pluginId for \"([^\"]*)\" and values$")
    public void publishesVersionForPluginWithPluginIdForAndValues(String user, String pluginName,
                                                                  String pluginIdToPublishWith,
                                                                  Map<String, String> values) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        String newPluginId = getVariableValue(nameFrom("plugin_id", pluginIdToPublishWith));

        String pluginType = values.getOrDefault("type", DEFAULT_PLUGIN_TYPE);
        publishPlugin("plugin/plugin_success.zip",
                getVariableValue(nameFrom("plugin_id", pluginName)),
                pluginName,
                pluginType,
                values.getOrDefault("description", ""),
                values.get("version"), newPluginId);
    }

    public void publishPlugin(String pluginId, String pluginName, String pluginType,
                               String pluginDescription, String pluginVersion) {
        publishPlugin("plugin/plugin_success.zip", pluginId, pluginName, pluginType, pluginDescription,
                pluginVersion);
    }

    public void publishPluginWithUpdatedSchema(String pluginId, String pluginName, String pluginType,
                                               String pluginDescription, String pluginVersion) {
        publishPlugin("plugin/plugin_success_schema_update.zip", pluginId, pluginName, pluginType, pluginDescription,
                      pluginVersion);
    }

    private void publishPluginWithInvalidSchema(String pluginId, String pluginName, String pluginType,
                                                String pluginDescription, String pluginVersion) {
        publishPlugin("plugin/plugin_invalid_schema.zip", pluginId, pluginName, pluginType, pluginDescription,
                pluginVersion);
    }

    private void publishPluginWithNoManifest(String pluginId, String pluginName, String pluginType,
                                             String pluginDescription, String pluginVersion) {
        publishPlugin("plugin/plugin_no_manifest.zip", pluginId, pluginName, pluginType, pluginDescription,
                pluginVersion);
    }

    private void publishPluginWithNoId(String pluginName,
                                       String pluginType,
                                       String pluginDescription,
                                       String pluginVersion) {
        publishPlugin("plugin/plugin_no_id.zip", null, pluginName, pluginType, pluginDescription, pluginVersion);
    }

    private void publishPlugin(String fileName, String pluginId, String pluginName, String pluginType,
                               String pluginDescription, String pluginVersion) {
        publishPlugin(fileName, pluginId, pluginName, pluginType, pluginDescription, pluginVersion, null);
    }

    private void publishPlugin(String fileName, String pluginId, String pluginName, String pluginType,
                               String pluginDescription, String pluginVersion, String pluginIdToOverwrite) {
        Map<String, String> fields = new HashMap<>();
        if (pluginId != null) {
            fields.put("<insert_plugin_id>", pluginId);
        }
        fields.put("<insert_plugin_name>", pluginName);
        fields.put("<insert_plugin_type>", pluginType);
        fields.put("<insert_plugin_description>", pluginDescription);
        fields.put("<insert_plugin_version>", pluginVersion);

        try {
            Path tempPath = null;
            if (fileName.contains("package"))
                tempPath = ZipUtils.modifyPackageInZip(fileName, fields);
            else
                tempPath = ZipUtils.modifyManifestInZip(fileName, fields);

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>(2);
            map.set("devKey", getVariableValue(DEFAULT_DEV_KEY));
            map.set("file", new PathResource(tempPath));
            if (pluginIdToOverwrite != null) {
                map.set("pluginId", pluginIdToOverwrite);
            }

            runner.http(action -> action.client(REST_CLIENT)
                    .send().post("/plugin/publish/")
                    .contentType("multipart/form-data")
                    .payload(map));

            Files.delete(tempPath);
        } catch (IOException | URISyntaxException e) {
            throw new CitrusRuntimeException("plugin publishing failed", e);
        }
    }

    private void validatePluginPublishOkResponse(String pluginName, String pluginType, String pluginDescription) {
        runner.http(action -> action.client(REST_CLIENT)
                .receive()
                .response()
                .validationCallback(new JsonMappingValidationCallback<PublishedPlugin>(PublishedPlugin.class) {
                    @Override
                    public void validate(PublishedPlugin plugin, Map<String, Object> headers, TestContext context) {
                        assertAll(() -> {
                            assertHttpStatusCode(HttpStatus.CREATED.value(), headers);
                            assertEquals(pluginName, plugin.getPluginManifest().getName());
                            assertEquals(pluginType, plugin.getPluginManifest().getType().getLabel());
                        });
                    }
                }));

    }

    private void verifyPluginFilesAreUploaded(PublishedPlugin plugin) {
        String pluginId = plugin.getPluginManifest().getPluginId().toString();
        String zipHash = plugin.getPluginManifest().getZipHash();
        String filePath = buildPath(pluginId, zipHash);
        //verify zip
        verifyUploadedPluginFile(PLUGIN_ZIP_ENDPOINT,
                "/" + plugin.getPluginManifest().getPluginId() + "/" + plugin.getPluginManifest().getVersion() + "/" + zipHash + ".zip",
                "application/zip",
                "@notEmpty()@");
        //verify index.js
        verifyUploadedPluginFile(PLUGIN_FILES_ENDPOINT,
                filePath + "index.js",
                "application/x-javascript",
                "some random content\n");
        //verify screenshots
        verifyUploadedPluginFile(PLUGIN_FILES_ENDPOINT,
                filePath + "img/mercury.jpg",
                "image/jpeg",
                "@notEmpty()@");
        //verify thumbnail
        verifyUploadedPluginFile(PLUGIN_FILES_ENDPOINT,
                filePath + "img/thumbnail.png",
                "image/png",
                "@notEmpty()@");
    }

    private static String buildPath(String pluginId, String zipHash) {
        return "/" + pluginId + "/" + zipHash + "/";
    }

    private void verifyUploadedPluginFile(String endpoint, String actualFileUrl, String expectedContentType, String expectedPayload) {
        runner.http(action -> action.client(endpoint).send()
                .get(actualFileUrl));
        runner.http(action -> action.client(endpoint).receive().response()
                .payload(expectedPayload)
                .status(HttpStatus.OK)
                .contentType(expectedContentType));

    }

    /**
     * hack to get variable value from context. Used were we can't rely on Citrus for converting variableExpression into value
     */
    private String getVariableValue(String variableName) {
        return runner.variable(variableName, "${" + variableName + "}");
    }

    @When("{string} has published version {string} for {string} plugin with filename {string}")
    public void hasPublishedVersionForPluginFile(String user, String pluginVersion, String pluginName, String fileName) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();
        publishPluginWithFileName(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                DEFAULT_PLUGIN_TYPE, "", pluginVersion, "plugin/" + fileName);
        validatePluginPublishOkResponse(pluginName, DEFAULT_PLUGIN_TYPE, "");
    }

    private void publishPluginWithFileName(String pluginId, String pluginName, String pluginType,
                                           String pluginDescription, String pluginVersion, String fileName) {
        publishPlugin(fileName, pluginId, pluginName, pluginType, pluginDescription,
                pluginVersion);
    }

    @When("{string} publishes version {string} for {string} plugin with filename {string}")
    public void publishesVersionForPluginWithFileName(String user, String pluginVersion, String pluginName, String fileName) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();
        publishPluginWithFileName(getVariableValue(nameFrom("plugin_id", pluginName)), pluginName,
                DEFAULT_PLUGIN_TYPE, "", pluginVersion, "plugin/" + fileName);
    }

    @And("{string} has published version {string} for {string} plugin of type {string}")
    public void hasPublishedVersionForPluginOfType(String accountName, String version, String pluginName, String pluginType) {
        publishesVersionForPluginOfType(accountName, version, pluginName, pluginType);
        validatePluginPublishOkResponse(pluginName, pluginType, "");
    }

    public void hasPublishedVersionForeTextPluginWithValues(String user, String pluginName, Map<String, String> values) {
        publishesVersionForeTextPluginWithValues(user, pluginName, values);
        validatePluginPublishOkResponse(values.get("name"), values.get("type"), values.get("description"));
    }

    public void publishesVersionForeTextPluginWithValues(String user, String pluginName, Map<String, String> values) {
        authenticationSteps.authenticateUser(user);
        devKeySteps.aValidDeveloperKeyIsProvided();

        String pluginIdVar = nameFrom("plugin_id", pluginName);
        String pluginType = values.getOrDefault("type", DEFAULT_PLUGIN_TYPE);

        publisheTextPlugin(getVariableValue(pluginIdVar), values.get("name"), pluginType, values.get("description"), values.get("version"));
    }

    public void publisheTextPlugin(String pluginId, String pluginName, String pluginType,
                              String pluginDescription, String pluginVersion) {
        publishPlugin("plugin/eText_plugin_success.zip", pluginId, pluginName, pluginType, pluginDescription,
                      pluginVersion);
    }
}
