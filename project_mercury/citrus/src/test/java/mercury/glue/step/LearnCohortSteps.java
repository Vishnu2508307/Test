package mercury.glue.step;

import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;

public class LearnCohortSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;


    @Then("^\"([^\"]*)\" can not query learn cohort due to error with code (\\d+) and message \"([^\"]*)\"$")
    public void canNotQueryLearnCohortDueToErrorWithCodeAndMessage(String accountName, int code, String message) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendGraphQL(runner, "query {learn { cohort(cohortId: \"${cohort_id}\") {id} }}");

        messageOperations.validateResponseType(runner, "graphql.response", action ->
                action.jsonPath("$.response.data.learn.cohort", null)
                        .jsonPath("$.response.errors[0].message", "Exception while fetching data (/learn/cohort) : " + message));
    }

    @Then("^\"([^\"]*)\" can query learn cohort successfully$")
    public void canQueryLearnCohortSuccessfully(String accountName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendGraphQL(runner, "query {learn { cohort(cohortId: \"${cohort_id}\") {id} }}");

        messageOperations.validateResponseType(runner, "graphql.response", action ->
                action.jsonPath("$.response.data.learn.cohort.id", "${cohort_id}"));
    }
}
