package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.beust.jcommander.internal.Maps;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class ProgressSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" does evaluation for \"([^\"]*)\" and interactive \"([^\"]*)\" and gets progresses$")
    public void doesEvaluationFor(String user, String deployment, String interactive,
                                  Map<String, Double> progressByTargetName) {
        authenticationSteps.authenticateUser(user);

        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "learner.evaluate")
                .addField("deploymentId", interpolate(deployment, "id"))
                .addField("interactiveId", interpolate(interactive, "id"));

        messageOperations.sendJSON(runner, payload.build());

        Map<String, Double> progressByCoursewareElementId = Maps.newHashMap();
        progressByTargetName.forEach((name, aDouble) -> {
            String coursewareElementId = runner.variable(nameFrom(name, "id"), interpolate(nameFrom(name, "id")));
            progressByCoursewareElementId.put(coursewareElementId, aDouble);
        });

        AtomicInteger broadcastMessages = new AtomicInteger(progressByCoursewareElementId.size());
        AtomicInteger okMessages = new AtomicInteger(1);

        for (int i = 0; i < progressByCoursewareElementId.size() + 1; i++) {
            messageOperations.receiveJSON(runner, action -> action
                    .validationCallback(new JsonMappingValidationCallback<Map>(Map.class) {
                        @Override
                        public void validate(Map payload, Map<String, Object> headers, TestContext context) {

                            //when https://github.com/citrusframework/citrus/issues/626 will be done it will be
                            // possible to use selectors instead of this if
                            if ("learner.progress.broadcast".equals(payload.get("type"))) {
                                broadcastMessages.decrementAndGet();
                                assertEquals("learner.progress.broadcast", payload.get("type"));

                                Map response = (Map) payload.get("response");
                                Map progress = (Map) response.get("progress");
                                String elementId = (String) progress.get("coursewareElementId");

                                assertTrue(String.format("Broadcast message for the elementId '%s' is not expected", elementId),
                                        progressByCoursewareElementId.containsKey(elementId));
                                Double expectedCompletion = progressByCoursewareElementId.get(elementId);

                                Map completion = (Map) progress.get("completion");
                                Double actualCompletion = (Double) completion.get("value");

                                assertEquals(context.getVariable(nameFrom(deployment, "id")), progress.get("deploymentId"));
                                assertEquals(expectedCompletion, actualCompletion);
                            } else {
                                okMessages.decrementAndGet();
                                assertEquals("learner.evaluate.ok", payload.get("type"));
                            }


                        }

                    }));
        }

        assertEquals("Not all learner.progress.broadcast received", 0, broadcastMessages.get());
        assertEquals("learner.evaluate.ok message is not received", 0, okMessages.get());
    }

    @Given("^\"([^\"]*)\" has subscribed to progress for \"([^\"]*)\" and targets$")
    public void hasSubscribedToProgressForAndTargets(String user, String deployment, List<String> coursewareElements) {
        authenticationSteps.authenticateUser(user);

        for (String target : coursewareElements) {
            PayloadBuilder payload = new PayloadBuilder()
                    .addField("type", "learner.progress.subscribe")
                    .addField("deploymentId", interpolate(deployment, "id"))
                    .addField("coursewareElementId", interpolate(target, "id"));

            messageOperations.sendJSON(runner, payload.build());
            messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "learner.progress.subscribe.ok"));
        }

    }

    @When("^\"([^\"]*)\" does evaluation for \"([^\"]*)\" and interactive \"([^\"]*)\"$")
    public void doesEvaluationForAndInteractive(String user, String deployment, String interactive) {
        authenticationSteps.authenticateUser(user);

        PayloadBuilder payload = new PayloadBuilder()
                .addField("type", "learner.evaluate")
                .addField("deploymentId", interpolate(deployment, "id"))
                .addField("interactiveId", interpolate(interactive, "id"));

        messageOperations.sendJSON(runner, payload.build());
        messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "learner.evaluate.ok"));

    }

    @Then("{string} has unsubscribed to progress for {string} and targets")
    public void hasUnsubscribedToProgressForAndTargets(String user, String deployment, List<String> coursewareElements) {
        authenticationSteps.authenticateUser(user);

        for (String target : coursewareElements) {
            PayloadBuilder payload = new PayloadBuilder()
                    .addField("type", "learner.progress.unsubscribe")
                    .addField("deploymentId", interpolate(deployment, "id"))
                    .addField("coursewareElementId", interpolate(target, "id"));

            messageOperations.sendJSON(runner, payload.build());
            messageOperations.receiveJSON(runner, action -> action.jsonPath("$.type", "learner.progress.unsubscribe.ok"));
        }
    }
}
