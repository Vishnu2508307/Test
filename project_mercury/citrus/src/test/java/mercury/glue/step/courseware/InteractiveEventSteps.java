package mercury.glue.step.courseware;

import static com.smartsparrow.courseware.eventmessage.CoursewareAction.SCENARIO_REORDERED;
import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;

public class InteractiveEventSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;


    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" interactive and \"([^\"]*)\" parent pathway$")
    public void shouldReceiveAnActionMessageForTheInteractive(String clientName, String coursewareAction,
                                                              String interactiveName, String parentPathway) {
        messageOperations.receiveJSON(runner, action -> action.payload("{" +
                "    \"type\": \"author.activity.broadcast\"," +
                "    \"response\": {" +
                "      \"elementId\" : \"" + interpolate(nameFrom(interactiveName, "id")) + "\"," +
                "      \"elementType\" : \"INTERACTIVE\"," +
                "      \"rtmEvent\": \"INTERACTIVE_" + coursewareAction.toUpperCase() + "\"," +
                (parentPathway != null ? "\"parentPathwayId\" : \"" + interpolate(nameFrom(parentPathway, "id")) + "\"," : "") +
                "      \"action\" : \"" + coursewareAction.toUpperCase() + "\"" +
                (coursewareAction.equals(SCENARIO_REORDERED.name()) ? ",\"lifecycle\":\"" + ScenarioSteps.DEFAULT_LIFECYCLE + "\"" : "") +
                (coursewareAction.equals(SCENARIO_REORDERED.name()) ? ",\"scenarioIds\":\"@notEmpty()@\"" : "") +
                "    }," +
                "    \"replyTo\": \"@notEmpty()@\"" +
                "}"), clientName);
    }

    @Then("{string} should receive an action {string} message for the {string} interactive")
    public void shouldReceiveAMessageForTheActivity(String clientName, String coursewareAction, String interactiveName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.elementId", interpolate(nameFrom(interactiveName, "id")))
                        .jsonPath("$.response.action", coursewareAction)
                        .jsonPath("$.response.elementType", "INTERACTIVE"), clientName);
    }

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" interactive with \"([^\"]*)\"$")
    public void shouldReceiveAnActionMessageForTheInteractiveWith(String clientName, String coursewareAction,
                                                                  String interactiveName, String additionalParameter) {
        String fieldName = additionalParameter.split("=")[0];
        String fieldValue = additionalParameter.split("=")[1];
        String expected = "{" +
                            "\"type\":\"author.activity.broadcast\"," +
                            "\"response\":{" +
                                "\"elementId\":\"" + interpolate(nameFrom(interactiveName, "id")) + "\"," +
                                "\"action\":\"CONFIG_CHANGE\"," +
                                "\"elementType\":\"INTERACTIVE\"," +
                                "\"rtmEvent\": \"INTERACTIVE_CONFIG_CHANGE\"," +
                                "\"" + fieldName + "\":\"{\\\"foo\\\":\\\"" + fieldValue + "\\\"}\"}," +
                                "\"replyTo\":\"@notEmpty()@\"}";
        messageOperations.receiveJSON(runner, action -> action.payload(expected), clientName);
    }
}
