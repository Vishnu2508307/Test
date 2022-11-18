package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class LTIProviderCredentialSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @And("{string} creates lti provider credentials for plugin {string} with")
    public void createsLtiProviderCredentialsForPluginWith(final String accountName, final String pluginName,
                                                           final Map<String, String> args) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "lti.credentials.create")
                .addField("pluginId", interpolate(nameFrom(pluginName, "id")))
                .addField("key", args.get("key"))
                .addField("secret", args.get("secret"))
                .addField("whiteListedFields", Arrays.stream(args.get("whitelistFields")
                        .split(","))
                        .collect(Collectors.toSet()))
                .build()
        );
    }

    @Then("the lti provider credentials {string} are created for {string}")
    public void theLtiProviderCredentialsAreCreatedFor(String credentialsName, String pluginName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "lti.credentials.create.ok")
                .extractFromPayload("$.response.credentials.key", nameFrom(credentialsName, "key"))
                .extractFromPayload("$.response.credentials.secret", nameFrom(credentialsName, "secret"))
                .jsonPath("$.response.credentials.pluginId", interpolate(nameFrom(pluginName, "id")))
        );
    }

    @Given("{string} has created lti provider credentials {string} for plugin {string} with")
    public void hasCreatedLtiProviderCredentialsForPluginWith(final String accountName, final String credentialsName,
                                                              final String pluginName, final Map<String, String> args) {
        createsLtiProviderCredentialsForPluginWith(accountName, pluginName, args);
        theLtiProviderCredentialsAreCreatedFor(credentialsName, pluginName);
    }

    @When("{string} deletes lti credentials {string} for plugin {string}")
    public void deletesLtiCredentialsForPlugin(String accountName, String credentialsName, String pluginName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "lti.credentials.delete")
                .addField("key", interpolate(nameFrom(credentialsName, "key")))
                .addField("pluginId", interpolate(nameFrom(pluginName, "id")))
                .build());
    }

    @Then("the lti credentials are deleted successfully")
    public void theLtiCredentialsAreDeletedSuccessfully() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "lti.credentials.delete.ok")
        );
    }

    @Then("the lti provider credentials are not {string} due to missing permission level")
    public void theLtiProviderCredentialsAreNotDueToMissingPermissionLevel(String actionType) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "lti.credentials." + actionType + ".error")
                .jsonPath("$.code", 401)
        );
    }
}
