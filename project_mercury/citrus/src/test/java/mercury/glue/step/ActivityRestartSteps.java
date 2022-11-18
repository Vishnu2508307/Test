package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class ActivityRestartSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" opens the \"([^\"]*)\" deployment")
    public void opensTheActivity(String userName, String deploymentName) {

        authenticationSteps.authenticateUser(userName);

        String query = "{" +
                "  learn {" +
                "    cohort(cohortId: \"" + interpolate("cohort_id") + "\") {" +
                "      deployment(deploymentId: \"" + interpolate(deploymentName, "id") + "\") {\n" +
                "        activity {" +
                "          attempt {" +
                "            value " +
                "          }" +
                "          progress {" +
                "            completion {" +
                "              value" +
                "            }" +
                "          }" +
                "          pathways {" +
                "            walkables {" +
                "              edges {" +
                "                node {" +
                "                  id" +
                "                  attempt {" +
                "                    value" +
                "                  }" +
                "                  progress {" +
                "                    completion {" +
                "                      value" +
                "                    }" +
                "                  }" +
                "                }" +
                "              }" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";

        messageOperations.sendGraphQL(runner, query);

    }

    @When("^\"([^\"]*)\" opens the \"([^\"]*)\" activity for the \"([^\"]*)\" deployment$")
    public void opensTheActivityForTheDeployment(String userName, String activityName, String deploymentName) {

        authenticationSteps.authenticateUser(userName);

        String query = "{" +
                "  learn {" +
                "    cohort(cohortId: \"" + interpolate("cohort_id") + "\") {" +
                "      deployment(deploymentId: \"" + interpolate(deploymentName, "id") + "\") {" +
                "        activity {" +
                "          pathways {" +
                "            walkables {" +
                "              edges {" +
                "                node {" +
                "                  ... on LearnerActivity{" +
                "                    pathways {" +
                "                      walkables {" +
                "                        edges {" +
                "                          node {" +
                "                            id" +
                "                            attempt {" +
                "                              value" +
                "                            }" +
                "                            scope {" +
                "                              data" +
                "                            }" +
                "                            progress {" +
                "                              completion {" +
                "                                value" +
                "                              }" +
                "                            }" +
                "                          }" +
                "                        }" +
                "                      }" +
                "                    }" +
                "                  }" +
                "                }" +
                "              }" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";

        messageOperations.sendGraphQL(runner, query);

    }

    @Then("^\"([^\"]*)\" gets the \"([^\"]*)\" interactive$")
    public void getsTheInteractive(String userName, String interactiveName) {
        String interactivePath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.pathways[0].walkables.edges[0].node";
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(interactivePath + ".id", interpolate(interactiveName, "id"))
                        .jsonPath(interactivePath + ".attempt.value", 1));
    }

    @Then("^\"([^\"]*)\" gets the \"([^\"]*)\" interactive with$")
    public void getsTheInteractiveWith(String userName, String interactiveName, Map<String, String> values) {
        String interactivePath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.pathways[0].walkables.edges[0].node";

        String expectedScope = values.get("SCOPE_DATA");

        final String expectedScopeData = getExpectedScopeData(expectedScope);

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(interactivePath + ".id", interpolate(interactiveName, "id"))
                        .jsonPath(interactivePath + ".attempt.value", Integer.valueOf(values.get("ATTEMPT")))
                        .jsonPath(interactivePath + ".scope", expectedScopeData));
    }

    private String getExpectedScopeData(String expectedScope) {
        if (expectedScope.equals("EMPTY")) {
            return "[{\"data\":\"\"}]";
        } else if (expectedScope.equals("NOT_EMPTY")) {
            return "@notEmpty()@";
        }
        return null;
    }

    @Then("^\"([^\"]*)\" gets interactives with values$")
    public void getsInteractivesWithValues(String userName, List<ProgressItem> values) {
        String interactivePath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.pathways[0].walkables";

        String progressOnePath = interactivePath + ".edges[0].node.progress";
        String progressTwoPath = interactivePath + ".edges[1].node.progress";

        final Double progressOneValue = values.get(0).progress;
        final Double progressTwoValue = values.get(1).progress;

        final String progressValueOnePath = (progressOneValue != null ? progressOnePath + ".completion.value" : progressOnePath);
        final String progressValueTwoPath = (progressTwoValue != null ? progressTwoPath + ".completion.value" : progressTwoPath);

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(interactivePath + ".edges[0].node.id", interpolate(values.get(0).id, "id"))
                        .jsonPath(interactivePath + ".edges[0].node.attempt.value", values.get(0).attempt)
                        .jsonPath(progressValueOnePath, progressOneValue)
                        .jsonPath(interactivePath + ".edges[1].node.id", interpolate(values.get(1).id, "id"))
                        .jsonPath(interactivePath + ".edges[1].node.attempt.value", values.get(1).attempt)
                        .jsonPath(progressValueTwoPath, progressTwoValue));
    }

    @Then("^\"([^\"]*)\" gets the \"([^\"]*)\" activity with attempts")
    public void getsTheActivity(String userName, String activityName, List<List<String>> expectedAttempts) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.id",
                        interpolate(activityName, "id"))
                        .jsonPath("$.response.data.learn.cohort.deployment[0].activity.attempt.value", expectedAttempts.get(0).get(1))
                        .jsonPath("$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.attempt.value", expectedAttempts.get(1).get(1)));
    }

    @Then("^\"([^\"]*)\" gets the \"([^\"]*)\" activity with values$")
    public void getsTheActivityWithValues(String userName, String activityName, List<ProgressItem> values) {

        String progressOnePath = "$.response.data.learn.cohort.deployment[0].activity.progress";
        String progressTwoPath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.progress";

        final Double progressOneValue = values.get(0).progress;
        final Double progressTwoValue = values.get(1).progress;

        final String progressValueOnePath = (progressOneValue != null ? progressOnePath + ".completion.value" : progressOnePath);
        final String progressValueTwoPath = (progressTwoValue != null ? progressTwoPath + ".completion.value" : progressTwoPath);

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.id",
                        interpolate(activityName, "id"))
                        .jsonPath("$.response.data.learn.cohort.deployment[0].activity.attempt.value", values.get(0).attempt)
                        .jsonPath(progressValueOnePath, progressOneValue)
                        .jsonPath("$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.attempt.value", values.get(1).attempt)
                        .jsonPath(progressValueTwoPath, progressTwoValue));
    }

    @Then("^\"([^\"]*)\" does not get an activity")
    public void doesNotGetAnActivity(String userName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges",
                        "@empty()@"));
    }

    @When("^\"([^\"]*)\" restarts the \"([^\"]*)\" activity for \"([^\"]*)\" deployment")
    public void restartsTheLessonForDeployment(String userName, String activityName, String deploymentName) {
        authenticationSteps.authenticateUser(userName);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.activity.restart")
                .addField("activityId", interpolate(nameFrom(activityName, "id")))
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .build());

        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.type", "learner.activity.restart.ok"));
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "The citrus writes to this fields with values from feature files.")
    public static class ProgressItem {
        final String id;
        final Integer attempt;
        final Double progress;

        public ProgressItem(String id, Integer attempt, Double progress) {
            this.id = id;
            this.attempt = attempt;
            this.progress = progress;
        }
    }
}
