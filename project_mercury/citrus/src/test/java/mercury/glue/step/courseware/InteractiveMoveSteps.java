package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.InteractiveHelper.interactiveMoveOk;
import static mercury.helpers.courseware.InteractiveHelper.moveInteractiveToPathway;
import static org.junit.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class InteractiveMoveSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} moves {string} interactive to {string} pathway at position {int}")
    public void movesInteractiveToPathwayAtPosition(String accountName, String interactiveName, String pathwayName, int index) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, moveInteractiveToPathway(interpolate(nameFrom(interactiveName, "id")),
                                                                 interpolate(nameFrom(pathwayName, "id")), index));
    }

    @Then("the {string} interactive has been successfully moved to {string} pathway")
    public void theInteractiveHasBeenSuccessfullyMoved(String interactiveName, String pathwayName) {
        messageOperations.receiveJSON(runner, action ->
                action.validationCallback(interactiveMoveOk((payload, context) -> {
                    assertNotNull(payload);
                    assertNotNull(payload.getInteractiveId());
                    assertNotNull(payload.getParentPathwayId());
                    context.setVariable(nameFrom(interactiveName, "id"), payload.getInteractiveId().toString());
                    context.setVariable(nameFrom(pathwayName, "id"), payload.getParentPathwayId().toString());
                })));
    }


    @Then("{string} can move {string} interactive to {string} pathway at position {int}")
    public void canMoveInteractiveToPathway(String accountName, String interactiveName, String pathwayName, int index) {
        movesInteractiveToPathwayAtPosition(accountName, interactiveName, pathwayName, index);
        theInteractiveHasBeenSuccessfullyMoved(interactiveName, pathwayName);
    }

    @Then("^the interactive move fails with message \"([^\"]*)\" and code (\\d+)$")
    public void theInteractiveMoveFailsWithMessageAndCode(String message, int code) {
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"@assertThat(anyOf(equalTo(author.interactive.move.error), equalTo(workspace.interactive.move.error)))@\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("{string} can not move {string} interactive to {string} pathway at position {int} due to error: code {int} message {string}")
    public void canNotMoveInteractiveToPathwayAtPositionDueToErrorCodeMessage(String accountName,
                                                                             String interactiveName,
                                                                             String pathwayName,
                                                                             int index,
                                                                             int code,
                                                                             String message) {
        movesInteractiveToPathwayAtPosition(accountName, interactiveName, pathwayName, index);
        theInteractiveMoveFailsWithMessageAndCode(message, code);
    }
}
