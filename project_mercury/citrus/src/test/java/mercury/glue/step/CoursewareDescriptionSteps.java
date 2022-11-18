package mercury.glue.step;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

public class CoursewareDescriptionSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} set courseware description {string} for {string} and type {string}")
    public void setCoursewareDescriptionForElement(String user, String description, String elementName, String elementType) {
        authenticationSteps.authenticateUser(user);

        String payload = new PayloadBuilder()
                .addField("type", "project.courseware.description.set")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("description", description)
                .build();

        messageOperations.sendJSON(runner, payload);
    }

    @Then("{string} is not allow to set description on the courseware")
    public void isNotAllowToSetDescriptionOnTheCourseware(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                        "\"type\":\"project.courseware.description.set.error\"," +
                        "\"code\":401," +
                        "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                        "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("the {string} description for {string} and type {string} is set by {string}")
    public void hasCreatedDescriptionForActivityAndType(String description, String elementName, String elementType, String user) {
        authenticationSteps.authenticateUser(user);

        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "project.courseware.description.set.ok")
                .jsonPath("$.response.coursewareElementDescription.elementId", interpolate(nameFrom(elementName, "id")))
                .jsonPath("$.response.coursewareElementDescription.elementType", elementType)
                .jsonPath("$.response.coursewareElementDescription.value", description)
        );
    }

    @And("{string} has added description {string} for {string} {string}")
    public void hasAddedDescriptionFor(String user, String description, String elementName, String elementType) {
        setCoursewareDescriptionForElement(user, description, elementName, elementType);
        hasCreatedDescriptionForActivityAndType(description, elementName, elementType, user);
    }
}
