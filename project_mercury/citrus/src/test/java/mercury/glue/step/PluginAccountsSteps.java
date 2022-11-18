package mercury.glue.step;

import static mercury.glue.step.PluginShareSteps.PLUGIN_ID_VAR;
import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.plugin.PluginHelper.listPluginCollaboratorsRequest;
import static mercury.helpers.plugin.PluginHelper.validatePluginCollaboratorList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;
import mercury.common.Variables;
import mercury.helpers.plugin.PluginHelper;

public class PluginAccountsSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" requests detailed list of plugin accounts$")
    public void requestsDetailedListOfPluginAccounts(String accountName) throws JsonProcessingException {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.account.list");
        payload.addField("pluginId", Variables.interpolate(PLUGIN_ID_VAR));
        messageOperations.sendJSON(runner, payload.build());
    }

    @When("^\"([^\"]*)\" requests summary list of plugin accounts$")
    public void requestsSummaryListOfPluginAccounts(String accountName) throws Throwable {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "workspace.plugin.account.summary");
        payload.addField("pluginId", Variables.interpolate(PLUGIN_ID_VAR));
        messageOperations.sendJSON(runner, payload.build());
    }

    @SuppressWarnings("Duplicates")
    @Then("^the following users are in the detailed list$")
    public void theFollowingUsersAreInTheDetailedList(Map<String, String> dataTable) {

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "collaborators";
                    }

                    @Override
                    public String getType() {
                        return "workspace.plugin.account.list.ok";
                    }

                    @Override
                    public void validate(ArrayList collaborators, Map<String, Object> headers, TestContext context) {
                        Set<Map> expectedAccounts = new HashSet<>();
                        for (Map.Entry<String, String> user : dataTable.entrySet()) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("accountId", context.getVariable(getAccountIdVar(user.getKey())));
                            expected.put("email", context.getVariable(getAccountEmailVar(user.getKey())));
                            expected.put("permission", user.getValue());
                            expectedAccounts.add(expected);
                        }

                        Set<Map> actualAccounts = new HashSet<>();
                        for (Object coll : collaborators) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("accountId", (String) ((Map) ((Map) coll).get("account")).get("accountId"));
                            actual.put("email", (String) ((Map) ((Map) coll).get("account")).get("primaryEmail"));
                            actual.put("permission", (String) ((Map) coll).get("permissionLevel"));
                            actualAccounts.add(actual);
                        }

                        assertEquals(expectedAccounts, actualAccounts);

                    }
                }));
    }

    @SuppressWarnings("Duplicates")
    @Then("^the following users are in the summary list$")
    public void theFollowingUsersAreInTheSummaryList(Map<String, String> dataTable) {

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new JsonMappingValidationCallback<BasicResponseMessage>(BasicResponseMessage.class) {
                    @Override
                    public void validate(BasicResponseMessage payload, Map<String, Object> headers, TestContext context) {
                        assertEquals("workspace.plugin.account.summary.ok", payload.getType());

                        Set<Map.Entry<String, String>> users = dataTable.entrySet();

                        assertEquals(users.size(), (int) payload.getResponse().get("total"));

                        ArrayList accounts = (ArrayList) payload.getResponse().get("collaborators");

                        Set<Map> expectedAccounts = new HashSet<>();
                        for (Map.Entry<String, String> user : users) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("accountId", context.getVariable(getAccountIdVar(user.getKey())));
                            expected.put("email", context.getVariable(getAccountEmailVar(user.getKey())));
                            expected.put("permission", user.getValue());
                            expectedAccounts.add(expected);
                        }

                        Set<Map> actualAccounts = new HashSet<>();
                        for (Object acc : accounts) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("accountId", (String) ((Map) ((Map) acc).get("account")).get("accountId"));
                            actual.put("email", (String) ((Map) ((Map) acc).get("account")).get("primaryEmail"));
                            actual.put("permission", (String) ((Map) acc).get("permissionLevel"));
                            actualAccounts.add(actual);
                        }

                        assertEquals(expectedAccounts, actualAccounts);
                    }
                }));
    }

    @Then("^the list of plugin collaborators contains total (\\d+)$")
    public void theListOfPluginCollaboratorsContainsTotal(int expectedTotal, List<PluginHelper.CollaboratorItem> expectedList) {
        messageOperations.sendJSON(runner, listPluginCollaboratorsRequest(Variables.interpolate(PLUGIN_ID_VAR), null));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(validatePluginCollaboratorList(expectedTotal, expectedList)));
    }

    @Then("^the list of plugin collaborators with limit (\\d+) contains total (\\d+)$")
    public void theListOfPluginCollaboratorsWithLimitContainsTotal(int limit, int expectedTotal, List<PluginHelper.CollaboratorItem> expectedList) {
        messageOperations.sendJSON(runner, listPluginCollaboratorsRequest(Variables.interpolate(PLUGIN_ID_VAR), limit));
        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(validatePluginCollaboratorList(expectedTotal, expectedList)));
    }


    @Then("^\"([^\"]*)\" cannot list plugin collaborators$")
    public void cannotListPluginCollaborators(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, listPluginCollaboratorsRequest(Variables.interpolate(PLUGIN_ID_VAR), null));

        messageOperations.receiveJSON(runner, action ->
                action.validate("$.type", "workspace.plugin.collaborator.summary.error")
                        .validate("$.code", 401)
                        .validate("$.message", "Unauthorized: Unauthorized permission level"));
    }
}
