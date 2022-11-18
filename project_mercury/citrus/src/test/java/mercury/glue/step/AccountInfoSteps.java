package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.common.ResponseMessageValidationCallback;

public class AccountInfoSteps {

    public static final String DEFAULT_PASSWORD = "password";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches following account details")
    public void fetchesAccountDetails(String user, List<String> accountNames) {
        authenticationSteps.authenticateUser(user);

        List<String> accountIds = accountNames.stream()
                .map(accountName -> interpolate(nameFrom(accountName, "id")))
                .collect(Collectors.toList());

        String message = new PayloadBuilder()
                .addField("type", "author.account.summary.list")
                .addField("accountIds", accountIds)
                .build();
        messageOperations.sendJSON(runner, message);
    }

    @Then("account details are successfully fetched")
    public void followingAccountDetailsAreSuccessfullyFetched() {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(
                new ResponseMessageValidationCallback<ArrayList>(ArrayList.class) {
                    @Override
                    public void validate(final ArrayList actualAccountSummary,
                                         final Map<String, Object> headers,
                                         final TestContext context) {
                        assertNotNull(actualAccountSummary);

                        for (int i = 0; i < actualAccountSummary.size(); i++) {
                            final Map accountSummary = (Map) actualAccountSummary.get(i);
                            assertNotNull(accountSummary.get("primaryEmail"));
                            assertNotNull(accountSummary.get("familyName"));
                            assertNotNull(accountSummary.get("givenName"));
                        }
                    }

                    @Override
                    public String getRootElementName() {
                        return "accountSummaryPayloads";
                    }

                    @Override
                    public String getType() {
                        return "author.account.summary.list.ok";
                    }
                }));
    }

    @Given("^a workspace account \"([^\"]*)\" is created with details$")
    public void aWorkspaceAccountIsCreated(String accountName) {
        PayloadBuilder payload = new PayloadBuilder();
        payload.addField("type", "iam.instructor.provision")
                .addField("email", "mercury:randomEmail()")
                .addField("password", DEFAULT_PASSWORD)
                .addField("familyName", "Doe")
                .addField("givenName", "Joe");
        messageOperations.sendJSON(runner, payload.build());
        receiveAndExtractAccount(accountName, "iam.instructor.provision.ok");
    }

    private void receiveAndExtractAccount(String accountName, String messageType) {
        messageOperations.validateResponseType(runner, messageType,
                                               action -> action.extractFromPayload("$.response.account.primaryEmail", ProvisionSteps.getAccountEmailVar(accountName))
                                                       .extractFromPayload("$.response.account.accountId", ProvisionSteps.getAccountIdVar(accountName))
                                                       .extractFromPayload("$.response.account.subscriptionId", ProvisionSteps.getSubscriptionIdVar(accountName)));
    }
}
