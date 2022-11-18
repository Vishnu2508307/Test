package mercury.glue.step;

import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
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
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class UsersListSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^user requests a list of users$")
    public void userRequestsAListOfUsers() throws JsonProcessingException {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.subscription.user.list");
        messageOperations.sendJSON(runner, payload.build());
    }

    @Then("^\"([^\"]*)\" should fetch list of users$")
    public void shouldFetchListOfUsers(String accountName, List<String> users) throws Throwable {
        authenticationSteps.authenticateUser(accountName);

        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.subscription.user.list");
        messageOperations.sendJSON(runner, payload.build());

        messageOperations.receiveJSON(runner,
                action -> action.validationCallback(new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public String getRootElementName() {
                        return "accounts";
                    }

                    @Override
                    public String getType() {
                        return "iam.subscription.user.list.ok";
                    }

                    @Override
                    public void validate(ArrayList accounts, Map<String, Object> headers, TestContext context) {
                        //todo: add validation avatar
                        Set<Map> expectedAccounts = new HashSet<>();
                        for (String user : users) {
                            Map<String, String> expected = new HashMap<>();
                            expected.put("accountId", context.getVariable(getAccountIdVar(user)));
                            expected.put("email", context.getVariable(getAccountEmailVar(user)));
                            expectedAccounts.add(expected);
                        }

                        Set<Map> actualAccounts = new HashSet<>();
                        for (Object account : accounts) {
                            Map<String, String> actual = new HashMap<>();
                            actual.put("accountId", (String) ((Map) account).get("accountId"));
                            actual.put("email", (String) ((Map) account).get("primaryEmail"));
                            actualAccounts.add(actual);
                        }

                        assertEquals(expectedAccounts, actualAccounts);
                    }
                }));
    }

    @When("{string} fetches the list of users")
    public void fetchesTheListOfUsers(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.subscription.user.list").build());
    }

    @Then("the list of users is not returned due to missing permission level")
    public void theListOfUsersIsNotReturnedDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"iam.subscription.user.list.error\"," +
                                "\"code\":401," +
                                "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                                "\"replyTo\":\"@notEmpty()@\"}"));
    }
}
