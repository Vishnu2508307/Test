package mercury.glue.step.team;

import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.glue.step.AuthenticationSteps;
import mercury.helpers.team.TeamHelper;

public class TeamListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" lists the teams in the subscription$")
    public void listsTheTeamsInTheSubscription(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, TeamHelper.listSubscriptionTeamsRequest(2));
    }

    @Then("^the following teams are returned$")
    public void theFollowingTeamsAreReturned(List<String> teamNameList) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(TeamHelper.validateReceivedTeamsCallback(
                        teamNameList,
                        "iam.team.list.ok"
                )));
    }

    @When("^\"([^\"]*)\" lists the teams$")
    public void listsTheTeams(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.team.list")
                .addField("collaboratorLimit", 5)
                .build());
    }

    @Then("^an empty list of teams is returned$")
    public void anEmptyListOfTeamsIsReturned() {
        messageOperations.receiveJSON(runner, action ->
                action.payload(TeamHelper.teamResponseEmptyList("iam.team.list.ok")));
    }

    @Then("^the following subscription teams are returned$")
    public void theFollowingSubscriptionTeamsAreReturned(List<String> teamNameList) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(TeamHelper.validateReceivedTeamsCallback(
                        teamNameList,
                        "iam.subscription.team.list.ok"
                )));
    }

    @Then("^an empty subscription list of teams is returned$")
    public void anEmptySubscriptionListOfTeamsIsReturned() {
        messageOperations.receiveJSON(runner, action ->
                action.payload(TeamHelper.teamResponseEmptyList("iam.subscription.team.list.ok")));
    }

    @When("^\"([^\"]*)\" lists the \"([^\"]*)\" team collaborators with limit (\\d+)$")
    public void listsTheTeamCollaboratorsWithLimit(String user, String teamName, Integer limit) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.team.account.summary")
                .addField("teamId", Variables.interpolate(Variables.nameFrom(teamName, "id")))
                .addField("limit", limit)
                .build());
    }

    @Then("^the following collaborators are returned$")
    public void theFollowingCollaboratorsAreReturned(List<String> accountNames) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new ResponseMessageValidationCallback<List>(List.class) {
            @Override
            public void validate(List payload, Map<String, Object> headers, TestContext context) {
                assertEquals(accountNames.size(), payload.size());

                final Map<String, String> expected = accountNames.stream()
                        .map(one-> {
                            final String id = context.getVariable(Variables.interpolate(getAccountIdVar(one)));
                            return Maps.newHashMap(id, id);
                        }).reduce((prev, next) ->{
                            next.putAll(prev);
                            return next;
                        }).orElse(new HashMap<>());

                for (Object aPayload : payload) {
                    Map collaboratorPayload = (Map) aPayload;
                    Map account = (Map) collaboratorPayload.get("account");
                    String accountId = String.valueOf(account.get("accountId"));
                    assertEquals(expected.get(accountId), accountId);
                }
            }

            @Override
            public String getRootElementName() {
                return "collaborators";
            }

            @Override
            public String getType() {
                return "iam.team.account.summary.ok";
            }
        }));
    }

    @When("^\"([^\"]*)\" lists the \"([^\"]*)\" team collaborators$")
    public void listsTheTeamCollaborators(String user, String teamName) {
        listsTheTeamCollaboratorsWithLimit(user, teamName, null);
    }

    @SuppressWarnings("unchecked")
    @Then("^only (\\d+) team collaborator is returned with a total of (\\d+)$")
    public void onlyTeamCollaboratorIsReturnedWithATotalOf(int collaboratorsReturned, int collaboratorsTotal) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
            @Override
            public void validate(BasicResponseMessage message, Map<String, Object> headers, TestContext context) {
                Map<String, Object> response = message.getResponse();

                assertEquals(collaboratorsTotal, (int)response.get("total"));
                List<Object> collaborators = (List<Object>) response.get("collaborators");
                assertEquals(collaboratorsReturned, collaborators.size());
            }
        }));
    }

    @Then("the teams are not listed due to missing permission level")
    public void theTeamsAreNotListedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"iam.subscription.team.list.error\"," +
                                "\"code\":401," +
                                "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                                "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
