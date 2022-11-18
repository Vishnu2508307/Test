package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.learner.LearnerWalkableFetchHelper.fetchActivityScopeEntryQuery;
import static mercury.helpers.learner.LearnerWalkableFetchHelper.fetchScopeEntryQuery;
import static mercury.helpers.learner.LearnerWalkableFetchHelper.fetchWalkableQuery;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class LearnerFetchWalkableSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" gets the next walkable from$")
    public void getsTheNextWalkableFrom(String user, Map<String, String> fields) {
        authenticationSteps.authenticateUser(user);

        final String deploymentId = interpolate(nameFrom(fields.get("deploymentName"), "id"));
        final String pathwayId = interpolate(nameFrom(fields.get("pathwayName"), "id"));
        final String cohortId = interpolate(nameFrom(fields.get("cohortName"), "id"));
        messageOperations.sendGraphQL(runner, fetchWalkableQuery(cohortId, deploymentId, pathwayId));

    }

    @Then("^\"([^\"]*)\" interactive is returned$")
    public void interactiveIsReturned(String interactiveName) {
        final String jsonPath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.id";
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(jsonPath, interpolate(nameFrom(interactiveName, "id"))));
    }

    @When("^\"([^\"]*)\" fetches scope entry for cohort in deployment \"([^\"]*)\"$")
    public void fetchesScopeEntryForCohortInDeployment(String user, String deploymentName) {
        messageOperations.sendGraphQL(runner, fetchScopeEntryQuery(
                interpolate(nameFrom("cohort", "id")),
                interpolate(nameFrom(deploymentName, "id"))
        ));
    }

    @Then("^the following scope entry data is returned$")
    public void theFollowingScopeEntryDataIsReturned(String data) {

        String path = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.scope[0].data";
        messageOperations.receiveJSON(runner, action -> action.jsonPath(path, data));
    }

    @When("{string} fetches scope entry for cohort {string} deployment {string} and activity {string}")
    public void fetchesScopeEntryForCohortDeploymentAndActivity(String accountName, String cohortName, String deploymentName,
                                                                String activityName) {
        messageOperations.sendGraphQL(runner, fetchActivityScopeEntryQuery(
                interpolate(nameFrom(cohortName, "id")),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(activityName, "id"))
        ));
    }

    @Then("the following activity scope entry data is returned")
    public void theFollowingActivityScopeEntryDataIsReturned(String data) {
        String path = "$.response.data.learn.cohort.deployment[0].activity.scope[0].data";
        messageOperations.receiveJSON(runner, action -> action.jsonPath(path, data));
    }
}
