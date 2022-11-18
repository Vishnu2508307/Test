package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import mercury.common.MessageOperations;
import mercury.common.Variables;

public class ComponentEventSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    private MessageOperations messageOperations;

    @Then("^\"([^\"]*)\" should receive an action \"([^\"]*)\" message for the \"([^\"]*)\" component$")
    public void shouldReceiveAnActionMessageForTheComponent(String clientName,
                                                            String coursewareAction,
                                                            String componentName) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.elementId",
                                interpolate(nameFrom(componentName, "id"))
                        )
                        .jsonPath("$.response.action", coursewareAction)
                        .jsonPath("$.response.rtmEvent", "COMPONENT_" + coursewareAction)
                        .jsonPath("$.response.elementType", "COMPONENT"), clientName);
    }

    @Then("{string} receives {string} changes with action {string} on client {string}")
    public void receivesChangesWithActionOnClient(String user, String componentName, String actionName, String clientName) {
        messageOperations.receiveJSON(runner, action ->
                        action.jsonPath("$.type", "author.activity.broadcast")
                                .jsonPath("$.response.action", actionName)
                                .jsonPath("$.response.rtmEvent", "COMPONENT_" + actionName)
                                .jsonPath("$.response.elementType", "COMPONENT")
                                .jsonPath("$.response.elementId", interpolate(nameFrom(componentName, "id"))),
                clientName);
    }

    @Then("{string} should receive an action {string} message for the {string} {string}")
    public void shouldReceiveAnActionMessageForThe(String clientName, String coursewareAction, String componentName, String elementType) {
        messageOperations.receiveJSON(runner, action ->
                action.jsonPath("$.response.elementId", Variables.interpolate(componentName))
                        .jsonPath("$.response.action", coursewareAction)
                        .jsonPath("$.response.rtmEvent", coursewareAction)
                        .jsonPath("$.response.elementType", elementType.toUpperCase()), clientName);
    }
}
