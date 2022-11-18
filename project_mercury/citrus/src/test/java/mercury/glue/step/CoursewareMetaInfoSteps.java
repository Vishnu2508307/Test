package mercury.glue.step;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;

public class CoursewareMetaInfoSteps {
    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} set meta info on courseware {string} {string} with key {string} and value {string}")
    public void setMetaInfoOnCoursewareWithKeyAndValue(String user, String elementType, String elementName, String key, String value) {
        authenticationSteps.authenticateUser(user);

        String payload = new PayloadBuilder()
                .addField("type", "workspace.courseware.meta.info.set")
                .addField("elementType", elementType)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("key", key)
                .addField("value", value)
                .build();

        messageOperations.sendJSON(runner, payload);
    }

    @Then("{string} is not allow to set meta info on the courseware")
    public void isNotAllowToSetMetaInfoOnTheCourseware(String user) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                                "\"type\":\"workspace.courseware.meta.info.set.error\"," +
                                "\"code\":401," +
                                "\"message\":\"Unauthorized: Unauthorized permission level\"," +
                                "\"replyTo\":\"@notEmpty()@\"}"));
    }

    @Then("courseware {string} has meta info key {string} with value {string}")
    public void coursewareHasMetaInfoKeyWithValue(String elementName, String key, String value) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.courseware.meta.info.set.ok")
                .jsonPath("$.response.coursewareElementMetaInformation.key", key)
                .jsonPath("$.response.coursewareElementMetaInformation.value", value)
                .jsonPath("$.response.coursewareElementMetaInformation.elementId", interpolate(nameFrom(elementName, "id")))
        );
    }

    @And("{string} has set meta info on courseware {string} {string} with key {string} and value {string}")
    public void hasSetMetaInfoOnCoursewareWithKeyAndValue(String user, String elementType, String elementName, String key, String val) {
        setMetaInfoOnCoursewareWithKeyAndValue(user, elementType, elementName, key, val);
        coursewareHasMetaInfoKeyWithValue(elementName, key, val);
    }

    @When("{string} fetches courseware {string} {string} meta info {string}")
    public void fetchesCoursewareMetaInfo(String user, String elementType, String elementName, String key) {
        authenticationSteps.authenticateUser(user);

        String payload = new PayloadBuilder()
                .addField("type", "workspace.courseware.meta.info.get")
                .addField("elementType", elementType)
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("key", key)
                .build();

        messageOperations.sendJSON(runner, payload);
    }

    @Then("the courseware meta info {string} has value {string}")
    public void theCoursewareMetaInfoHasValue(String key, String value) {
        messageOperations.receiveJSON(runner, action -> action
                .jsonPath("$.type", "workspace.courseware.meta.info.get.ok")
                .jsonPath("$.response.coursewareElementMetaInformation.value", value)
                .jsonPath("$.response.coursewareElementMetaInformation.key", key)
        );
    }
}
