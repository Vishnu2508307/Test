package mercury.glue.step.team;

import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.team.TeamPermissionHelper;

public class TeamPermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" adds \"([^\"]*)\" to the team \"([^\"]*)\" as \"([^\"]*)\"$")
    public void addsToTheTeamWithPermissionAs(String user, String account, String teamName, String permission) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, TeamPermissionHelper.grantTeamPermissionRequest(
                Variables.interpolate(nameFrom(teamName, "id")),
                Variables.interpolate(getAccountIdVar(account)),
                permission
        ));
    }

    @Then("^\"([^\"]*)\" is successfully added to the team \"([^\"]*)\" as \"([^\"]*)\"$")
    public void isSuccessfullyAddedToTheTeamAs(String user, String teamName, String permission) {
        messageOperations.receiveJSON(runner, action ->
                action.payload(TeamPermissionHelper.grantTeamPermissionResponse(
                        user,
                        permission,
                        Variables.interpolate(nameFrom(teamName, "id")))));
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\"'s \"([^\"]*)\" permission$")
    public void revokesTeamPermission(String user, String account, String teamName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner,TeamPermissionHelper.revokeTeamPermissionRequest(
                Variables.interpolate(getAccountIdVar(account)),
                Variables.interpolate(nameFrom(teamName, "id"))
        ));
    }

    @Then("^the team permission is successfully revoked$")
    public void theTeamPermissionIsSuccessfullyRevoked() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.team.permission.revoke.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("^\"([^\"]*)\" has granted \"([^\"]*)\" with \"([^\"]*)\" permission over the team \"([^\"]*)\"$")
    public void hasGrantedWithPermissionOverTheTeam(String user, String account, String permission, String teamName) {
        addsToTheTeamWithPermission(user, account, permission, teamName);
        isSuccessfullyAddedToTheTeamWithPermisionAs(account, permission, teamName);
    }

    private void isSuccessfullyAddedToTheTeamWithPermisionAs(String user, String permission, String teamName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload(TeamPermissionHelper.grantTeamPermissionResponse(
                        user,
                        permission,
                        Variables.interpolate(nameFrom(teamName, "id")))));
    }

    private void addsToTheTeamWithPermission(String user, String account, String permission, String teamName) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, TeamPermissionHelper.grantTeamPermissionRequest(
                Variables.interpolate(nameFrom(teamName, "id")),
                Variables.interpolate(getAccountIdVar(account)),
                permission
        ));
    }

    @Then("^the team permission is not \"([^\"]*)\"ed$")
    public void theTeamPermissionIsNotEd(String permissionAction) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"iam.team.permission." + permissionAction + ".error\"," +
                        "\"code\":401," +
                        "\"message\":\"Unauthorized: Higher permission level required\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("^\"([^\"]*)\" adds \"([^\"]*)\" to the \"([^\"]*)\" team as \"([^\"]*)\"$")
    public void addsToTheTeamAs(String user, String account, String teamName, String permission) throws Throwable {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, TeamPermissionHelper.grantTeamPermissionRequest(
                Variables.interpolate(nameFrom(teamName, "id")),
                Variables.interpolate(getAccountIdVar(account)),
                permission
        ));
        messageOperations.receiveJSON(runner, action ->
                action.payload(TeamPermissionHelper.grantTeamPermissionResponse(
                        account,
                        permission,
                        Variables.interpolate(nameFrom(teamName, "id")))));
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\"'s \"([^\"]*)\" team permission$")
    public void revokesSTeamPermission(String user, String account, String teamName) throws Throwable {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner,TeamPermissionHelper.revokeTeamPermissionRequest(
                Variables.interpolate(getAccountIdVar(account)),
                Variables.interpolate(nameFrom(teamName, "id"))
        ));

        theTeamPermissionIsSuccessfullyRevoked();
    }
}
