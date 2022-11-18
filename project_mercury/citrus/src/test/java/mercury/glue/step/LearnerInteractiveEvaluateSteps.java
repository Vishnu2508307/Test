package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class LearnerInteractiveEvaluateSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" evaluates interactive \"([^\"]*)\" for deployment \"([^\"]*)\"$")
    public void evaluatesInteractiveForDeployment(String user, String interactiveName, String deploymentName) {
        authenticationSteps.authenticateUser(user);

        messageOperations.sendJSON(runner, new PayloadBuilder()
                .addField("type", "learner.evaluate")
                .addField("interactiveId", interpolate(nameFrom(interactiveName, "id")))
                .addField("deploymentId", interpolate(nameFrom(deploymentName, "id")))
                .build());
    }

    @SuppressWarnings("unchecked")
    @Then("^the evaluation result has$")
    public void theEvaluationResultHas(Map<String, String> fields) {

        messageOperations.receiveJSON(runner, action -> {
            action.jsonPath("$.type", "learner.evaluate.ok")
                    .jsonPath("$.response.evaluationResult.triggeredActions", "@notEmpty()@")
                    .jsonPath("$.response.evaluationResult.coursewareElementId", interpolate(nameFrom(fields.get("interactiveName"), "id")))
                    .extractFromPayload("$.response.evaluationResult.interactiveComplete", "interactiveComplete")
                    .jsonPath("$.response.evaluationResult.deployment.id", interpolate(nameFrom(fields.get("deploymentName"), "id")));

            String scenarioCorrectness = fields.get("scenarioCorrectness");

            if (!scenarioCorrectness.equals("null")) {
                action.jsonPath("$.response.evaluationResult.scenarioCorrectness", fields.get("scenarioCorrectness"));
            }

            action.validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                @Override
                public void validate(Map payload, Map<String, Object> headers, TestContext context) {
                    Map response = (Map) payload.get("response");
                    Map evaluationResult = (Map) response.get("evaluationResult");
                    List<Object> triggeredActions = (List<Object>) evaluationResult.get("triggeredActions");
                    Boolean actual = Boolean.valueOf(context.getVariable(interpolate("interactiveComplete")));

                    assertNotNull(triggeredActions);
                    assertFalse(triggeredActions.isEmpty());
                    assertNotNull(actual);
                    assertEquals(Boolean.valueOf(fields.get("interactiveCompleted")), actual);

                    String triggeredActionsSize = fields.get("triggeredActionsSize");

                    if (triggeredActionsSize != null) {
                        int size = Integer.parseInt(triggeredActionsSize);

                        assertEquals(triggeredActions.size(), size);
                    }

                    String defaultProgression = fields.get("defaultProgression");

                    if (defaultProgression != null) {

                        Map action = (Map) triggeredActions.get(0);
                        Map actionContext = (Map) action.get("context");
                        String actualProgressionType = (String) actionContext.get("progressionType");

                        assertEquals(actualProgressionType, defaultProgression);
                    }

                    String firedScenarios = fields.get("fired_scenarios");
                    // get the evaluation mode and default it to DEFAULT when not provided
                    String evaluationMode = fields.getOrDefault("evaluation_mode", "DEFAULT");

                    if (firedScenarios != null) {
                        int firedScenariosSize = Integer.parseInt(firedScenarios);
                        if (firedScenariosSize > 0 || evaluationMode.equals("COMBINED")) {
                            List<Object> scenarioEvaluationResults = (List<Object>) evaluationResult.get("scenarioEvaluationResults");
                            assertEquals(Integer.valueOf(firedScenarios), Integer.valueOf(scenarioEvaluationResults.size()));
                        } else {
                            if (evaluationMode.equals("DEFAULT")) {
                                assertNull(evaluationResult.get("scenarioEvaluationResults"));
                            }
                        }
                    }
                }
            });


            String errorMessage = fields.get("errorMessage");
            if(errorMessage!=null && !errorMessage.equals("null")) {
                action.jsonPath("$.response.evaluationResult.errorMessage",fields.get("errorMessage"));
            }
        });
    }

    @And("^\"([^\"]*)\" has successfully evaluated interactive \"([^\"]*)\" for deployment \"([^\"]*)\"$")
    public void hasSuccessfullyEvaluatedInteractiveForDeployment(String user, String interactiveName, String deploymentName) {
        evaluatesInteractiveForDeployment(user, interactiveName, deploymentName);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "learner.evaluate.ok")
                .jsonPath("$.response.evaluationResult.interactiveComplete", true));
    }
}
