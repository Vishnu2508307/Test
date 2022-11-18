package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.glue.step.AuthenticationSteps;

public class ThemePermissionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} grants {string} with {string} permission level for theme {string}")
    public void grantsWithPermissionLevelForTheme(String user, String account, String permission, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.grant")
                .addField("accountIds", Lists.newArrayList(interpolate(getAccountIdVar(account))))
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("permissionLevel", permission).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("{string} has {string} permission level for theme {string}")
    public void hasPermissionLevelForTheme(String account, String permission, String themeName) {
        messageOperations.receiveJSON(runner, action -> action.payload(grantThemePermissionResponse(
                Lists.newArrayList(interpolate(getAccountIdVar(account))),
                "account",
                themeName,
                permission
        )));
    }

    @Then("the theme is not shared due to missing permission level")
    public void theThemeIsNotSharedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"theme.permission.grant.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("{string} grants {string} permission level for theme {string} to {string}")
    public void grantsPermissionLevelForThemeTo(String user, String permissionLevel, String themeName,
                                                String collaboratorType, List<String> collaboratorNames) {
        authenticationSteps.authenticateUser(user);

        List<String> ids = collaboratorNames.stream()
                .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.grant")
                .addField(collaboratorType + "Ids", ids)
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("permissionLevel", permissionLevel).build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("the following {string} have {string} permission level over theme {string}")
    public void theFollowingHavePermissionLevelOverTheme(String collaboratorType, String permissionLevel,
                                                         String themeName, List<String> collaboratorNames) {
        List<String> ids = collaboratorNames.stream()
                .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                .collect(Collectors.toList());

        messageOperations.receiveJSON(runner, action -> action.payload(grantThemePermissionResponse(
                ids,
                collaboratorType,
                interpolate(nameFrom(themeName, "id")),
                permissionLevel
        )));
    }

    private String grantThemePermissionResponse(List<String> collaboratorIds, String collaboratorType,
                                                String themeName, String permission) {
        ObjectMapper om = new ObjectMapper();
        try {
            return "{" +
                    "\"type\":\"theme.permission.grant.ok\"," +
                    "\"response\":{" +
                    "\"" + collaboratorType + "Ids\":" + om.writeValueAsString(collaboratorIds) + "," +
                    "\"permissionLevel\":\"" + permission + "\"," +
                    "\"themeId\":\"@notEmpty()@\"" +
                    "},\"replyTo\":\"@notEmpty()@\"}";
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException(e.getMessage());
        }
    }

    @When("{string} revokes {string}'s permission for theme {string}")
    public void revokesSPermissionForTheme(String user, String account, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.revoke")
                .addField("accountId", interpolate(getAccountIdVar(account)))
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the theme permission is successfully revoked")
    public void theThemePermissionIsSuccessfullyRevoked() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"theme.permission.revoke.ok\",\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("the request is denied due to missing permission level for theme")
    public void theRequestIsDeniedDueToMissingPermissionLevelForTheme() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                       "\"type\":\"theme.permission.revoke.error\"," +
                                       "\"code\":401," +
                                       "\"message\":\"@notEmpty()@\"," +
                                       "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} revokes team {string} permission for theme {string}")
    public void revokesTeamPermissionForTheme(String user, String teamName, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.revoke")
                .addField("teamId", interpolate(nameFrom(teamName, "id")))
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .build();

        messageOperations.sendJSON(runner, message);
    }

    @And("{string} grants {string} permission level for theme {string} to team {string}")
    public void grantsPermissionLevelForThemeToTeam(String user, String permissionLevel, String themeName, String teamName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.grant")
                .addField("teamIds", Lists.newArrayList(interpolate(nameFrom(teamName, "id"))))
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("permissionLevel", permissionLevel).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the team {string} have {string} permission level over theme {string}")
    public void theTeamHavePermissionLevelOverTheme(String teamName, String permission, String themeName) {
        messageOperations.receiveJSON(runner, action -> action.payload(grantThemePermissionResponse(
                Lists.newArrayList(interpolate(nameFrom(teamName, "id"))),
                "team",
                themeName,
                permission
        )));
    }

    @When("{string} granted {string} with {string} permission level for theme {string}")
    public void grantedPermissionLevelForTheme(String user, String account, String permission, String themeName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "theme.permission.grant")
                .addField("accountIds", Lists.newArrayList(interpolate(getAccountIdVar(account))))
                .addField("themeId", interpolate(nameFrom(themeName, "id")))
                .addField("permissionLevel", permission).build();

        messageOperations.sendJSON(runner, message);
        messageOperations.receiveJSON(runner, action -> action.payload(grantThemePermissionResponse(
                Lists.newArrayList(interpolate(getAccountIdVar(account))),
                "account",
                themeName,
                permission
        )));
    }

    @When("{string} fetches the accounts for theme {string}")
    public void fetchesTheAccountsForTheme(String accountName, String themeName) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner,
                                   summaryThemeAccountsRequest(interpolate(nameFrom(themeName, "id")), null));
    }

    @When("{string} fetches the accounts for theme {string} with limit {int}")
    public void fetchesTheAccountsForThemeWithLimit(String accountName, String themeName, int limit) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendJSON(runner,
                                   summaryThemeAccountsRequest(interpolate(nameFrom(themeName, "id")), limit));
    }

    @Then("the theme collaborators list contains")
    public void theThemeCollaboratorsListContains(Map<String, String> expectedCollaborators) {
        messageOperations.receiveJSON(runner,
                                      action -> action.validationCallback(new ResponseMessageValidationCallback<Object>(
                                              Object.class) {
                                          @Override
                                          public void validate(Object payload,
                                                               Map<String, Object> headers,
                                                               TestContext context) {

                                              List<String> accountColl = expectedCollaborators.entrySet().stream()
                                                      .filter(one -> one.getValue().trim().equals("account"))
                                                      .map(Map.Entry::getKey).collect(Collectors.toList());

                                              List<String> teamColl = expectedCollaborators.entrySet().stream()
                                                      .filter(one -> one.getValue().trim().equals("team"))
                                                      .map(Map.Entry::getKey).collect(Collectors.toList());

                                              assertNotNull(accountColl);
                                              assertNotNull(teamColl);

                                              Map<String, Object> collaborators = (Map<String, Object>) payload;

                                              List<Object> actualAccountCollaborators = (List<Object>) collaborators.get(
                                                      "accounts");
                                              List<Object> actualTeamCollaborators = (List<Object>) collaborators.get(
                                                      "teams");

                                              if (!accountColl.isEmpty()) {
                                                  assertEquals(accountColl.size(), actualAccountCollaborators.size());
                                              }

                                              if (!teamColl.isEmpty()) {
                                                  assertEquals(teamColl.size(), actualTeamCollaborators.size());
                                              }
                                          }

                                          @Override
                                          public String getRootElementName() {
                                              return "collaborators";
                                          }

                                          @Override
                                          public String getType() {
                                              return "author.theme.collaborator.summary.ok";
                                          }
                                      }));
    }

    private String summaryThemeAccountsRequest(final String themeId, final Integer limit) {
        PayloadBuilder pb = new PayloadBuilder()
                .addField("type", "author.theme.collaborator.summary")
                .addField("themeId", themeId);
        if (limit != null) {
            pb.addField("limit", limit);
        }
        return pb.build();
    }
}
