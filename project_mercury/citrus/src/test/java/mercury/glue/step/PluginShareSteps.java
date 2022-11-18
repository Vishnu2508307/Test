package mercury.glue.step;

import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;

public class PluginShareSteps {

    public static final String PLUGIN_ID_VAR = "plugin_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @When("^\"([^\"]*)\" shares this plugin with \"([^\"]*)\"$")
    public void sharesThisPluginWith(String ownerAccountName, String accountName) {
        sharesThisPluginWithAs(ownerAccountName, accountName, "CONTRIBUTOR");
    }

    @When("^\"([^\"]*)\" shares this plugin with \"([^\"]*)\" team$")
    public void sharesThisPluginWithTeam(String ownerAccountName, String teamName) {
        hasSharedAPluginWithTeamAs(ownerAccountName, teamName, "CONTRIBUTOR");
    }

    @Then("^the plugin is not shared due to missing permission level$")
    public void thePluginIsNotSharedDueToMissingPermissionLevel() {
        String type = "workspace.plugin.permission.grant.error";
        String error = "@notEmpty()@";
        validateUnauthorizedPermission(type, error);
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\"'s plugin permission$")
    public void revokesSPluginPermission(String userOne, String userTwo) {
        authenticationSteps.authenticateUser(userOne);
        loggedInUserRevokesPermissionTo(userTwo);
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.revoke.ok");
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\" team's plugin permission$")
    public void revokesTeamSPluginPermission(String accountName, String teamName) {
        authenticationSteps.authenticateUser(accountName);
        loggedInUserRevokesTeamPermission(teamName);
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.revoke.ok");
    }

    @Then("^\"([^\"]*)\" cannot share the plugin with \"([^\"]*)\"$")
    public void cannotShareThePluginWith(String userOne, String userTwo) {
        triesSharingThisPluginWithAs(userOne, userTwo, "REVIEWER");
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.error");

    }

    @Then("^\"([^\"]*)\" can share this plugin with \"([^\"]*)\"$")
    public void canShareThisPluginWith(String userOne, String userTwo) {
        triesSharingThisPluginWithAs(userOne, userTwo, "CONTRIBUTOR");
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }

    @When("^\"([^\"]*)\" shares this plugin with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void sharesThisPluginWithAs(String userOne, String userTwo, String permissionLevel) {
        authenticationSteps.authenticateUser(userOne);

        loggedInUserSharesPluginWith(PLUGIN_ID_VAR, userTwo, permissionLevel);
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }

    @When("^\"([^\"]*)\" tries sharing this plugin with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void triesSharingThisPluginWithAs(String userOne, String userTwo, String permissionLevel) {
        authenticationSteps.authenticateUser(userOne);
        loggedInUserSharesPluginWith(PLUGIN_ID_VAR, userTwo, permissionLevel);
    }

    @When("^\"([^\"]*)\" tries sharing this plugin with \"([^\"]*)\" team as \"([^\"]*)\"$")
    public void triesSharingThisPluginWitTeamAs(String loggedUser, String teamName, String permissionLevel) {
        authenticationSteps.authenticateUser(loggedUser);
        loggedInUserSharesPluginWithTeam(teamName, permissionLevel, Variables.interpolate(PLUGIN_ID_VAR));
    }

    @And("^\"([^\"]*)\" has shared a plugin with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void hasSharedAPluginWithAs(String userOne, String userTwo, String permissionLevel) {
        sharesThisPluginWithAs(userOne, userTwo, permissionLevel);
    }

    @And("^\"([^\"]*)\" has shared a plugin with \"([^\"]*)\" team as \"([^\"]*)\"$")
    public void hasSharedAPluginWithTeamAs(String loggedUser, String teamName, String permissionLevel) {
        authenticationSteps.authenticateUser(loggedUser);

        loggedInUserSharesPluginWithTeam(teamName, permissionLevel, Variables.interpolate(PLUGIN_ID_VAR));
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }

    @When("^\"([^\"]*)\" tries revoking \"([^\"]*)\"'s permission$")
    public void triesRevokingSPermission(String userOne, String userTwo) {
        authenticationSteps.authenticateUser(userOne);
        loggedInUserRevokesPermissionTo(userTwo);
    }

    @Then("^the permission is not revoked due to unauthorized permission level$")
    public void thePermissionIsNotRevokedDueToUnauthorizedPermissionLevel() {
        String type = "workspace.plugin.permission.revoke.error";
        String error = "Unauthorized: Higher permission level required";
        validateUnauthorizedPermission(type, error);
    }

    private void loggedInUserSharesPluginWith(String pluginIdVar, String accountName, String permissionLevel) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.permission.grant")
                .addField("accountId", Variables.interpolate(getAccountIdVar(accountName)))
                .addField("pluginId", Variables.interpolate(pluginIdVar))
                .addField("permissionLevel", permissionLevel);
        messageOperations.sendJSON(runner, payload.build());
    }

    private void loggedInUserSharesPluginWithTeam(String teamName, String permissionLevel, String pluginId) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.permission.grant")
                .addField("teamId", Variables.interpolate(Variables.nameFrom(teamName, "id")))
                .addField("pluginId", pluginId)
                .addField("permissionLevel", permissionLevel);
        messageOperations.sendJSON(runner, payload.build());
    }

    private void loggedInUserRevokesPermissionTo(String userTwo) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.permission.revoke")
                .addField("accountId", Variables.interpolate(getAccountIdVar(userTwo)))
                .addField("pluginId", Variables.interpolate(PLUGIN_ID_VAR));
        messageOperations.sendJSON(runner, payload.build());
    }

    private void loggedInUserRevokesTeamPermission(String teamName) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.permission.revoke")
                .addField("teamId", Variables.interpolate(Variables.nameFrom(teamName, "id")))
                .addField("pluginId", Variables.interpolate(PLUGIN_ID_VAR));
        messageOperations.sendJSON(runner, payload.build());
    }

    private void validateUnauthorizedPermission(String type, String message) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", type)
                .jsonPath("$.code", 401)
                .jsonPath("$.message", message));
    }

    @Then("^\"([^\"]*)\" can revoke \"([^\"]*)\"'s permission$")
    public void canRevokeSPermission(String userOne, String userTwo) {
        triesRevokingSPermission(userOne, userTwo);
        String type = "workspace.plugin.permission.revoke.ok";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", type));
    }

    @Then("^the plugin permission is successfully revoked$")
    public void thePluginPermissionIsSuccessfullyRevoked() {
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.revoke.ok");
    }

    @When("^\"([^\"]*)\" tries revoking \"([^\"]*)\" team's permission$")
    public void triesRevokingTeamSPermission(String loggedUser, String teamName) {
        authenticationSteps.authenticateUser(loggedUser);
        loggedInUserRevokesTeamPermission(teamName);
    }

    @And("^\"([^\"]*)\" has shared plugin \"([^\"]*)\" with team \"([^\"]*)\" as \"([^\"]*)\"$")
    public void hasSharedPluginWithTeamAs(String user, String pluginName, String teamName, String permission) {
        authenticationSteps.authenticateUser(user);
        loggedInUserSharesPluginWithTeam(teamName, permission,
                Variables.interpolate(Variables.nameFrom("plugin_id", pluginName)));
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }

    @When("^\"([^\"]*)\" has shared \"([^\"]*)\" plugin with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void hasSharedPluginWithAs(String user, String pluginName, String accountName, String permissionLevel) {
        authenticationSteps.authenticateUser(user);
        loggedInUserSharesPluginWith(nameFrom("plugin_id", pluginName), accountName, permissionLevel);
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }

    @And("{string} shares {string} plugin with {string} as {string}")
    public void sharesPluginWithAs(String userOne, String pluginName, String userTwo, String permissionLevel) {
        authenticationSteps.authenticateUser(userOne);

        loggedInUserSharesPluginWith(nameFrom("plugin_id", pluginName), userTwo, permissionLevel);
        messageOperations.validateResponseType(runner, "workspace.plugin.permission.grant.ok");
    }
}
