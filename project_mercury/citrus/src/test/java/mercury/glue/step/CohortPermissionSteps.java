package mercury.glue.step;

import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.Variables;
import mercury.helpers.cohort.CohortPermissionHelper;

public class CohortPermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @Given("^\"([^\"]*)\" grants \"([^\"]*)\" permission to \"([^\"]*)\" over the cohort$")
    public void grantsPermissionToOverTheCohort(String user, String permissionLevel, String toAnotherUser) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, CohortPermissionHelper.createCohortPermissionGrantMessage(
                Variables.interpolate("cohort_id"),
                permissionLevel.toUpperCase(),
                Variables.interpolate(getAccountIdVar(toAnotherUser))
        ));
    }

    @Given("^\"([^\"]*)\" has shared the created cohort with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void hasSharedTheCreatedCohortWithAs(String user, String collaborator, String permissionLevel) {
        grantsPermissionToOverTheCohort(user, permissionLevel, collaborator);
        hasPermissionOverTheCohort(collaborator, permissionLevel);

    }

    @Then("^\"([^\"]*)\" has \"([^\"]*)\" permission over the cohort$")
    public void hasPermissionOverTheCohort(String user, String permissionLevel) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortPermissionHelper.getCohortGrantPermissionSuccessResponse(
                        Variables.interpolate("cohort_id"),
                        permissionLevel,
                        Variables.interpolate(getAccountIdVar(user))
                )));
    }

    @Given("^\"([^\"]*)\" has granted \"([^\"]*)\" permission to \"([^\"]*)\" over the cohort$")
    public void hasGrantedPermissionToOverTheCohort(String user, String permissionLevel, String toAnotherUser) {
        grantsPermissionToOverTheCohort(user, permissionLevel, toAnotherUser);
        hasPermissionOverTheCohort(toAnotherUser, permissionLevel);
    }

    @Then("^the permission is not granted$")
    public void thePermissionIsNotGranted() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortPermissionHelper.getCohortPermissionErrorResponse("grant")
        ));
    }

    @When("^\"([^\"]*)\" revokes \"([^\"]*)\"'s permission$")
    public void revokesSPermission(String user, String anotherUser) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, CohortPermissionHelper.createCohortPermissionRevokeMessage(
                Variables.interpolate("cohort_id"),
                Variables.interpolate(getAccountIdVar(anotherUser))
        ));
    }

    @When("^\"([^\"]*)\" has revoked \"([^\"]*)\"'s permission$")
    public void hasRevokedSPermission(String user, String anotherUser) {
        revokesSPermission(user, anotherUser);
        thePermissionIsSuccessfullyRevoked();
    }

    @Then("^the permission is successfully revoked$")
    public void thePermissionIsSuccessfullyRevoked() {
        messageOperations.validateResponseType(runner, "workspace.cohort.permission.revoke.ok");
    }

    @Then("^the permission is not revoked$")
    public void thePermissionIsNotRevoked() {
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortPermissionHelper.getCohortPermissionErrorResponse("revoke")
        ));
    }

    @Given("^\"([^\"]*)\" has revoked \"([^\"]*)\"'s permission over the created cohort$")
    public void hasRevokedSPermissionOverTheCreatedCohort(String user, String anotherUser) throws Throwable {
        revokesSPermission(user, anotherUser);
        thePermissionIsSuccessfullyRevoked();
    }

    @Given("^\"([^\"]*)\" grants \"([^\"]*)\" permission over the cohort to the users$")
    public void grantsPermissionOverTheCohortToTheUsers(String accountName, String permissionLevel, List<String> accounts) {
        authenticationSteps.authenticateUser(accountName);

        List<String> accountIds = accounts.stream()
                .map(accName -> Variables.interpolate(getAccountIdVar(accName)))
                .collect(Collectors.toList());
        messageOperations.sendJSON(runner, CohortPermissionHelper.createCohortPermissionGrantMessage(
                Variables.interpolate("cohort_id"),
                permissionLevel.toUpperCase(),
                accountIds.toArray(new String[]{})
        ));
    }

    @Then("^cohort \"([^\"]*)\" permissions successfully given to$")
    public void cohortPermissionsSuccessfullyGivenTo(String permissionLevel, List<String> accounts) {
        List<String> accountIds = accounts.stream()
                .map(accName -> Variables.interpolate(getAccountIdVar(accName)))
                .collect(Collectors.toList());
        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortPermissionHelper.getCohortGrantPermissionSuccessResponse(
                        Variables.interpolate("cohort_id"),
                        permissionLevel,
                        accountIds.toArray(new String[]{})
                )));
    }

    @And("^\"([^\"]*)\" has granted \"([^\"]*)\" permission to the \"([^\"]*)\" team over the cohort$")
    public void hasGrantedPermissionToTheTeamOverTheCohort(String accountName, String permissionLevel, String teamName) {
        hasGrantedPermissionToTheTeamsOverTheCohort(accountName, permissionLevel, Lists.newArrayList(teamName));
    }

    @Given("^\"([^\"]*)\" has granted \"([^\"]*)\" permission to the teams over the cohort$")
    public void hasGrantedPermissionToTheTeamsOverTheCohort(String accountName, String permissionLevel,
                                                            List<String> teamNames) {
        authenticationSteps.authenticateUser(accountName);

        List<String> teamIds = teamNames.stream()
                .map(team -> Variables.interpolate(Variables.nameFrom(team, "id")))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, CohortPermissionHelper.createCohortPermissionTeamGrantMessage(
                Variables.interpolate("cohort_id"),
                permissionLevel.toUpperCase(),
                teamIds.toArray(new String[]{})
        ));

        messageOperations.receiveJSON(runner, action -> action.payload(
                CohortPermissionHelper.getCohortTeamGrantPermissionSuccessResponse(
                        Variables.interpolate("cohort_id"),
                        permissionLevel.toUpperCase(),
                        teamIds.toArray(new String[]{})
                )));

    }

    @When("^\"([^\"]*)\" has revoked \"([^\"]*)\" permission from the \"([^\"]*)\" team over the cohort$")
    public void hasRevokedPermissionFromTheTeamOverTheCohort(String accountName, String permissionLevel, String teamName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, CohortPermissionHelper.createCohortTeamPermissionRevokeMessage(
                Variables.interpolate("cohort_id"),
                Variables.interpolate(Variables.nameFrom(teamName, "id"))
        ));
        thePermissionIsSuccessfullyRevoked();
    }
}
