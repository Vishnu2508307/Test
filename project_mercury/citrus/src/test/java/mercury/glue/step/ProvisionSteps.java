package mercury.glue.step;

import static com.smartsparrow.rtm.message.handler.iam.AccountSubscriptionMigrateMessageHandler.IAM_ACCOUNT_SUBSCRIPTION_MIGRATE;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.Variables;
import mercury.helpers.subscription.SubscriptionHelper;
import reactor.core.Exceptions;

public class ProvisionSteps {

    public static final String DEFAULT_PASSWORD = "password";
    public static final String SUPPORT_ACCOUNT_ID = "57dbfa40-c49b-11e9-8623-39aed6204ec3";
    public static final String SUPPORT_ACCOUNT_SUBSCRIPTION_ID = "ac0baf30-c477-11e9-8fae-43bc3cbb0b3f";
    public static final String SUPPORT_ACCOUNT_EMAIL = "support@citrus.dev";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    private static boolean superUserCreated = false;

    @And("^a user with email \"([^\"]*)\" is created$")
    public void aUserWithEmailIsCreated(String email) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.instructor.provision")
                .addField("email", email)
                .addField("password", DEFAULT_PASSWORD);
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.validateResponseType(runner, "iam.instructor.provision.ok");
    }

    @And("^a random user is already created$")
    public void aRandomUserIsAlreadyCreated() {
        runner.variable("randomEmail", "mercury:randomEmail()");
        aUserWithEmailIsCreated("${randomEmail}");
    }

    @When("^I try to create an account for this random user again$")
    public void iTryToCreateAnAccountForThisRandomUserAgain() {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.instructor.provision")
                .addField("email", "${randomEmail}")
                .addField("password", DEFAULT_PASSWORD);
        messageOperations.sendJSON(runner, payload.build());
    }

    /**
     * Creates a user with email if it does not exist.
     * This method tries to create user once for all scenarios.
     * If user already exists in the database it ignores conflict exception.
     * This method is intended to create a super user 'citrus@dev.dev' that can be used in all scenarios.
     */
    @And("^a user with email \"([^\"]*)\" is created once$")
    public void aUserWithEmailIsCreatedOnce(String email) {
        if (superUserCreated) {
            //user citrus@dev.dev already exists
            return;
        }
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.instructor.provision").addField("email", email).addField("password", DEFAULT_PASSWORD);
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.receiveJSON(runner, action -> action.validationCallback((message, context) -> {
            String actualPayload = (String) message.getPayload();
            if (actualPayload.contains("iam.instructor.provision.ok") || actualPayload.contains("email already in use")) {
                superUserCreated = true;
            } else {
                throw new ValidationException("Super user '" + email + "' can not be created: " + actualPayload);
            }
        }));

    }

    @Given("^a workspace account \"([^\"]*)\" is created$")
    public void aWorkspaceAccountIsCreated(String accountName) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.instructor.provision")
                .addField("email", "mercury:randomEmail()")
                .addField("password", DEFAULT_PASSWORD);
        messageOperations.sendJSON(runner, payload.build());
        receiveAndExtractAccount(accountName, "iam.instructor.provision.ok");
    }

    @Given("^a student account \"([^\"]*)\" is created$")
    public void aStudentAccountIsCreated(String accountName) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.student.provision")
                .addField("email", "mercury:randomEmail()")
                .addField("password", DEFAULT_PASSWORD);
        messageOperations.sendJSON(runner, payload.build());
        receiveAndExtractAccount(accountName, "iam.student.provision.ok");
    }

    private void receiveAndExtractAccount(String accountName, String messageType) {
        messageOperations.validateResponseType(runner, messageType,
                action -> action.extractFromPayload("$.response.account.primaryEmail", getAccountEmailVar(accountName))
                        .extractFromPayload("$.response.account.accountId", getAccountIdVar(accountName))
                        .extractFromPayload("$.response.account.subscriptionId", getSubscriptionIdVar(accountName)));
    }

    @When("^\"([^\"]*)\" creates account \"([^\"]*)\" with ([^\"]*) role$")
    public void createsAccountWithRole(String loggedAccount, String account, String role)
            throws JsonProcessingException {
        authenticationSteps.authenticateUser(loggedAccount);
        List<String> roles = new ArrayList<>();
        if (role.equals("workspace")) {
            roles.add("DEVELOPER");
            roles.add("AERO_INSTRUCTOR");
            roles.add("ADMIN");
        } else {
            roles.add(role.toUpperCase());
        }

        provisionUserWithinSameSubscriptionWithRoles(DEFAULT_PASSWORD, roles);

        receiveAndExtractAccount(account, "iam.subscription.user.provision.ok");

    }

    @And("^\"([^\"]*)\" creates accounts with ([^\"]*) role$")
    public void createsAccountsWithWorkspaceRole(String loggedAccount, String role, List<String> variables) throws Throwable {
        for (String entry : variables) {
            createsAccountWithRole(loggedAccount, entry, role);
        }
    }

    @Given("^workspace accounts are created$")
    public void workspaceAccountsAreCreated(List<String> variables) {
        for (String entry : variables) {
            aWorkspaceAccountIsCreated(entry);
        }
    }

    @Given("^a new account \"([^\"]*)\" is created$")
    public void aNewAccountIsCreated(String accountName) {
        aWorkspaceAccountIsCreated(accountName);
    }

    public static String getAccountEmailVar(String accountName) {
        return accountName + "_email";
    }

    public static String getAccountIdVar(String accountName) {
        return accountName + "_id";
    }

    public static String getSubscriptionIdVar(String accountName) {
        return accountName + "_subscriptionId";
    }

    @Then("^a user should be created$")
    public void aUserShouldBeCreated() {
        messageOperations.receiveJSON(runner, action -> action.extractFromPayload("$.response.account.email[0]", "email"));
    }

    @And("^have roles: \"([^\"]*)\"$")
    public void haveRoles(String arg) {
        String[] roles = (arg.contains(", ") ? arg.split(", ") : new String[]{arg});

        authenticateProvisionedUser("${email}");

        requestProvisionedUserDetails();

        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.response.account.roles", Matchers.containsInAnyOrder(roles)));
    }

    private void requestProvisionedUserDetails() {
        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", "me.get");
        messageOperations.sendJSON(runner, payloadBuilder.build());
    }

    private void authenticateProvisionedUser(String email) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "authenticate").addField("email", email).addField("password", "password");
        messageOperations.sendJSON(runner, payload.build());
        messageOperations.validateResponseType(runner, "authenticate.ok");
    }

    @Given("^an ADMIN user with subscription OWNER permission is logged in$")
    public void anADMINUserWithSubscriptionOWNERPermissionIsLoggedIn() {
        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", "iam.instructor.provision");
        payloadBuilder.addField("email", "mercury:randomEmail()");
        payloadBuilder.addField("password", "password");

        messageOperations.sendJSON(runner, payloadBuilder.build());
        aUserShouldBeCreated();

        authenticateProvisionedUser("${email}");
    }

    @When("^I provision a new user within the same subscription$")
    public void iProvisionANewUserWithinTheSameSubscription() throws Throwable {
        provisionUserWithinSameSubscriptionWithRoles(DEFAULT_PASSWORD, Lists.newArrayList("DEVELOPER"));
    }

    private void provisionUserWithinSameSubscriptionWithRoles(String password, List<String> developer)
            throws JsonProcessingException {
        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", "iam.subscription.user.provision");
        payloadBuilder.addField("email", "mercury:randomEmail()");
        payloadBuilder.addField("password", password);
        payloadBuilder.addField("roles", developer);

        messageOperations.sendJSON(runner, payloadBuilder.build());
    }

    @Given("^a new workspace user is logged in$")
    public void aNewUserWithEmailIsLoggedIn() throws Throwable {
        aRandomUserIsAlreadyCreated();
        authenticateProvisionedUser("${randomEmail}");
    }

    @When("^\"([^\"]*)\" is created under different subscription$")
    public void isCreatedUnderDifferentSubscription(String accountName) {
        aWorkspaceAccountIsCreated(accountName);
    }

    @Given("^\"([^\"]*)\" provision \"([^\"]*)\" as a new user with role \"([^\"]*)\"$")
    public void provisionAsANewUserWithRole(String actorAccount, String subjectAccount, String role) {
        authenticationSteps.authenticateUser(actorAccount);

        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", "iam.subscription.user.provision");
        payloadBuilder.addField("email", "mercury:randomEmail()");
        payloadBuilder.addField("password", "password");
        payloadBuilder.addField("roles", Lists.newArrayList(role));

        messageOperations.sendJSON(runner, payloadBuilder.build());
        receiveAndExtractAccount(subjectAccount, "iam.subscription.user.provision.ok");
    }

    @When("^\"([^\"]*)\" migrates \"([^\"]*)\"'s account to his subscription as \"([^\"]*)\"$")
    public void migratesSAccountToHisSubscription(String actorAccount, String subjectAccount, String role) {
        runner.variable(nameFrom(subjectAccount, "role"), role);

        authenticationSteps.authenticateUser(actorAccount);

        PayloadBuilder payloadBuilder = new PayloadBuilder();
        payloadBuilder.addField("type", IAM_ACCOUNT_SUBSCRIPTION_MIGRATE);
        payloadBuilder.addField("accountId", Variables.interpolate(getAccountIdVar(subjectAccount)));
        payloadBuilder.addField("roles", Sets.newHashSet(role));

        messageOperations.sendJSON(runner, payloadBuilder.build());
    }

    @Then("^\"([^\"]*)\"'s account is under \"([^\"]*)\"'s subscription$")
    public void sAccountIsUnderSSubscription(String actorAccount, String subjectAccount) {
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "iam.account.subscription.migrate.ok")
                .jsonPath("$.response.account.accountId", Variables.interpolate(getAccountIdVar(actorAccount)))
                .jsonPath("$.response.account.subscriptionId", Variables.interpolate(getSubscriptionIdVar(subjectAccount)))
                .jsonPath("$.response.account.roles", "[" + Variables.interpolate(actorAccount, "role") + "]"));
    }

    @Then("^the account is not migrated$")
    public void theAccountIsNotMigrated() {
        messageOperations.receiveJSON(runner, action -> action.payload("{\"type\":\"iam.account.subscription.migrate.error\"," +
                "\"code\":401," +
                "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Given("^\"([^\"]*)\" is logged in$")
    public void isLoggedIn(String userName) {
        aWorkspaceAccountIsCreated(userName);
        authenticationSteps.authenticateUser(userName);
    }

    @Given("^a list of workspace users")
    public void aListOfSubscriptionAdmins(List<String> accountNames) {
        authenticateProvisionedUser("citrus@dev.dev");

        ArrayList<String> roles = Lists.newArrayList("ADMIN", "AERO_INSTRUCTOR", "DEVELOPER");

        accountNames.forEach(accountName -> {
            try {
                provisionUserWithinSameSubscriptionWithRoles(DEFAULT_PASSWORD, roles);
                receiveAndExtractAccount(accountName, "iam.subscription.user.provision.ok");

                messageOperations.sendJSON(runner, SubscriptionHelper.createSubscriptionPermissionGrantRequest(
                        Variables.interpolate(getSubscriptionIdVar(accountName)),
                        "account",
                        Lists.newArrayList(Variables.interpolate(getAccountIdVar(accountName))),
                        "CONTRIBUTOR"
                ));

                messageOperations.validateResponseType(runner, "iam.subscription.permission.grant.ok");

            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }

        });
    }


    @When("^\"([^\"]*)\" provisions a new user \"([^\"]*)\" within the same subscription with roles$")
    public void provisionsANewUserWithinTheSameSubscription(String creator, String createe, List<String> roles)
            throws Throwable {
        provisionUserWithinSameSubscriptionWithRoles(DEFAULT_PASSWORD, roles);
        receiveAndExtractAccount(createe, "iam.subscription.user.provision.ok");
    }

    @And("^\"([^\"]*)\" provides \"([^\"]*)\" with a new account in the subscription as$")
    public void providesWithANewAccountInTheSubscriptionAs(String user, String newAccountName, List<String> dataTable) throws Throwable {
        authenticationSteps.authenticateUser(user);
        provisionUserWithinSameSubscriptionWithRoles("password", dataTable);
        receiveAndExtractAccount(newAccountName, "iam.subscription.user.provision.ok");
    }

    @Given("a support role account {string} exists")
    public void aSupportRoleAccount(String user) {
        runner.createVariable(getAccountEmailVar(user), SUPPORT_ACCOUNT_EMAIL);
        runner.createVariable(getAccountIdVar(user), SUPPORT_ACCOUNT_ID);
        runner.createVariable(getSubscriptionIdVar(user), SUPPORT_ACCOUNT_SUBSCRIPTION_ID);
    }

    @Then("the account is not provisioned")
    public void theAccountIsNotProvisioned() {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "iam.subscription.user.provision.error")
                .jsonPath("$.message", "Invalid role/s supplied")
        );
    }

    @When("{string} try to provision a new user {string} within the same subscription with roles")
    public void tryToProvisionANewUserWithinTheSameSubscriptionWithRoles(final String accountName,
                                                                         final String newAccountName,
                                                                         final List<String> roles) throws JsonProcessingException {
        provisionUserWithinSameSubscriptionWithRoles(DEFAULT_PASSWORD, roles);
    }

    @Given("an ies account {string} is provisioned")
    public void anIesAccountIsProvisioned(String accountName) {
        String sequence = "abcdefghijklmnopqrstuvwxyz0123456789_";

        final String pearsonUid = RandomStringUtils.random(10, sequence);
        JWT pearsonJWT = new PlainJWT(new PlainHeader.Builder().build(), new JWTClaimsSet.Builder()
                .claim("exp", TimeUnit.MILLISECONDS.
                        toSeconds(Instant.now().plus(30, ChronoUnit.MINUTES).toEpochMilli()))
                .claim("sub", pearsonUid)
                .build());
        // this is a valid JWT
        final String pearsonToken = pearsonJWT.serialize();
        // save the variables to the citrus context
        runner.variable(nameFrom(accountName, "pearsonUid"), pearsonUid);
        runner.variable(nameFrom(accountName, "pearsonToken"), pearsonToken);
        // authenticate the user
        authenticationSteps.authenticatesViaIes(accountName);

    }

    @Given("a mycloud account {string} is provisioned")
    public void aMyCloudAccountIsProvisioned(String accountName) {
        String sequence = "abcdefghijklmnopqrstuvwxyz0123456789_";

        final String pearsonToken = RandomStringUtils.random(10, sequence);

        // save the variables to the citrus context
        runner.variable(nameFrom(accountName, "pearsonToken"), pearsonToken);

        // authenticate the user
        authenticationSteps.authenticatesViaMyCloud(accountName);
    }
}
