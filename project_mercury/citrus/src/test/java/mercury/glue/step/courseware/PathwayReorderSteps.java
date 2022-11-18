package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.step.courseware.PathwayReorderHelper.pathwayReorderErrorResponse;
import static mercury.glue.step.courseware.PathwayReorderHelper.pathwayReorderOkResponse;
import static mercury.glue.step.courseware.PathwayReorderHelper.pathwayReorderRequest;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;
import static mercury.helpers.courseware.PathwayHelper.getPathway;
import static mercury.helpers.courseware.PathwayHelper.validatePathwayPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.glue.step.AuthenticationSteps;

public class PathwayReorderSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("^\"([^\"]*)\" has reordered walkables for the \"([^\"]*)\" pathway")
    public void hasReorderedWalkablesForThePathway(String user, String pathwayName, List<String> walkableNames) {
        authenticationSteps.authenticateUser(user);
        List<String> walkableIds = walkableNames.stream()
                .map(name -> interpolate(nameFrom(name, "id")))
                .collect(Collectors.toList());
        messageOperations.sendJSON(runner, pathwayReorderRequest(interpolate(nameFrom(pathwayName, "id")), walkableIds));
        messageOperations.receiveJSON(runner, action -> action.payload(pathwayReorderOkResponse()));
    }

    @Then("^the \"([^\"]*)\" pathway payload contains reordered walkables")
    public void thePathwayPayloadContainsReorderedWalkables(String pathwayName, List<String> walkableNames) {
        messageOperations.sendJSON(runner, getPathway(interpolate(nameFrom(pathwayName, "id"))));
        messageOperations.receiveJSON(runner, action -> action.validationCallback(validatePathwayPayload(
                (payload, context) -> {
                    List<String> expectedWalkableIds = walkableNames.stream()
                            .map(name -> context.getVariable(nameFrom(name, "id")))
                            .collect(Collectors.toList());
                    List<String> actualWalkableIds = payload.getChildren().stream()
                            .map(WalkableChild::getElementId)
                            .map(UUID::toString)
                            .collect(Collectors.toList());
                    assertEquals(expectedWalkableIds, actualWalkableIds);
                }
        )));

    }

    @Then("^\"([^\"]*)\" can not reorder \"([^\"]*)\" walkable inside \"([^\"]*)\" pathway due to error: code (\\d+) message \"([^\"]*)\"$")
    public void canNotReorderWalkableInsidePathwayDueToErrorCodeMessage(String user, String walkableName, String pathwayName, int code,
                                                                        String message) {
        authenticationSteps.authenticateUser(user);
        messageOperations.sendJSON(runner, pathwayReorderRequest(interpolate(nameFrom(pathwayName, "id")),
                Lists.newArrayList(interpolate(nameFrom(walkableName, "id")))));
        messageOperations.receiveJSON(runner, action -> action.payload(pathwayReorderErrorResponse(code, message)));
    }

    @Then("^\"([^\"]*)\" can reorder \"([^\"]*)\" walkables inside \"([^\"]*)\" pathway successfully$")
    public void canReorderWalkablesInsidePathwaySuccessfully(String user, String walkableNames, String pathwayName) {
        authenticationSteps.authenticateUser(user);
        List<String> names = Stream.of(walkableNames.split(","))
                .map(name -> interpolate(nameFrom(name, "id")))
                .collect(Collectors.toList());

        messageOperations.sendJSON(runner, pathwayReorderRequest(interpolate(nameFrom(pathwayName, "id")), names));
        messageOperations.receiveJSON(runner, action -> action.payload(pathwayReorderOkResponse()));
    }
}
