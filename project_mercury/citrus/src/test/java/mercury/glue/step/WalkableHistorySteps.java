package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.helpers.learner.WalkableHistoryHelper;

public class WalkableHistorySteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches the completed walkable history for {string} pathway on deployment {string}")
    public void fetchesTheCompletedWalkableHistoryForPathway(String user, String pathwayName, String deploymentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, WalkableHistoryHelper.fetchHistory(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(pathwayName, "id"))
        ));
    }

    @Then("the following completed walkables are returned")
    public void theFollowingCompletedWalkablesAreReturned(List<String> expectedWalkableNames) {

        messageOperations.receiveJSON(runner, action -> action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
            @Override
            public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                Map response = (Map) payload.get("response");
                Map data = (Map) response.get("data");
                Map learn = (Map) data.get("learn");
                Map cohort = (Map) learn.get("cohort");
                List deployment = (List) cohort.get("deployment");
                Map activity = (Map) ((Map) deployment.get(0)).get("activity");
                List pathways = (List) activity.get("pathways");
                Map pathway = (Map) pathways.get(0);
                Map history = (Map) pathway.get("history");
                List edges = (List) history.get("edges");

                for (int i = 0; i < edges.size(); i++) {
                    Map edge = (Map) edges.get(i);
                    Map node = (Map) edge.get("node");

                    assertEquals(context.getVariable(interpolate(nameFrom(expectedWalkableNames.get(i), "id"))), String.valueOf(node.get("elementId")));
                    Object evaluationId = node.get("evaluationId");
                    assertNotNull(evaluationId);
                    runner.variable(nameFrom(expectedWalkableNames.get(i), "EVALUATION_id"), evaluationId.toString());
                    assertNotNull(node.get("evaluation"));
                    assertNotNull(node.get("evaluatedAt"));
                    assertNotNull(node.get("configurationFields"));

                    Map scopePayload = (Map) node.get("evaluation");
                    List scopes = (List) scopePayload.get("scope");
                    assertNotNull(scopes);
                }
            }
        }));
    }

    @When("{string} fetches the evaluation {string} on deployment {string}")
    public void fetchesTheEvaluationForPathwayOnDeployment(String user, String evaluationName, String deploymentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendGraphQL(runner, WalkableHistoryHelper.fetchEvaluation(
                interpolate("cohort_id"),
                interpolate(nameFrom(deploymentName, "id")),
                interpolate(nameFrom(evaluationName, "id"))
        ));
    }

    @Then("evaluation {string} is returned correctly")
    public void evaluationIsReturnedCorrectly(String evaluationName) {
        String evaluationPath = "4.response.data.learn.cohort.deployment[0].evaluation";
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath(evaluationPath, "@notEmpty()@")
                .jsonPath(evaluationPath + ".id", interpolate(nameFrom(evaluationName, "id")))
                .jsonPath(evaluationPath + ".elementId", "@notEmpty()@")
                .jsonPath(evaluationPath + ".elementType", "INTERACTIVE")
                .jsonPath(evaluationPath + ".studentId", "@notEmpty()@")
                .jsonPath(evaluationPath + ".attemptId", "@notEmpty()@")
                .jsonPath(evaluationPath + ".studentScopeURN", "@notEmpty()@")
                .jsonPath(evaluationPath + ".completed", true)
                .jsonPath(evaluationPath + ".scope", "@notEmpty()@")
        );
    }
}
