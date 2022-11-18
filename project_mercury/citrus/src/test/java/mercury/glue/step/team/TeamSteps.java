package mercury.glue.step.team;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.team.TeamHelper.createTeam;
import static mercury.helpers.team.TeamHelper.createTeamValidationCallback;
import static mercury.helpers.team.TeamHelper.updateTeam;
import static mercury.helpers.team.TeamHelper.updateTeamValidationCallback;
import static mercury.helpers.team.TeamHelper.deleteTeam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class TeamSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" creates a team$")
    public void createsATeam(String accountName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(accountName);

        Map<String, String> expectedFields = new HashMap<>(fields);
        String subscriptionId = runner.variable(getSubscriptionIdVar(accountName), interpolate(getSubscriptionIdVar(accountName)));
        expectedFields.put("subscriptionId", subscriptionId);
        runner.variable("expected_team", expectedFields);

        messageOperations.sendJSON(runner, createTeam(fields));
    }

    @Then("^the team is successfully created$")
    public void theTeamIsSuccessfullyCreated() {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(createTeamValidationCallback("expected_team")));
    }

    @When("^\"([^\"]*)\" updates the team \"([^\"]*)\"$")
    public void updatesTheTeam(String accountName, String teamName, Map<String, String> fields) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, updateTeam(interpolate(nameFrom(teamName, "id")), fields));
    }

    @Then("^the team is successfully updated and has fields$")
    public void theTeamIsSuccessfullyUpdatedAndHasFields(Map<String, String> fields) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(updateTeamValidationCallback(fields)));
    }

    @Given("^\"([^\"]*)\" has created a \"([^\"]*)\" team$")
    public void hasCreatedTeam(String user, String teamName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, createTeam(teamName));
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "iam.team.create.ok")
                        .extractFromPayload("$.response.team.id", nameFrom(teamName, "id"))
                        .extractFromPayload("$.response.team.name", nameFrom(teamName, "team_name")));
    }


    @And("^\"([^\"]*)\" has created teams$")
    public void hasCreatedTeams(String user, List<String> teamNames) {
        for (String name : teamNames) {
            hasCreatedTeam(user, name);
        }
    }

    @When("^\"([^\"]*)\" tries to delete the team \"([^\"]*)\"$")
    public void deletesTheTeam(String accountName, String teamName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, deleteTeam(interpolate(nameFrom(teamName, "id")), interpolate(getSubscriptionIdVar(accountName))));
    }

    @Then("^the team is successfully deleted")
    public void theTeamIsSuccessfullyDeleted() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.team.delete.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^the team is not deleted due to missing permission level$")
    public void theTeamIsNotDeletedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"iam.team.delete.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} has granted {string} permission level to team {string} over project {string}")
    public void hasGrantedPermissionLevelToTeamOverProject(final String accountName, final String permissionLevel,
                                                           final String teamName, final String projectName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "workspace.project.permission.grant")
                .addField("teamIds", Lists.newArrayList(interpolate(nameFrom(teamName, "id"))))
                .addField("projectId", interpolate(nameFrom(projectName, "id")))
                .addField("permissionLevel", permissionLevel)
                .build());

        messageOperations.validateResponseType(runner, "workspace.project.permission.grant.ok");
    }
}
