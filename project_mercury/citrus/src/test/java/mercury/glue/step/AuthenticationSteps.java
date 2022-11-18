package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.DEFAULT_PASSWORD;
import static mercury.glue.step.ProvisionSteps.getAccountEmailVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.fasterxml.jackson.core.JsonProcessingException;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class AuthenticationSteps {

    public static final String CURRENT_ACCOUNT = "current_account";
    private static String CURRENT_ACCOUNT_VAR = interpolate(CURRENT_ACCOUNT);

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Before
    public void initializeVariables() {
        runner.createVariable(CURRENT_ACCOUNT, ""); //this variable is used to define what user is authenticated in a socket
    }

    @Then("^It should \"([^\"]*)\" the user$")
    public void authenticateResponse(String action) {
        final String type = action.contains("not") ? "authenticate.error" : "authenticate.ok";
        messageOperations.validateResponseType(runner, type);
    }

    @Given("^a user is logged in and \"([^\"]*)\" is stored(?: to \"([^\"]*)\")?$")
    public void aUserIsLoggedInAndIsStored(String field, String variableName) throws Throwable {
        createStudent();
        sendAuthenticateUser();
        messageOperations.validateResponseType(runner, "authenticate.ok", action -> action
                .extractFromPayload("$.response." + field, variableName == null ? field : variableName));
    }

    @And("^a user with email \"([^\"]*)\" and password \"([^\"]*)\" is logged in$")
    public void aUserWithEmailAndPasswordIsLoggedIn(String email, String password) throws JsonProcessingException {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "authenticate").addField("email", email).addField("password", password);
        messageOperations.sendJSON(runner, payload.build());

        authenticateResponse("authenticate");
    }

    @Given("^a student is logged in$")
    public void aStudentIsLoggedIn() throws JsonProcessingException {
        createStudent();
        sendAuthenticateUser();
        authenticateResponse("authenticate");
    }

    @Given("^a user is logged in$")
    public void aUserIsLoggedIn() throws Throwable {
        aUserWithEmailAndPasswordIsLoggedIn("citrus@dev.dev", "password");
    }

    @Given("^a user is not logged in$")
    public void noUserAuthenticated() {
        runner.createVariable(CURRENT_ACCOUNT, "");
    }


    private void createStudent() throws JsonProcessingException {
        PayloadBuilder payload = new PayloadBuilder();
        runner.variable("randomEmail", "mercury:randomEmail()");
        payload.addField("type", "iam.student.provision")
                .addField("email", "${randomEmail}")
                .addField("password", "password");
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.validateResponseType(runner, "iam.student.provision.ok");
    }

    private void sendAuthenticateUser() throws JsonProcessingException {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "authenticate").addField("email", "${randomEmail}").addField("password", "password");
        messageOperations.sendJSON(runner, payload.build());
    }

    /**
     * Authenticates an account if it is not authenticated.
     * The current authenticated account is kept in 'current_account' variable.
     *
     * @param accountName account name
     * @param email       account's email, usually random generated email
     * @param password    account's password, usually {@link ProvisionSteps#DEFAULT_PASSWORD}
     */
    public void authenticateUser(String accountName, String email, String password) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "authenticate")
                .addField("email", email)
                .addField("password", password);
        runner.conditional()
                .when(CURRENT_ACCOUNT_VAR, not(equalTo(accountName)))
                .actions(
                        messageOperations.sendJSON(runner, payload.build()),
                        messageOperations.validateResponseType(runner, "authenticate.ok",
                                action -> action.extractFromPayload("$.response.bearerToken", accountName + "_bearerToken")),
                        runner.createVariable(CURRENT_ACCOUNT, accountName));

    }

    public void authenticateUser(String accountName) {
        this.authenticateUser(accountName, interpolate(getAccountEmailVar(accountName)), DEFAULT_PASSWORD);
    }

    @And("^\"([^\"]*)\" is authenticated and bearer token saved$")
    public void isAuthenticatedAndBearerTokenSaved(String user) {
        authenticateUser(user);
        String bearerToken = runner.variable(user + "_bearerToken", interpolate(user + "_bearerToken"));
        runner.createVariable("old_bearerToken", bearerToken);
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "This could not be executed by multiple instances. May be improved later.")
    @And("^\"([^\"]*)\" is logged out$")
    public void isLoggedOut(String user) {
        messageOperations.sendJSON(runner, logoutRequest(interpolate(user + "_bearerToken")));
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "me.logout.ok"));

        CURRENT_ACCOUNT_VAR = null;
        runner.createVariable(CURRENT_ACCOUNT, "");

    }

    @And("^\"([^\"]*)\" is authenticated$")
    public void isAuthenticated(String user) {
        authenticateUser(user);
    }

    @Then("^\"([^\"]*)\" can not logout with old bearer token due to code (\\d+) and message \"([^\"]*)\"$")
    public void canNotLogoutWithOldBearerTokenDueToCodeAndMessage(String user, int code, String message) {
        messageOperations.sendJSON(runner, logoutRequest("${old_bearerToken}"));
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "me.logout.error")
                        .jsonPath("$.code", code)
                        .jsonPath("$.response.reason", message));
    }

    private static String logoutRequest(String bearerToken) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "me.logout");
        payload.addField("bearerToken", bearerToken);
        return payload.build();
    }

    @Then("{string} can authenticate using password {string}")
    public void userAuthenticatesWithPassword(String user, String password) {
        this.authenticateUser(user, interpolate(getAccountEmailVar(user)), password);
    }

    @And("{string} authenticates via ies")
    public void authenticatesViaIes(String accountName) {

        final PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "ies.authorize")
                .addField("pearsonToken", interpolate(nameFrom(accountName, "pearsonToken")))
                .addField("pearsonUid", interpolate(nameFrom(accountName, "pearsonUid")));

        runner.conditional()
                .when(CURRENT_ACCOUNT_VAR, not(equalTo(accountName)))
                .actions(
                        // perform ies authorization
                        messageOperations.sendJSON(runner, payload.build()),
                        // validate the ies account is authorized
                        messageOperations.receiveJSON(runner, action ->
                                action.payload("{" +
                                        "\"type\":\"ies.authorize.ok\"," +
                                        "\"response\":{" +
                                        "\"bearerToken\":\"" + interpolate(nameFrom(accountName, "pearsonToken")) + "\"," +
                                        "\"expiry\":\"@notEmpty()@\"" +
                                        "},\"replyTo\":\"@notEmpty()@\"}")),
                        // save the variable to the current account
                        runner.createVariable(CURRENT_ACCOUNT, accountName),
                        // perform a me.get so the accountId can be stored
                        messageOperations.sendJSON(runner, new PayloadBuilder()
                                .addField("type", "me.get")
                                .build()),
                        // validate the response and save the account id
                        messageOperations.receiveJSON(runner, action -> action
                                .jsonPath("$.type", "me.get.ok")
                                .extractFromPayload("$.response.account.accountId", nameFrom(accountName, "id"))
                        ));
    }

    @And("{string} authenticates via mycloud")
    public void authenticatesViaMyCloud(String accountName) {

        final PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "mycloud.authorize")
                .addField("myCloudToken", interpolate(nameFrom(accountName, "pearsonToken")));

        runner.conditional()
                .when(CURRENT_ACCOUNT_VAR, not(equalTo(accountName)))
                .actions(
                        // perform myCloud authorization
                        messageOperations.sendJSON(runner, payload.build()),
                        // validate the myCloud account is authorized
                        messageOperations.receiveJSON(runner, action ->
                                action.payload("{" +
                                        "\"type\":\"mycloud.authorize.ok\"," +
                                        "\"response\":{" +
                                        "\"bearerToken\":\"" + interpolate(nameFrom(accountName, "pearsonToken")) + "\"," +
                                        "\"expiry\":\"@notEmpty()@\"" +
                                        "},\"replyTo\":\"@notEmpty()@\"}")),
                        // save the variable to the current account
                        runner.createVariable(CURRENT_ACCOUNT, accountName),
                        // perform a me.get so the accountId can be stored
                        messageOperations.sendJSON(runner, new PayloadBuilder()
                                .addField("type", "me.get")
                                .build()),
                        // validate the response and save the account id
                        messageOperations.receiveJSON(runner, action -> action
                                .jsonPath("$.type", "me.get.ok")
                                .extractFromPayload("$.response.account.accountId", nameFrom(accountName, "id"))
                        ));
    }
}
