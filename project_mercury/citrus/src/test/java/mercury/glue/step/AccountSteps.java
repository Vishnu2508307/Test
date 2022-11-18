package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.glue.step.ProvisionSteps.DEFAULT_PASSWORD;
import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
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
import mercury.common.PayloadBuilder;

public class AccountSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches account info using {string}'s email")
    public void fetchesAccountInfoUsingSEmail(String user, String accountName) {

        authenticationSteps.authenticateUser(user);

        String email = getAccountEmailVar(accountName);

        String query = "query {\n" +
                       "  accountByEmail(email: \"" + interpolate(email) + "\") {\n" +
                       "    id\n" +
                       "  }\n" +
                "}";

        messageOperations.sendGraphQL(runner, query);
    }

    @Then("the account by email info request is not allowed")
    public void theAccountByEmailInfoRequestIsNotAllowed() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "graphql.response")
                .jsonPath("$.response.errors[0].extensions.type", "FORBIDDEN"));
    }

    @Given("{string} wait {int} seconds")
    public void waitSeconds(String user, int seconds) {
        runner.sleep(seconds * 1000);
    }

    @Then("{string}'s account information is successfully fetched")
    public void sAccountInformationIsSuccessfullyFetched(String user) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.response.data.accountByEmail.id", interpolate(getAccountIdVar(user))));
    }

    @When("{string} sets the password for account {string} as {string}")
    public void supportUserSetPassword(String support, String account, String password) {
        authenticationSteps.authenticateUser(support);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.account.password.set")
                .addField("accountId", interpolate(getAccountIdVar(account)))
                .addField("password", password)
                .build()
        );
    }

    @Then("the account password is successfully set")
    public void supportUserPasswordSetSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.account.password.set.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the account password is not set due to missing permission level")
    public void supportUserPasswordFailsMissingPermission() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"iam.account.password.set.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} sets the password for their account as {string}")
    public void userSetPassword(String account, String password) {
        authenticationSteps.authenticateUser(account);
        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.password.set")
                .addField("oldPassword", DEFAULT_PASSWORD)
                .addField("newPassword", password)
                .addField("confirmNew", password)
                .build()
        );
    }

    @Then("the password is successfully set")
    public void userPasswordSetSuccessfully() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{\"type\":\"iam.password.set.ok\",\"replyTo\":\"@notEmpty()@\"}"));
    }
}
