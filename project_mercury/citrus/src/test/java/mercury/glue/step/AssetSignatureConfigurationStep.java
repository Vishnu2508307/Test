package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class AssetSignatureConfigurationStep {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static boolean superUserCreated = false;


    @When("{string} creates asset signature for host {string} path {string} with type {string} and config")
    public void createsAssetSignatureForHostPathWithTypeAndConfig(final String accountName, final String host,
                                                                  final String path, final String type, final String config) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "asset.signature.config.create")
                .addField("host", host)
                .addField("path", path)
                .addField("config", config)
                .addField("strategyType", type)
                .build());
    }

    @Then("the {string} asset signature configuration is successfully created")
    public void theAssetSignatureConfigurationIsSuccessfullyCreated(final String signatureName) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                final String type = (String) payload.get("type");
                assertEquals("asset.signature.config.create.ok", type);
                final Map response = (Map) payload.get("response");
                final Map assetSignature = (Map) response.get("assetSignature");
                assertNotNull(assetSignature);
                final String id = (String) assetSignature.get("id");
                final String host = (String) assetSignature.get("host");
                final String path = (String) assetSignature.get("path");

                runner.variable(nameFrom(signatureName, "id"), id);
                runner.variable(nameFrom(signatureName, "host"), host);
                runner.variable(nameFrom(signatureName, "path"), path != null ? path : "");

            }
        }));
    }

    @Given("{string} has created {string} asset signature for host {string} path {string} with type {string} and config")
    public void hasCreatedAssetSignatureForHostPathWithTypeAndConfig(final String accountName, final String signatureName,
                                                                     final String host, final String path,
                                                                     final String type, final String config) {
        createsAssetSignatureForHostPathWithTypeAndConfig(accountName, host, path, type, config);
        theAssetSignatureConfigurationIsSuccessfullyCreated(signatureName);
    }

    @When("{string} deletes {string} asset signature configuration")
    public void deletesAssetSignatureConfiguration(final String accountName, final String signatureName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "asset.signature.config.delete")
                .addField("host", interpolate(nameFrom(signatureName, "host")))
                .addField("path", interpolate(nameFrom(signatureName, "path")))
                .build());
    }

    @Then("the asset signature configuration is deleted")
    public void theAssetSignatureConfigurationIsDeleted() {
        messageOperations.validateResponseType(runner, "asset.signature.config.delete.ok");
    }

    @Then("the asset signature configuration is not {string} due to missing permission level")
    public void theAssetSignatureConfigurationIsNotDueToMissingPermissionLevel(String actionName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "asset.signature.config." + actionName + ".error")
                .jsonPath("$.code", 401)
        );
    }
}
