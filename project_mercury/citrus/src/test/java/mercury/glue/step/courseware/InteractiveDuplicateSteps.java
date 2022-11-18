package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.InteractiveHelper.duplicateInteractive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class InteractiveDuplicateSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" duplicates \"([^\"]*)\" interactive into \"([^\"]*)\" pathway$")
    public void duplicatesInteractiveIntoPathway(String accountName, String interactiveName, String pathwayName) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, duplicateInteractive(interpolate(nameFrom(interactiveName, "id")),
                interpolate(nameFrom(pathwayName, "id")), null));
    }

    @When("^\"([^\"]*)\" duplicates \"([^\"]*)\" interactive into \"([^\"]*)\" pathway at position (\\d+)$")
    public void duplicatesInteractiveIntoPathwayAtPosition(String accountName, String interactiveName, String pathwayName,
                                                 int index) {
        authenticationSteps.authenticateUser(accountName);
        messageOperations.sendJSON(runner, duplicateInteractive(interpolate(nameFrom(interactiveName, "id")),
                interpolate(nameFrom(pathwayName, "id")), index));
    }

    @Then("^the \"([^\"]*)\" interactive has been successfully duplicated$")
    public void theInteractiveHasBeenSuccessfullyDuplicated(String interactiveName) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "author.interactive.duplicate.ok")
                .extractFromPayload("$.response.interactive.interactiveId", nameFrom(interactiveName, "id")));
    }

    @Then("^\"([^\"]*)\" can not duplicate \"([^\"]*)\" interactive into \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotDuplicateInteractiveIntoPathwayDueToErrorCodeMessage(String accountName, String interactiveName,
                                                                           String pathwayName, int code, String message) {
        duplicatesInteractiveIntoPathway(accountName, interactiveName, pathwayName);
        messageOperations.receiveJSON(runner, action -> action.payload(
                "{\"type\":\"author.interactive.duplicate.error\"," +
                        "\"code\":" + code + "," +
                        "\"message\":\"" + message + "\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("^\"([^\"]*)\" can duplicate \"([^\"]*)\" interactive into \"([^\"]*)\" pathway$")
    public void canDuplicateInteractiveIntoPathway(String accountName, String interactiveName, String pathwayName) {
        duplicatesInteractiveIntoPathway(accountName, interactiveName, pathwayName);
        theInteractiveHasBeenSuccessfullyDuplicated(nameFrom(interactiveName, "COPY"));
    }

}
