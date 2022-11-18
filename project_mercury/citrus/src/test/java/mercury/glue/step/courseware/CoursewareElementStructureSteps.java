package mercury.glue.step.courseware;

import static mercury.common.Variables.interpolate;
import static mercury.common.Variables.nameFrom;
import static mercury.glue.wiring.CitrusConfiguration.MESSAGE_OPERATIONS;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.runner.TestRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import mercury.common.MessageOperations;
import mercury.common.PayloadBuilder;
import mercury.glue.step.AuthenticationSteps;

public class CoursewareElementStructureSteps {

    @CitrusResource
    private TestRunner runner;

    @Autowired
    @Qualifier(MESSAGE_OPERATIONS)
    private MessageOperations messageOperations;

    @Autowired
    private AuthenticationSteps authenticationSteps;

    @When("{string} fetches the courseware element structure for {string} {string}")
    public void fetchCoursewareElementStructure(String user, String elementType, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.structure.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware element structure is not returned due to missing permission level")
    public void theCoursewareElementStructureFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.get.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("the courseware element structure is returned successfully for {string}")
    public void theCoursewareElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
            action.payload("{" +
                "\"type\":\"author.courseware.structure.get.ok\"," +
                "\"response\":{" +
                "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"," +
                    "\"parentId\":\"@notEmpty()@\"" +
                "}" +
                "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware root element structure is returned successfully for {string}")
    public void theCoursewareRootElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
            action.payload("{" +
                "\"type\":\"author.courseware.structure.get.ok\"," +
                "\"response\":{" +
                "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"" +
                "}" +
                "},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} fetches the courseware support element structure for {string} {string}")
    public void fetchCoursewareSupportElementStructure(String user, String elementType, String elementName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.support.structure.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware support element structure is not returned due to missing permission level")
    public void theCoursewareSupportElementStructureFetchFailsDueToMissingPermissionLevel() {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.get.error\"," +
                    "\"code\":401," +
                    "\"message\":\"@notEmpty()@\"," +
                    "\"replyTo\":\"@notEmpty()@\"}"));

    }

    @Then("the courseware support element structure is returned successfully for {string}")
    public void theCoursewareSupportElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.get.ok\"," +
                    "\"response\":{" +
                    "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"," +
                    "\"parentId\":\"@notEmpty()@\"" +
                    "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware support root element structure is returned successfully for {string}")
    public void theCoursewareSupportRootElementStructureisReturnedSuccessfully(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.support.structure.get.ok\"," +
                    "\"response\":{" +
                    "\"coursewareStructure\":{" +
                    "\"children\":\"@notEmpty()@\"," +
                    "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                    "\"type\":\"@notEmpty()@\"," +
                    "\"topParentId\":\"@notEmpty()@\"" +
                    "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }

    @When("{string} fetches the courseware element structure for {string} {string} with field {string}")
    public void fetchCoursewareElementStructureWithConfig(String user, String elementType, String elementName, String fieldName) {
        authenticationSteps.authenticateUser(user);

        String message = new PayloadBuilder()
                .addField("type", "author.courseware.structure.get")
                .addField("elementId", interpolate(nameFrom(elementName, "id")))
                .addField("elementType", elementType)
                .addField("fieldNames", Arrays.asList(fieldName)).build();

        messageOperations.sendJSON(runner, message);
    }

    @Then("the courseware element structure is returned successfully for {string} with fields")
    public void theCoursewareElementStructureisReturnedSuccessfullyWithFields(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.get.ok\"," +
                    "\"response\":{" +
                        "\"coursewareStructure\":{" +
                            "\"children\":\"@notEmpty()@\"," +
                            "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                            "\"type\":\"@notEmpty()@\"," +
                            "\"topParentId\":\"@notEmpty()@\"," +
                            "\"parentId\":\"@notEmpty()@\"," +
                            "\"configFields\":\"@notEmpty()@\"" +
                        "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }


    @Then("the courseware root element structure is returned successfully for {string} with fields")
    public void theCoursewareRootElementStructureisReturnedSuccessfullyWithFields(String elementName) {
        messageOperations.receiveJSON(runner, action ->
                action.payload("{" +
                    "\"type\":\"author.courseware.structure.get.ok\"," +
                    "\"response\":{" +
                        "\"coursewareStructure\":{" +
                            "\"children\":\"@notEmpty()@\"," +
                            "\"elementId\":\"" + interpolate(nameFrom(elementName, "id")) + "\"," +
                            "\"type\":\"@notEmpty()@\"," +
                            "\"topParentId\":\"@notEmpty()@\"," +
                            "\"configFields\":\"@notEmpty()@\"" +
                        "}" +
                    "},\"replyTo\":\"@notEmpty()@\"}"));
    }
}
