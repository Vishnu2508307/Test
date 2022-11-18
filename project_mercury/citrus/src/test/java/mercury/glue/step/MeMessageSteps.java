package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;

public class MeMessageSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Then("^mercury should respond with account details$")
    public void mercuryShouldRespondWithAccountDetails() {
        messageOperations.validateResponseType(runner, "me.get.ok",
                action -> action
                        .jsonPath("$.response.account.accountId", Matchers.not(Matchers.empty()))
                        .jsonPath("$.response.account.subscriptionId", Matchers.not(Matchers.empty()))
                        .jsonPath("$.response.account.iamRegion", Matchers.not(Matchers.empty()))
                        .jsonPath("$.response.account.primaryEmail", Matchers.not(Matchers.empty()))
                        .jsonPath("$.response.account.roles", Matchers.not(Matchers.empty()))
                        .jsonPath("$.response.account.email", Matchers.not(Matchers.empty()))
                        .jsonPath("$.replyTo", Matchers.not(Matchers.empty())));
    }
}
