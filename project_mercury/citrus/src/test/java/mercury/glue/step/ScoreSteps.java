package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.ProvisionSteps.getAccountIdVar;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.learner.ScoreHelper.fetchInteractiveScore;
import static mercury.helpers.learner.ScoreHelper.fetchPathwayScore;
import static mercury.helpers.learner.ScoreHelper.fetchRootActivityScore;
import static mercury.helpers.learner.ScoreHelper.fetchWalkableScore;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.When;
import mercury.common.MessageOperations;

public class ScoreSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches the score for {string} activity for deployment {string}, the score is")
    public void fetchesTheScoreForActivityForDeploymentTheScoreIs(String user, String activityName, String deploymentName,
                                                                  Map<String, String> expectedScore) {
        authenticationSteps.authenticatesViaIes(user);

        messageOperations.sendGraphQL(runner, fetchRootActivityScore(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(activityName, "id"))
        ));

        verifyScore("$.response.data.learn.cohort.deployment[0].activity.score", expectedScore);
    }


    @When("instructor {string} fetches the score for {string} activity for deployment {string}, the score is")
    public void instructorFetchesTheScoreForActivityForDeploymentTheScoreIs(String accountName, String activityName, String deploymentName,
                                                                            Map<String, String> expectedScore) {
        authenticationSteps.authenticateUser(accountName);

        messageOperations.sendGraphQL(runner, fetchRootActivityScore(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(activityName, "id"))
        ));

        verifyScore("$.response.data.learn.cohort.deployment[0].activity.score", expectedScore);
    }

    @When("{string} fetches the score for {string} pathway for deployment {string}, the score is")
    public void fetchesTheScoreForPathwayForDeploymentTheScoreIs(String user, String pathwayName, String deploymentName,
                                                                 Map<String, String> expectedScore) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchPathwayScore(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(pathwayName, "id"))
        ));

        verifyScore("$.response.data.learn.cohort.deployment[0].activity.pathways[0].score", expectedScore);
    }

    private void verifyScore(String scorePath, Map<String, String> expectedValues) {
        messageOperations.receiveJSON(runner, action -> action.jsonPath(scorePath, "@notEmpty()@")
                .jsonPath(scorePath + ".value", Double.valueOf(expectedValues.get("value")))
                .jsonPath(scorePath + ".reason", expectedValues.get("reason")));
    }

    @When("{string} fetches {string} pathway walkable score for deployment {string}, the scores is")
    public void fetchesPathwayWakablesScoresForDeploymentTheScoresAre(String user, String pathwayName, String deploymentName,
                                                                      Map<String, String> expectedScore) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchWalkableScore(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(pathwayName, "id"))
        ));

        String scorePath = "$.response.data.learn.cohort.deployment[0].activity.pathways[0].walkables.edges[0].node.score";

        verifyScore(scorePath, expectedScore);
    }

    @When("{string} fetches {string}'s score for {string} interactive for deployment {string}, the score is")
    public void fetchesTheScoreForInteractiveForDeploymentTheScoreIs(String user, String studentName,
                                                                     String interactiveName, String deploymentName,
                                                                     Map<String, String> expectedValues) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, fetchInteractiveScore(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(interactiveName, "id")),
                interpolate(getAccountIdVar(studentName))
        ));

        String scorePath = "$.response.data.cohortById.enrollmentByStudent.interactiveScore.";

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath(scorePath + "reason", expectedValues.get("reason"))
                .jsonPath(scorePath + "value", Double.valueOf(expectedValues.get("value"))));
    }
}
