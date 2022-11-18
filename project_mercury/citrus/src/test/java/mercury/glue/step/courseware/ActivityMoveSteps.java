package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.ActivityHelper.activityMoveOk;
import static mercury.helpers.courseware.ActivityHelper.moveActivityToPathway;
import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class ActivityMoveSteps {

    public static final String ACTIVITY_COPY_ID_VAR = "activity_copy_id";

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} moves {string} activity to {string} pathway at position {int}")
    public void movesActivityIntoPathwayAtPosition(String accountName, String activityName, String pathwayName, int index) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, moveActivityToPathway(interpolate(nameFrom(activityName, "id")),
                                                                 interpolate(nameFrom(pathwayName, "id")), index));
    }

    @Then("the {string} activity has been successfully moved to {string} pathway")
    public void theActivityHasBeenSuccessfullyMoved(String activityName, String pathwayName) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(activityMoveOk((payload, context) -> {
                    assertNotNull(payload);
                    assertNotNull(payload.getActivityId());
                    assertNotNull(payload.getParentPathwayId());
                    context.setVariable(nameFrom(activityName, "id"), String.valueOf(payload.getActivityId()));
                    context.setVariable(nameFrom(pathwayName, "id"), String.valueOf(payload.getParentPathwayId()));
                })));
    }


    @Then("{string} can move {string} activity to {string} pathway at position {int}")
    public void canMoveActivityIntoPathway(String accountName, String activityName, String pathwayName, int index) {
        movesActivityIntoPathwayAtPosition(accountName, activityName, pathwayName, index);
        theActivityHasBeenSuccessfullyMoved(activityName, pathwayName);
    }

    @Then("^the activity move fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theActivityMoveFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"@assertThat(anyOf(equalTo(author.activity.move.error), equalTo(workspace.activity.move.error)))@\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can not move {string} activity to {string} pathway at position {int} due to error: code {int} message {string}")
    public void canNotMoveActivityIntoPathwayAtPositionDueToErrorCodeMessage(String accountName,
                                                                             String activityName,
                                                                             String pathwayName,
                                                                             int index,
                                                                             int code,
                                                                             String message) {
        movesActivityIntoPathwayAtPosition(accountName, activityName, pathwayName, index);
        theActivityMoveFailsWithMessageAndCode(message, code);
    }
}
