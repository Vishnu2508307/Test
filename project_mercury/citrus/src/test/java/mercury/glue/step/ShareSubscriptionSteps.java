package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.step.ProvisionSteps.getSubscriptionIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.collaborator.CollaboratorHelper.validateCollaboratorsResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.helpers.subscription.SubscriptionHelper;

public class ShareSubscriptionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" shares his subscription with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void sharesHisSubscriptionWith(String loggedAccount, String accountName, String permissionLevel) throws Throwable {
        authenticationSteps.authenticateUser(loggedAccount);

        messageOperations.sendJSON(runner, SubscriptionHelper.createSubscriptionPermissionGrantRequest(
                interpolate(getSubscriptionIdVar(loggedAccount)),
                "account",
                Lists.newArrayList(interpolate(getAccountIdVar(accountName))),
                permissionLevel
        ));
    }

    @Then("^subscription shared successfully$")
    public void subscriptionSharedSuccessfully() {
        messageOperations.validateResponseType(runner, "iam.subscription.permission.grant.ok");
    }

    @Then("^\"([^\"]*)\" can share his subscription with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void canShareHisSubscriptionWith(String loggedAccount, String accountName, String permissionLevel) throws Throwable {
        sharesHisSubscriptionWith(loggedAccount, accountName, permissionLevel);
        subscriptionSharedSuccessfully();
    }

    @Given("^\"([^\"]*)\" has shared his subscription with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void hasSharedHisSubscriptionWith(String loggedAccount, String accountName, String permissionLevel) throws Throwable {
        sharesHisSubscriptionWith(loggedAccount, accountName, permissionLevel);
        subscriptionSharedSuccessfully();
    }

    @When("^\"([^\"]*)\" shares \"([^\"]*)\"'s subscription with \"([^\"]*)\" as \"([^\"]*)\"$")
    public void sharesSSubscriptionWithAs(String account, String subscriptionUser, String user, String permissionLevel) {
        authenticationSteps.authenticateUser(account);

        messageOperations.sendJSON(runner, SubscriptionHelper.createSubscriptionPermissionGrantRequest(
                interpolate(getSubscriptionIdVar(subscriptionUser)),
                "account",
                Lists.newArrayList(interpolate(getAccountIdVar(user))),
                permissionLevel
        ));
    }

    @Given("^\"([^\"]*)\" grants \"([^\"]*)\" permission level over the subscription to \"([^\"]*)\"$")
    public void grantsPermissionLevelOverTheSubscriptionTo(String user, String permissionLevel, String collaboratorType,
                                                           List<String> collaboratorNames) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, SubscriptionHelper.createSubscriptionPermissionGrantRequest(
                interpolate(getSubscriptionIdVar(user)),
                collaboratorType,
                collaboratorNames.stream()
                        .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                        .collect(Collectors.toList()),
                permissionLevel
        ));
    }

    @Then("^the following \"([^\"]*)\" have \"([^\"]*)\" permission level over \"([^\"]*)\"'s subscription$")
    public void theFollowingHavePermissionLevelOverSSubscription(String collaboratorType, String permissionLevel,
                                                                 String userSubscription, List<String> collaborators) {
        messageOperations.receiveJSON(runner, action -> action.payload(SubscriptionHelper.grantSubscriptionPermissionResponse(
                collaborators.stream()
                        .map(collaboratorName -> interpolate(nameFrom(collaboratorName, "id")))
                        .collect(Collectors.toList()),
                collaboratorType,
                interpolate(getSubscriptionIdVar(userSubscription)),
                permissionLevel
        )));
    }

    @Given("^\"([^\"]*)\" has granted \"([^\"]*)\" permission level over the subscription to \"([^\"]*)\"$")
    public void hasGrantedPermissionLevelOverTheSubscriptionTo(String user, String permissionLevel, String collaboratorType,
                                                               List<String> collaborators) {
        grantsPermissionLevelOverTheSubscriptionTo(user, permissionLevel, collaboratorType, collaborators);
        theFollowingHavePermissionLevelOverSSubscription(collaboratorType, permissionLevel, user, collaborators);
    }

    @When("^\"([^\"]*)\" fetches the collaborators for the subscription$")
    public void fetchesTheCollaboratorsForTheSubscription(String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.subscription.collaborator.summary")
                .addField("subscriptionId", interpolate(getSubscriptionIdVar(user)))
                .build());
    }

    @SuppressWarnings("unchecked")
    @Then("^the subscription collaborators list contains$")
    public void theSubscriptionCollaboratorsListContains(Map<String, String> collaborators) {
        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                validateCollaboratorsResponse(collaborators, "iam.subscription.collaborator.summary.ok")
        ));
    }

    @When("^\"([^\"]*)\" fetches the collaborators for the subscription with limit (\\d+)$")
    public void fetchesTheCollaboratorsForTheSubscriptionWithLimit(String user, int limit) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "iam.subscription.collaborator.summary")
                .addField("limit", limit)
                .addField("subscriptionId", interpolate(getSubscriptionIdVar(user)))
                .build());
    }
}
